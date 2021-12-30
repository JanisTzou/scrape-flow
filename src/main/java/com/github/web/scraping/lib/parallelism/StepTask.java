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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;

@RequiredArgsConstructor
@Getter
public class StepTask implements Comparable<StepTask> {

    public static Comparator<StepTask> NATURAL_COMPARATOR = (st1, st2) -> {
        return StepExecOrder.NATURAL_COMPARATOR.compare(st1.stepExecOrder, st2.stepExecOrder);
    };

    private final StepExecOrder stepExecOrder;

    /**
     * Tasks with this setting will be executed with priority
     * The implementation should effectively disable the execution of
     * tasks with subsequent stepExecOrder values which have with
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

    public static StepTask from(StepTaskBasis basis, int retries, Duration retryBackoff) {
        return new StepTask(
                basis.getStepExecOrder(),
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
    public int compareTo(StepTask o) {
        return NATURAL_COMPARATOR.compare(this, o);
    }

    @Override
    public String toString() {
        return "StepTask{" +
                "stepExecOrder=" + stepExecOrder +
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
