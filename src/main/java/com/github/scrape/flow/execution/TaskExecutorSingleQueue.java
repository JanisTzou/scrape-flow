/*
 * Copyright 2021 Janis Tzoumas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.scrape.flow.execution;

import com.github.scrape.flow.clients.ClientAccessManager;
import com.github.scrape.flow.throttling.ScrapingRateLimiter;
import com.github.scrape.flow.throttling.ThrottlingService;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Log4j2
public class TaskExecutorSingleQueue implements TaskExecutor {

    public static final Duration PERIODIC_EXEC_NEXT_TRIGGER_INTERVAL = Duration.ofMillis(100);
    public static final long COMPLETION_CHECK_FREQUENCY_MILLIS = 100L;
    private final Queue<QueuedTask> taskQueue;
    private final ThrottlingService throttlingService;
    private final Duration periodicExecNextTriggerInterval;
    private final ExecutingTasksTracker executingTasksTracker;
    private final ExclusiveExecutionTracker exclusiveExecutionTracker;
    private final ActiveStepsTracker activeStepsTracker;
    private final ScrapingRateLimiter scrapingRateLimiter;
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    // TODO number should be at least the number of open windows in chrome ...
    //  depends how we will handle windows ... vs threads ...
    //  in fact we should not have separate threads for loading stuff ...
    private static final Scheduler blockingTasksScheduler = Schedulers.newBoundedElastic(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, "io-worker", 60, true);
    private final Supplier<LocalDateTime> nowSupplier;
    private final ClientAccessManager clientAccessManager;

    private long lastActivatedReservation = 0L;
    private long lastLoggedNoProgress = 0L;

    public TaskExecutorSingleQueue(ThrottlingService throttlingService,
                                   ExclusiveExecutionTracker exclusiveExecutionTracker,
                                   ScrapingRateLimiter scrapingRateLimiter,
                                   ActiveStepsTracker activeStepsTracker,
                                   ClientAccessManager clientAccessManager) {
        this(throttlingService,
                PERIODIC_EXEC_NEXT_TRIGGER_INTERVAL, // sensible default
                LocalDateTime::now,
                new ExecutingTasksTracker(),
                exclusiveExecutionTracker,
                activeStepsTracker,
                scrapingRateLimiter,
                clientAccessManager);
    }

    /**
     * FOR TESTING PURPOSES ONLY
     * package private access is intentional
     */
    TaskExecutorSingleQueue(ThrottlingService requestsPerSecondCounter,
                            Duration periodicExecNextTriggerInterval,
                            Supplier<LocalDateTime> nowSupplier,
                            ExecutingTasksTracker executingTasksTracker,
                            ExclusiveExecutionTracker exclusiveExecutionTracker,
                            ActiveStepsTracker activeStepsTracker,
                            ScrapingRateLimiter scrapingRateLimiter,
                            ClientAccessManager clientAccessManager) {
        this.throttlingService = requestsPerSecondCounter;
        this.executingTasksTracker = executingTasksTracker;
        this.exclusiveExecutionTracker = exclusiveExecutionTracker;
        this.activeStepsTracker = activeStepsTracker;
        this.scrapingRateLimiter = scrapingRateLimiter;
        this.clientAccessManager = clientAccessManager;
        this.taskQueue = new PriorityBlockingQueue<>(100, QueuedTask.NATURAL_COMPARATOR);
        this.periodicExecNextTriggerInterval = periodicExecNextTriggerInterval;
        this.nowSupplier = nowSupplier;
        schedulePeriodicExecNextTrigger();
    }


    private void schedulePeriodicExecNextTrigger() {
        Flux.interval(periodicExecNextTriggerInterval, periodicExecNextTriggerInterval)
                .doOnNext(num -> this.dequeueNextAndExecute())
                .onErrorResume(throwable -> {
                    log.warn("Error in scheduled trigger of dequeueNextAndExecute()", throwable);
                    return Mono.empty();
                })
                .subscribe();
    }

    @Override
    public void submit(Task task,
                       Consumer<TaskResult> taskResultConsumer,
                       Consumer<TaskError> taskErrorConsumer) {
        enqueueTask(task, taskResultConsumer, taskErrorConsumer);
        dequeueNextAndExecute();
    }

    /**
     * must be synchronized -> is called from multiple threads and accesses data that is not thread-safe and operations on it need to be atomic
     */
    private synchronized void enqueueTask(Task task,
                                          Consumer<TaskResult> taskResultConsumer,
                                          Consumer<TaskError> taskErrorConsumer) {
        clientAccessManager.makeReservationPlaceholder(task.getClientReservationRequest());
        taskQueue.add(new QueuedTask(task, taskResultConsumer, taskErrorConsumer, System.currentTimeMillis()));
        log.trace("New enqueued request info: {}", task.loggingInfo());
        logEnqueuedRequestCount();
    }

    /**
     * must be synchronized -> is called from multiple threads and accesses data that is not thread-safe and operations on it need to be atomic
     */
    private synchronized void dequeueNextAndExecute() {
        try {
            // how to manage the number of executing tasks?

            QueuedTask next = taskQueue.peek();

            while (canExecute(next)) {
                clientAccessManager.activateReservation(next.getTask().getClientReservationRequest());
                executingTasksTracker.track(next.getTask());
                taskQueue.poll(); // remove from queue head
                executeTaskAsync(next.getTask(),
                        next.getTaskResultConsumer(),
                        next.getTaskErrorConsumer(),
                        next.getEnqueuedTimestamp()
                );
                next = taskQueue.peek();
            }
        } catch (Exception e) {
            log.error("Error executing next task!", e);
        }
    }

    /**
     * Non-throttlable tasks can proceed without limits. Throttlable tasks need to be limited in terms of how many are executed in parallel
     */
    private boolean canExecute(QueuedTask next) {
        if (next == null) {
            return false;
        }
        Task task = next.getTask();
        return isParentTaskFinished(task)
                && exclusiveExecutionTracker.canExecute(next)
                && isWithinScrapingLimits(task)
                && canActivateReservation(task);
    }

    private boolean isParentTaskFinished(Task task) {
        // super important that children do not skip parent tasks ... issues that are hard to debug ...
        return task.getStepOrder().getParent().map(pt -> !activeStepsTracker.isActive(pt)).orElse(true);
    }

    private boolean isWithinScrapingLimits(Task task) {
        return (!task.isMakingHttpRequests() || scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(nowSupplier.get()))
                && (!task.isThrottlingAllowed() || throttlingService.isWithinLimit(executingTasksTracker.countOfExecutingThrottlableTasks()));
    }

    private boolean canActivateReservation(Task task) {
        boolean success = clientAccessManager.canActivateReservation(task.getClientReservationRequest());
        checkAbilityToMakeProgress(success);
        return success;
    }

    private void checkAbilityToMakeProgress(boolean clientReservationSuccess) {
        long now = System.currentTimeMillis();
        if (clientReservationSuccess) {
            this.lastActivatedReservation = now;
            this.lastLoggedNoProgress = now;
        } else {
            if (this.lastActivatedReservation == 0L) {
                this.lastActivatedReservation = now;
                this.lastLoggedNoProgress = now;
            } else {
                if ((now - this.lastActivatedReservation) > 10_000) {
                    if ((now - this.lastLoggedNoProgress) > 10_000) {
                        this.lastLoggedNoProgress = now;
                        log.warn("It seems we cannot make progress due to lack of clients -> increase the max number of client instances");
                    }
                }
            }
        }
    }

    private void executeTaskAsync(Task task,
                                  Consumer<TaskResult> taskResultConsumer,
                                  Consumer<TaskError> taskErrorConsumer,
                                  long enqueuedTimestamp) {

        AtomicBoolean isRetry = new AtomicBoolean(false);

        Mono.just(task)
                .doOnNext(t -> log.debug("{} - ... executing ...", task.loggingInfo()))
                .map(task0 -> handleTaskIfRetried(isRetry, task0))
                .flatMap(canProceed -> runTask(task))
                .publishOn(Schedulers.parallel())
                .onErrorMap(error -> {
                    logRequestError(task, error);
                    return error;
                })
                .retryWhen(Retry.backoff(task.getMaxRetries(), task.getRetryBackoff()).filter(t -> !(t instanceof FlowException)))
                .onErrorResume(error -> {
                    logDroppingRetrying(task, error);
                    logEnqueuedRequestCount();
                    notifyOnErrorCallback(task, taskErrorConsumer, error);
                    executingTasksTracker.untrack(task);   // only untrack here when retries have finished
                    return Mono.empty();
                })
                .doOnNext(data -> {
                    executingTasksTracker.untrack(task);
                    logEnqueuedRequestCount();
                    logRequestProcessed(task, enqueuedTimestamp);
                })
                .map(TaskResult::new)
                .doOnCancel(taskFinishedHook(task))
                .doOnTerminate(taskFinishedHook(task))
                .subscribeOn(Schedulers.parallel())
                .subscribe(taskResult -> {
                            try {
                                taskResultConsumer.accept(taskResult);
                            } catch (Exception e) {
                                log.error("Error consuming result for task: {}", task.loggingInfo(), e);
                            }
                        },
                        throwable -> {
                            try {
                                taskErrorConsumer.accept(new TaskError(task, throwable));
                            } catch (Exception e) {
                                log.error("Error consuming error result for execution of task: {}", task.loggingInfo(), e);
                            }
                        }
                );

        this.activeTaskCount.incrementAndGet();
    }

    private Mono<Task> runTask(Task task) {
        // if we got here it means that the previous step passed and emitted 'true'
        Mono<Task> mono = Mono.fromCallable(() -> {
            task.getStepRunnable().run();
            return task;
        });
        if (task.isMakingHttpRequests()) {
            return mono.subscribeOn(blockingTasksScheduler);
        } else {
            return mono;
        }
    }

    private Runnable taskFinishedHook(Task task) {
        return () -> {
            log.debug("Finished step {}", task.loggingInfo());
            clientAccessManager.finishReservation(task.getStepOrder());
            this.activeTaskCount.decrementAndGet();
            this.dequeueNextAndExecute();
        };
    }

    // returns true if the stepTask is within limit and any subsequent execution of this block for the
    // same flux/mono instance will mean that it is a retry after failure
    private Mono<Boolean> handleTaskIfRetried(AtomicBoolean isRetry, Task task) {
        if (isRetry.get()) {
            if (isWithinScrapingLimits(task)) {
                logRetry(task);
                return Mono.just(true);
            } else {
                // repeat until we are within limit ...
                logDelayedRetry(task);
                return Mono.just(false)
                        .delayElement(periodicExecNextTriggerInterval)
                        .flatMap(dummy -> handleTaskIfRetried(isRetry, task)); // call this method again ... kind of recursively ... until we are within limit at some point

            }
        } else {
            isRetry.set(true); // any subsequent traversal of this mono can only be a retry
            return Mono.just(true);
        }
    }

    private void notifyOnErrorCallback(Task task, Consumer<TaskError> taskErrorConsumer, Throwable error) {
        try {
            taskErrorConsumer.accept(new TaskError(task, error));
        } catch (Exception e) {
            log.error("Error in taskErrorConsumer callback: ", e);
        }
    }

    /**
     * @return true if all tasks finished within the specified timeout
     */
    @Override
    public boolean awaitCompletion(Duration timeout) {

        LocalDateTime start = LocalDateTime.now();
        long checkFrequencyMillis = timeout.toMillis() > COMPLETION_CHECK_FREQUENCY_MILLIS ? COMPLETION_CHECK_FREQUENCY_MILLIS : 1L;
        Duration period = Duration.ofMillis(checkFrequencyMillis);

        AtomicBoolean withinTimeout = new AtomicBoolean(false);

        try {
            Flux.interval(period, period)
                    .doOnNext(checkNo -> {
                        // TODO cleanup reasources ... open browser windows and such ...
                        if (activeTaskCount.get() == 0 && taskQueue.size() == 0) {
                            LocalDateTime end = LocalDateTime.now();
                            long millis = ChronoUnit.MILLIS.between(start, end);
                            log.info(">>> Finished scraping in {}s <<<", millis / 1000.0);
                            withinTimeout.set(true);
                            throw new TerminateFluxException();
                        }
                    })
                    .blockLast(timeout);
        } catch (TerminateFluxException e) {
            // ok - we terminate the flux with this ...
        } catch (Exception e) {
            // TODO we need a way to cancel the running tasks ... and kill all the scraping here ...
            log.warn("Scraping did not manage to finish within the specified timeout {}", timeout);
            return false;
        }

        return withinTimeout.get(); // if this was set to true we made it within the given timeout
    }

    private void logRequestError(Task request, Throwable error) {
        log.warn("Error for task: {}", request.loggingInfo(), error);
    }

    private void logRetry(Task request) {
        log.info("Going to retry task after previous failure {}", request.loggingInfo());
    }

    private void logDelayedRetry(Task request) {
        log.trace("Cannot retry task yet - due to rqs per sec. limit {}", request.loggingInfo());
    }

    private void logEnqueuedRequestCount() {
        log.trace("Currently enqueued tasks count = {}", executingTasksTracker.countOfExecutingTasks());
    }

    private void logRequestProcessed(Task request, long enqueuedTimestamp) {
        final double processingTime = (System.currentTimeMillis() - enqueuedTimestamp) / 1000.0;
        log.trace("Task took {}s to process: {}", String.format("%.2f", processingTime), request);
    }

    private void logDroppingRetrying(Task request, Throwable error) {
        log.trace("Dropping task retry {} after error: ", request.loggingInfo(), error);
    }

    // used only to terminate a blocking flux from within (no other way to "cancel" it)
    private static class TerminateFluxException extends RuntimeException {
    }

}
