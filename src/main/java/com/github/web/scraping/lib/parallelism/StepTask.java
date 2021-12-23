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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public class StepTask implements Comparable<StepTask> {

    public static Comparator<StepTask> NATURAL_COMPARATOR = (st1, st2) -> {
        return StepExecOrder.NATURAL_COMPARATOR.compare(st1.stepExecOrder, st2.stepExecOrder);
    };

    private final StepExecOrder stepExecOrder;

    /**
     * Tasks with this setting will be executed with priority
     * The implementation should effectively disable the execution of tasks without exclusive order for the time.
     */
    private final boolean exclusiveExecution;

    private final String stepName;
    private final Runnable stepRunnable;
    private final LocalDateTime created = LocalDateTime.now();
    private final boolean throttlingAllowed;
    private final boolean makesHttpRequests;

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
        return 0; // TODO ?
    }

    public Duration getRetryBackoff() {
        return Duration.ZERO; // TODO ...
    }
}
