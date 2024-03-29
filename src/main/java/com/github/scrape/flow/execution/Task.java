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

import com.github.scrape.flow.clients.ClientReservationRequest;
import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.scraping.ClientType;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;

@Data
public class Task implements Comparable<Task> {

    public static Comparator<Task> NATURAL_COMPARATOR = (st1, st2) -> {
        return StepOrder.NATURAL_COMPARATOR.compare(st1.stepOrder, st2.stepOrder);
    };

    private final StepOrder stepOrder;
    private final StepOrder stepHierarchyOrder;

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
    private final ClientType clientType;
    private final ClientReservationType clientReservationType;

    private final int maxRetries;
    private final Duration retryBackoff;

    private final LocalDateTime created = LocalDateTime.now();

    public static Task from(TaskDefinition basis, int retries, Duration retryBackoff) {
        return new Task(
                basis.getStepOrder(),
                basis.getStepHierarchyOrder(),
                basis.isExclusiveExecution(),
                basis.getStepName(),
                basis.getStepRunnable(),
                basis.isThrottlingAllowed(),
                basis.isMakingHttpRequests(),
                basis.getClientType(),
                basis.getClientReservationType(),
                retries,
                retryBackoff
        );
    }

    public ClientReservationRequest getClientReservationRequest() {
        return new ClientReservationRequest(stepOrder, clientReservationType, clientType);
    }

    @Override
    public int compareTo(Task o) {
        return NATURAL_COMPARATOR.compare(this, o);
    }

    public String loggingInfo() {
        return "StepTask{" +
                "stepOrder=" + stepOrder +
                ", stepHierarchyOrder=" + stepHierarchyOrder +
                ", exclusiveExecution=" + exclusiveExecution +
                ", stepName='" + stepName + '\'' +
                ", created=" + created +
                '}';
    }

}
