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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.function.Consumer;

public class QueueStepTask {

    public static Comparator<QueueStepTask> NATURAL_COMPARATOR = (qst1, qst2) -> StepTask.NATURAL_COMPARATOR.compare(qst1.getStepTask(), qst2.getStepTask());


    private final StepTask stepTask;
    private final Consumer<TaskResult> pullResultConsumer;
    private final Consumer<TaskError> pullErrorConsumer;
    private final long enqueuedTimestamp;

    public QueueStepTask(StepTask stepTask,
                         Consumer<TaskResult> pullResultConsumer,
                         Consumer<TaskError> pullErrorConsumer,
                         long enqueuedTimestamp) {
        this.stepTask = stepTask;
        this.pullResultConsumer = pullResultConsumer;
        this.pullErrorConsumer = pullErrorConsumer;
        this.enqueuedTimestamp = enqueuedTimestamp;
    }

    public StepTask getStepTask() {
        return stepTask;
    }

    public Consumer<TaskResult> getPullResultConsumer() {
        return pullResultConsumer;
    }

    public Consumer<TaskError> getPullErrorConsumer() {
        return pullErrorConsumer;
    }

    public long getEnqueuedTimestamp() {
        return enqueuedTimestamp;
    }

    public LocalDateTime getCreated() {
        return stepTask.getCreated();
    }
}
