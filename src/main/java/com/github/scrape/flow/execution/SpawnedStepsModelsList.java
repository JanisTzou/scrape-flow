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

import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

class SpawnedStepsModelsList {

    private final List<SpawnedStepsModels> list = new CopyOnWriteArrayList<>(); // must be thread safe

    Optional<SpawnedStepsModels> getSpawnedContaining(StepOrder order) {
        return list.stream().filter(s -> s.getSteps().contains(order)).findFirst();
    }

    void remove(SpawnedStepsModels spawnedStepsModels) {
        Optional<SpawnedStepsModels> found = list.stream().filter(s -> s.containsSameSteps(spawnedStepsModels)).findFirst();
        found.ifPresent(list::remove);
    }

    public void add(SpawnedStepsModels spawnedStepsModels) {
        this.list.add(spawnedStepsModels);
    }

    boolean isEmpty() {
        return list.isEmpty();
    }
}
