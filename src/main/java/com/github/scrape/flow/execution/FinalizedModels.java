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

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Comparator;
import java.util.Objects;

/**
 * Holds the data for which all parsing tasks have been finished and should contained final parsed information
 */
@Data
public class FinalizedModels {

    public static Comparator<FinalizedModels> NATURAL_COMPARATOR = (fm1, fm2) -> {
        // first step is enough for comparison
        StepOrder so1 = fm1.models.getSteps().stream().min(StepOrder.NATURAL_COMPARATOR).get(); // must be present
        StepOrder so2 = fm2.models.getSteps().stream().min(StepOrder.NATURAL_COMPARATOR).get();
        return StepOrder.NATURAL_COMPARATOR.compare(so1, so2);
    };

    private final SpawnedStepsModels models;

}
