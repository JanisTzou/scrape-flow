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

package com.github.web.scraping.lib.parallelism;

import com.github.web.scraping.lib.throttling.ScrapingRateLimiter;
import com.github.web.scraping.lib.throttling.ThrottlingService;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Log4j2
public class StepTaskExecutor {

    // TODO specialised thread pool for parsing / blocking operations  ...

    public static final Duration PERIODIC_EXEC_NEXT_TRIGGER_INTERVAL = Duration.ofMillis(100);
    public static final long AWAIT_COMPLETION_TIMEOUT_CHECK_FREQUENCY_MILLIS = 10L;
    private final Queue<QueuedStepTask> taskQueue;
    private final ThrottlingService throttlingService;
    private final Duration periodicExecNextTriggerInterval;
    private final ExecutingTasksTracker executingTasksTracker;
    private volatile Supplier<LocalDateTime> nowSupplier;
    private final ExclusiveExecutionTracker exclusiveExecutionTracker;
    private final ScrapingRateLimiter scrapingRateLimiter;
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);

    public StepTaskExecutor(ThrottlingService throttlingService,
                            ExclusiveExecutionTracker exclusiveExecutionTracker,
                            ScrapingRateLimiter scrapingRateLimiter) {
        this(throttlingService,
                PERIODIC_EXEC_NEXT_TRIGGER_INTERVAL, // sensible default
                LocalDateTime::now,
                new ExecutingTasksTracker(),
                exclusiveExecutionTracker, scrapingRateLimiter);
    }

    /**
     * FOR TESTING PURPOSES ONLY
     * package private access is intentional
     */
    StepTaskExecutor(ThrottlingService requestsPerSecondCounter,
                     Duration periodicExecNextTriggerInterval,
                     Supplier<LocalDateTime> nowSupplier,
                     ExecutingTasksTracker executingTasksTracker,
                     ExclusiveExecutionTracker exclusiveExecutionTracker,
                     ScrapingRateLimiter scrapingRateLimiter) {
        this.throttlingService = requestsPerSecondCounter;
        this.executingTasksTracker = executingTasksTracker;
        this.exclusiveExecutionTracker = exclusiveExecutionTracker;
        this.scrapingRateLimiter = scrapingRateLimiter;
        this.taskQueue = new PriorityBlockingQueue<>(100, QueuedStepTask.NATURAL_COMPARATOR);
        this.periodicExecNextTriggerInterval = periodicExecNextTriggerInterval;
        this.nowSupplier = nowSupplier;
        schedulePeriodicExecNextTrigger();
    }


    private void schedulePeriodicExecNextTrigger() {
        Flux.interval(periodicExecNextTriggerInterval, periodicExecNextTriggerInterval)
                .doOnNext(num -> {
                    this.dequeueNextAndExecute();
                })
                .onErrorResume(throwable -> {
                    log.warn("Error in scheduled trigger of dequeueNextAndExecute()", throwable);
                    return Mono.empty();
                })
                .subscribe();
    }

    public void submit(StepTask task,
                       Consumer<TaskResult> taskResultConsumer,
                       Consumer<TaskError> taskErrorConsumer) {
        enqueueTask(task, taskResultConsumer, taskErrorConsumer);
        dequeueNextAndExecute();
    }

    /**
     * must be synchronized -> is called from multiple threads and accesses data that is not thread-safe and operations on it need to be atomic
     */
    private synchronized void enqueueTask(StepTask stepTask, Consumer<TaskResult> taskResultConsumer, Consumer<TaskError> taskErrorConsumer) {
        taskQueue.add(new QueuedStepTask(stepTask, taskResultConsumer, taskErrorConsumer, System.currentTimeMillis()));
        log.trace("New enqueued request info: {}", stepTask.loggingInfo());
        logEnqueuedRequestCount();
    }

    /**
     * must be synchronized -> is called from multiple threads and accesses data that is not thread-safe and operations on it need to be atomic
     */
    private synchronized void dequeueNextAndExecute() {
        try {
            // how to manage the number of executing tasks?

            QueuedStepTask next = taskQueue.peek();

            while (canExecute(next)) { // TODO maybe we are processing too much ? ... we should only take as many as there are threads in the pool ... at most ...
                executingTasksTracker.track(next.getStepTask());
                taskQueue.poll(); // remove from queue head
                executeTask(next.getStepTask(),
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
    private boolean canExecute(QueuedStepTask next) {
        return next != null
                && exclusiveExecutionTracker.canExecute(next)
                && (!next.getStepTask().isMakesHttpRequests() || scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(nowSupplier.get()))
                && (!next.getStepTask().isThrottlingAllowed() || throttlingService.canProceed(executingTasksTracker.countOfExecutingThrottlableTasks()));
    }

    private void executeTask(StepTask task,
                             Consumer<TaskResult> taskResultConsumer,
                             Consumer<TaskError> taskErrorConsumer,
                             long enqueuedTimestamp) {

        AtomicBoolean isRetry = new AtomicBoolean(false);

        Mono.just(task)
                .doOnNext(t -> {
                    log.debug("{} - ... executing ...", task.loggingInfo());
                })
                .map(task0 -> handleTaskIfRetried(isRetry, task0))
                // TODO if task make HttpRequerst then use a different thread pool ...
                .flatMap(canProceed ->
                        Mono.fromCallable(() -> {
                            task.getStepRunnable().run();
                            return task;
                        }) // if we got her eit means that the previous step passed and emmited 'true'
                )
                .onErrorMap(error -> {
                    logRequestError(task, error);
                    executingTasksTracker.untrack(task);   // if an error happened and will be retried at some point, we want to untrack the task so that other requests coming in for the same url do not get ignored
                    return error;
                })
                .retryBackoff(task.getNumOfRetries(), task.getRetryBackoff())
                .onErrorResume(error -> {
                    logDroppingRetrying(task, error);
                    logEnqueuedRequestCount();
                    notifyOnErrorCallback(task, taskErrorConsumer, error);
                    return Mono.empty();
                    // note - do not untrack task here, it should already be untracked by onErrorMap() above
                })
                .doOnNext(data -> {
                    executingTasksTracker.untrack(task);
                    logEnqueuedRequestCount();
                    logRequestProcessed(task, enqueuedTimestamp);
                })
                .map(stepResults -> new TaskResult(task))
                .doOnCancel(taskFinishedHook())
                .doOnTerminate(taskFinishedHook())
                .subscribeOn(Schedulers.parallel())
//                .subscribeOn(Schedulers.single())
                .subscribe(taskResult -> {
                            try {
                                taskResultConsumer.accept(taskResult);
                            } catch (Exception e) {
                                log.error("Error consuming result for task: {}", task.loggingInfo());
                            }
                        },
                        throwable -> {
                            try {
                                taskErrorConsumer.accept(new TaskError(task, throwable));
                            } catch (Exception e) {
                                log.error("Error consuming error result for execution of task: {}", task.loggingInfo());
                            }
                        }
                );

        this.activeTaskCount.incrementAndGet();
    }


    private Runnable taskFinishedHook() {
        return () -> {
            this.activeTaskCount.decrementAndGet();
            this.dequeueNextAndExecute();
        };
    }


    // returns true if te stepTask is within limit. The returned value has no affect though on subsequent items in the Mono chain
    private Mono<Boolean> handleTaskIfRetried(AtomicBoolean isRetry, StepTask stepTask) {
        // done like this with AtomicBoolean because when we poll requests from requestsQueue
        // we have checked that they are within limit so that is ok ...
        // ... but when requests fail and are retried by the Reactor Flux we need to check again
        // the retry because it happens at some later point and we might have run out of rqs / sec for that moment
        // ->>> WE NEED TO MAKE SURE THAT RETRIED REQUEST ALSO RESPECT THE RQs/SEC LIMIT + THAT THEY ARE TRACKED CORRECTLY
        if (isRetry.get()) {
            // TODO call to requestsPerSecondCounter.incrementIfRequestWithinLimitAndGet(nowSupplier.get())
            if (throttlingService.canProceed(executingTasksTracker.countOfExecutingThrottlableTasks())) {
                logRetry(stepTask);
                return Mono.just(true);
            } else {
                // repeat until we are within limit ...
                logDelayedRetry(stepTask);
                return Mono.just(false)
                        .delayElement(periodicExecNextTriggerInterval)
                        .flatMap(dummy -> handleTaskIfRetried(isRetry, stepTask)); // call this method again ... kind of recursively ... until we are within limit at some point

            }
        } else {
            isRetry.set(true); // any subsequent traversal of this mono can only be a retry
            return Mono.just(true);
        }
    }

    private void notifyOnErrorCallback(StepTask task, Consumer<TaskError> taskErrorConsumer, Throwable error) {
        try {
            taskErrorConsumer.accept(new TaskError(task, error));
        } catch (Exception e) {
            log.error("Error in taskErrorConsumer callback: ", e);
        }
    }

    /**
     * @return true if all tasks finished within the specified timeout
     */
    public boolean awaitCompletion(Duration timeout) {

        long checkFrequencyMillis = timeout.toMillis() > AWAIT_COMPLETION_TIMEOUT_CHECK_FREQUENCY_MILLIS ? AWAIT_COMPLETION_TIMEOUT_CHECK_FREQUENCY_MILLIS : 0L;
        Duration period = Duration.ofMillis(checkFrequencyMillis);

        AtomicBoolean withinTimeout = new AtomicBoolean(false);

        try {
            Flux.interval(period, period)
                    .doOnNext(checkNo -> {
                        if (activeTaskCount.get() == 0 && taskQueue.size() == 0) {
                            log.info(">>> Finished scraping <<<");
                            withinTimeout.set(true);
                            throw new TerminateFluxException();
                        }
                    })
                    .blockLast(timeout);
        } catch (TerminateFluxException e) {
            // ok - we terminate the flux with this ...
        }

        return withinTimeout.get(); // if this was set to true we made it within the given timeout
    }

    // used only to terminate a blocking flux from within (no other way to "cancel" it)
    private static class TerminateFluxException extends RuntimeException {
    }

    private void logRequestError(StepTask request, Throwable error) {
        log.warn("Error for request: {}", request.loggingInfo(), error);
    }

    private void logRetry(StepTask request) {
        log.trace("Going to retry request after previous failure {}", request.loggingInfo());
    }

    private void logDelayedRetry(StepTask request) {
        log.trace("Cannot retry request yet - due to rqs per sec. limit {}", request.loggingInfo());
    }

    private void logEnqueuedRequestCount() {
        log.trace("Currently enqueued rqs count = {}", executingTasksTracker.countOfExecutingTasks());
    }

    private void logRequestProcessed(StepTask request, long enqueuedTimestamp) {
        final double processingTime = (System.currentTimeMillis() - enqueuedTimestamp) / 1000.0;
        log.trace("Request took {}s to process: {}", String.format("%.2f", processingTime), request);
    }

    private void logDroppingRetrying(StepTask request, Throwable error) {
        log.trace("Dropping request retry {} after error: ", request.loggingInfo(), error);
    }


    /**
     * FOR TESTING PURPOSES ONLY.
     * Needed so we are able to test changes of behaviour based on the passage of time
     * Package-private visibility intentional
     */
    void setNowSupplier(Supplier<LocalDateTime> nowSupplierNew) {
        this.nowSupplier = nowSupplierNew;
    }

}
