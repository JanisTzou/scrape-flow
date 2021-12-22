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
import java.util.function.Consumer;
import java.util.function.Supplier;

@Log4j2
public class TaskQueue {

    public static final Duration PERIODIC_EXEC_NEXT_TRIGGER_INTERVAL = Duration.ofMillis(5);
    private final Queue<QueueStepTask> taskQueue;
    private final ThrottlingService throttlingService;
    private final Duration periodicExecNextTriggerInterval;
    private final ExecutingTasksTracker executingTasksTracker;
    private volatile Supplier<LocalDateTime> nowSupplier;

    public TaskQueue(ThrottlingService throttlingService) {
        this(throttlingService,
                PERIODIC_EXEC_NEXT_TRIGGER_INTERVAL, // sensible default
                LocalDateTime::now,
                new ExecutingTasksTracker()
        );
    }

    /**
     * FOR TESTING PURPOSES ONLY
     * package private access is intentional
     */
    TaskQueue(ThrottlingService requestsPerSecondCounter,
              Duration periodicPullNextTriggerInterval,
              Supplier<LocalDateTime> nowSupplier, ExecutingTasksTracker executingTasksTracker) {
        this.throttlingService = requestsPerSecondCounter;
        this.executingTasksTracker = executingTasksTracker;
        this.taskQueue = new PriorityBlockingQueue<>(100, QueueStepTask.NATURAL_COMPARATOR);
        this.periodicExecNextTriggerInterval = periodicPullNextTriggerInterval;
        this.nowSupplier = nowSupplier;
        schedulePeriodicPullNextTrigger();
    }


    private void schedulePeriodicPullNextTrigger() {
        Flux.interval(periodicExecNextTriggerInterval, periodicExecNextTriggerInterval)
                .doOnNext(num -> {
//                    jsonLog.debug("Running regular trigger of pullNext()");
                    this.dequeueNextAndExecute();
                })
                .onErrorResume(throwable -> {
                    log.warn("Error in scheduled trigger of pullNext()", throwable);
                    return Mono.empty();
                })
                .subscribe();
    }

    public void submit(StepTask task,
                       Consumer<TaskResult> pullResultConsumer,
                       Consumer<TaskError> pullErrorConsumer) {
        enqueueTask(task, pullResultConsumer, pullErrorConsumer);
        dequeueNextAndExecute();
    }

    /**
     * must be synchronized -> is called from multiple threads and accesses data that is not thread-safe and operations on it need to be atomic
     */
    private synchronized void enqueueTask(StepTask feedRequest, Consumer<TaskResult> pullResultConsumer, Consumer<TaskError> pullErrorConsumer) {
        taskQueue.add(new QueueStepTask(feedRequest, pullResultConsumer, pullErrorConsumer, System.currentTimeMillis()));
        log.trace("New enqueued request info: {}", feedRequest.loggingInfo());
        logEnqueuedRequestCount();
    }

    /**
     * must be synchronized -> is called from multiple threads and accesses data that is not thread-safe and operations on it need to be atomic
     */
    private synchronized void dequeueNextAndExecute() {
        try {
            QueueStepTask next = taskQueue.peek();
            while (canExecute(next)) { // TODO maybe we are processing too much ? ... we should only take as many as there are threads in the pool ... at most ...
                executingTasksTracker.track(next.getStepTask());
                taskQueue.poll(); // remove from queue head
                executeTask(next.getStepTask(),
                        next.getPullResultConsumer(),
                        next.getPullErrorConsumer(),
                        next.getEnqueuedTimestamp()
                );
                next = taskQueue.peek();
            }
        } catch (Exception e) {
            log.error("Error pulling next request!", e);
        }
    }

    /**
     * Non-throttlable tasks can proceed without limits. Throttlable tasks need to be limited in terms of how many are executed in parallel
     */
    private boolean canExecute(QueueStepTask next) {
        return next != null && (!next.getStepTask().isThrottlingAllowed() || throttlingService.canProceed(executingTasksTracker.countOfExecutingThrottlableTasks()));
    }

    private void executeTask(StepTask task,
                             Consumer<TaskResult> pullResultConsumer,
                             Consumer<TaskError> pullErrorConsumer,
                             long enqueuedTimestamp) {

        AtomicBoolean isRetry = new AtomicBoolean(false);

        Mono.just(task)
                .doOnNext(t -> {
                    log.trace("{} - ... executing ...", task.loggingInfo());
                })
                .map(task0 -> handleTaskIfRetried(isRetry, task0))
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
                    notifyOnErrorCallback(task, pullErrorConsumer, error);
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
//                .subscribeOn(Schedulers.parallel())
                .subscribeOn(Schedulers.single())
                .subscribe(taskResult -> {
                            try {
                                pullResultConsumer.accept(taskResult);
                            } catch (Exception e) {
                                log.error("Error consuming result for task: {}", task.loggingInfo());
                            }
                        },
                        throwable -> {
                            try {
                                pullErrorConsumer.accept(new TaskError(task, throwable));
                            } catch (Exception e) {
                                log.error("Error consuming error result for execution of task: {}", task.loggingInfo());
                            }
                        }
                );
    }


    private Runnable taskFinishedHook() {
        return this::dequeueNextAndExecute;
    }


    // returns true if te stepTask is within limit. The returned value has no affect though on subsequent items in the Mono chain
    private Mono<Boolean> handleTaskIfRetried(AtomicBoolean isRetry, StepTask stepTask) {
        // done like this with AtomicBoolean because when we poll requests from requestsQueue
        // we have checked that they are within limit so that is ok ...
        // ... but when requests fail and are retried by the Reactor Flux we need to check again
        // the retry because it happens at some later point and we might have run out of rqs / sec for that moment
        // ->>> WE NEED TO MAKE SURE THAT RETRIED REQUEST ALSO RESPECT THE RQs/SEC LIMIT + THAT THEY ARE TRACKED CORRECTLY
        if (isRetry.get()) {
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

    private void notifyOnErrorCallback(StepTask task, Consumer<TaskError> pullErrorConsumer, Throwable error) {
        try {
            pullErrorConsumer.accept(new TaskError(task, error));
        } catch (Exception e) {
            log.error("Error in pullErrorConsumer callback: ", e);
        }
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
