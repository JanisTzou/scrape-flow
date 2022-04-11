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
import com.github.scrape.flow.scraping.ScrapingStep;
import lombok.Data;

import java.util.Comparator;

@Data
public class StepMetadata {

    public static Comparator<StepMetadata> COMPARATOR_BY_LOADING_STEP_COUNT = Comparator.comparingInt(sm -> sm.loadingStepCountUpToThisStep);

    private final ScrapingStep<?> step;
    // order in the step hierarchy/tree
    private final StepOrder stepHierarchyOrder;
    private final ClientReservationType clientReservationType;

    // includes this step
    private final int loadingStepCountUpToThisStep;


}
