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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;

@RequiredArgsConstructor
@Getter
public class Task implements Comparable<Task> {

    public static Comparator<Task> NATURAL_COMPARATOR = (st1, st2) -> {
        return StepOrder.NATURAL_COMPARATOR.compare(st1.stepOrder, st2.stepOrder);
    };

    private final StepOrder stepOrder;

    /**
     * Tasks with this setting will be executed with priority
     * The implementation should effectively disable the execution of
     * tasks with subsequent stepOrder values which have with
     * exclusive = false for the time exclusive tasks are running.
     */
    private final boolean exclusiveExecution;

    private final String stepName;
    private final Runnable stepRunnable;
    private final boolean throttlingAllowed;
    private final boolean makingHttpRequests;

    private final int retries;
    private final Duration retryBackoff;

    private final LocalDateTime created = LocalDateTime.now();

    public static Task from(TaskBasis basis, int retries, Duration retryBackoff) {
        return new Task(
                basis.getStepOrder(),
                basis.isExclusiveExecution(),
                basis.getStepName(),
                basis.getStepRunnable(),
                basis.isThrottlingAllowed(),
                basis.isMakingHttpRequests(),
                retries,
                retryBackoff
        );
    }

    @Override
    public int compareTo(Task o) {
        return NATURAL_COMPARATOR.compare(this, o);
    }

    @Override
    public String toString() {
        return "StepTask{" +
                "stepOrder=" + stepOrder +
                ", exclusiveExecution=" + exclusiveExecution +
                ", stepName='" + stepName + '\'' +
                ", created=" + created +
                '}';
    }

    public String loggingInfo() {
        return toString();
    }

    public long getNumOfRetries() {
        return retries;
    }

    public Duration getRetryBackoff() {
        return retryBackoff;
    }
}
