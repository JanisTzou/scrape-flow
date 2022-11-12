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
import com.github.scrape.flow.scraping.ScrapingStep;
import com.github.scrape.flow.scraping.ScrapingStepInternalAccessor;
import lombok.Getter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class StepMetadata {

    @Getter
    private final ScrapingStep<?> step;

    // order in the step hierarchy/tree
    @Getter
    private final StepOrder stepHierarchyOrder;

    @Getter
    private final ClientType clientType;

    @Getter
    private final ClientReservationType clientReservationType;

    /**
     * Includes this step.
     */
    private final Map<ClientType, Integer> loadingStepCountUpToThisStep;

    public StepMetadata(ScrapingStep<?> step,
                        StepOrder stepHierarchyOrder,
                        ClientType clientType,
                        ClientReservationType clientReservationType,
                        Map<ClientType, Integer> loadingStepCountUpToThisStep) {
        this.step = step;
        this.stepHierarchyOrder = stepHierarchyOrder;
        this.clientType = clientType;
        this.clientReservationType = clientReservationType;
        this.loadingStepCountUpToThisStep = new HashMap<>(loadingStepCountUpToThisStep);
    }

    public int getLoadingStepCountUpToThisStep(ClientType clientType) {
        return this.loadingStepCountUpToThisStep.getOrDefault(clientType, 0);
    }

    public boolean isExclusiveExecution() {
        return ScrapingStepInternalAccessor.of(step).isExclusiveExecution();
    }

    public static Comparator<StepMetadata> getComparatorByLoadingStepCount(ClientType clientType) {
        return Comparator.comparingInt(sm -> sm.getLoadingStepCountUpToThisStep(clientType));
    }

}
