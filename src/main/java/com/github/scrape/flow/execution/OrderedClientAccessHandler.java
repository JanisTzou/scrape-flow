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

import com.github.scrape.flow.scraping.ClientType;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OrderedClientAccessHandler {

    private final ActiveStepsTracker activeStepsTracker;
    @Setter
    private volatile StepHierarchyRepository stepHierarchyRepository;

    public boolean enoughFreeClientsForPrecedingSteps(int remainingClients, StepOrder stepExecToCheck, ClientType clientType) {
        checkInitialisation();
        // ideally we should also filter away steps that are executing now, but it's ok ...
        // ... the wort that can happen is that a client will not seem to be available for a while
        SortedMap<StepOrder, ActiveStepsTracker.TrackedStepOrder> precedingSteps = activeStepsTracker.getPrecedingSteps(stepExecToCheck);
        List<StepOrder> precedingStepsWithoutActiveParents = getStepsWithoutActiveParents(precedingSteps.values());
        int remainingDepth = stepHierarchyRepository.getRemainingLoadingStepsDepthMax(precedingStepsWithoutActiveParents, clientType);
        return remainingClients > remainingDepth;
    }

    // we want "top-most" steps only

    private List<StepOrder> getStepsWithoutActiveParents(Collection<ActiveStepsTracker.TrackedStepOrder> precedingSteps) {
        return precedingSteps.stream()
                .filter(this::isTopMost)
                .map(ActiveStepsTracker.TrackedStepOrder::getHierarchyOrder)
                .collect(Collectors.toList());
    }

    private boolean isTopMost(ActiveStepsTracker.TrackedStepOrder s) {
        Optional<StepOrder> parent = s.getStepOrder().getParent();
        if (parent.isPresent()) {
            if (activeStepsTracker.isActive(parent.get())) {
                return false; // if parent is active as well, do not include this step
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private void checkInitialisation() {
        if (stepHierarchyRepository == null) {
            throw new IllegalStateException("stepHierarchyRepository has not been initialised!");
        }
    }

}
