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

import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.scraping.ClientType;
import lombok.Data;


@Data
public class TaskDefinition {

    private final StepOrder stepHierarchyOrder;
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
    private final ClientType clientType;
    private final ClientReservationType clientReservationType;

}
