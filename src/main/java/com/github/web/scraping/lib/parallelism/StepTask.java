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

import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
@Getter
public class StepTask implements Comparable<StepTask> {

    public static Comparator<StepTask> NATURAL_COMPARATOR = (st1, st2) -> {
        return StepOrder.NATURAL_COMPARATOR.compare(st1.stepOrder, st2.stepOrder);
    };

    private final StepOrder stepOrder;
    private final String stepName;
    private final Callable<List<StepResult>> callableStep;
    private final LocalDateTime created = LocalDateTime.now();
    private final boolean throttlable;

    @Override
    public int compareTo(StepTask o) {
        return NATURAL_COMPARATOR.compare(this, o);
    }

    @Override
    public String toString() {
        return "StepTask{" +
                "stepOrder=" + stepOrder +
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
