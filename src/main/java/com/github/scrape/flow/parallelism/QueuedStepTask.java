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

package com.github.scrape.flow.parallelism;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.function.Consumer;

public class QueuedStepTask {

    public static final Comparator<QueuedStepTask> NATURAL_COMPARATOR = (qst1, qst2) -> Task.NATURAL_COMPARATOR.compare(qst1.getStepTask(), qst2.getStepTask());

    private final Task task;
    private final Consumer<TaskResult> taskResultConsumer;
    private final Consumer<TaskError> taskErrorConsumer;
    private final long enqueuedTimestamp;

    public QueuedStepTask(Task task,
                          Consumer<TaskResult> taskResultConsumer,
                          Consumer<TaskError> taskErrorConsumer,
                          long enqueuedTimestamp) {
        this.task = task;
        this.taskResultConsumer = taskResultConsumer;
        this.taskErrorConsumer = taskErrorConsumer;
        this.enqueuedTimestamp = enqueuedTimestamp;
    }

    public Task getStepTask() {
        return task;
    }

    public Consumer<TaskResult> getTaskResultConsumer() {
        return taskResultConsumer;
    }

    public Consumer<TaskError> getTaskErrorConsumer() {
        return taskErrorConsumer;
    }

    public long getEnqueuedTimestamp() {
        return enqueuedTimestamp;
    }

    public LocalDateTime getCreated() {
        return task.getCreated();
    }
}
