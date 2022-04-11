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

package com.github.scrape.flow.scraping;

import com.github.scrape.flow.execution.StepOrder;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Data
public class SpawnedSteps {

    private final StepOrder parent;
    private final List<StepOrder> steps;

    public SpawnedSteps(StepOrder parent, List<StepOrder> steps) {
        this.parent = parent;
        this.steps = new CopyOnWriteArrayList<>(steps);
    }

    public SpawnedSteps(StepOrder parent, StepOrder spawnedStep) {
        this(parent, List.of(spawnedStep));
    }

    public boolean containsSameSteps(SpawnedSteps other) {
        if (this.steps.size() != other.steps.size()) {
            return false;
        }
        List<StepOrder> steps1 = this.steps.stream().sorted(StepOrder.NATURAL_COMPARATOR).collect(Collectors.toList());
        List<StepOrder> steps2 = other.steps.stream().sorted(StepOrder.NATURAL_COMPARATOR).collect(Collectors.toList());

        return steps1.equals(steps2);
    }
}
