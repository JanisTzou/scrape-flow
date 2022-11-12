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

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

// not thread safe! Client code's responsibility ...
@Log4j2
public class ExclusiveExecutionHandler {

    private final ActiveStepsTracker activeStepsTracker;
    @Setter
    private volatile StepHierarchyRepository stepHierarchyRepository;

    public ExclusiveExecutionHandler(ActiveStepsTracker activeStepsTracker) {
        this.activeStepsTracker = activeStepsTracker;
    }

    public boolean canExecute(QueuedTask qst) {
        Task task = qst.getTask();
        StepOrder hierarchyOrder = task.getStepHierarchyOrder();
        StepOrder order = task.getStepOrder();

        return !shouldWaitForExclusiveSteps(order, hierarchyOrder);
    }

    boolean shouldWaitForExclusiveSteps(StepOrder order, StepOrder hierarchyOrder) {
        Optional<StepOrder> precedingHierarchySibling = hierarchyOrder.getPrecedingSibling();
        if (precedingHierarchySibling.isPresent()) {
            boolean exclusiveExecution = stepHierarchyRepository.getMetadataFor(precedingHierarchySibling.get()).isExclusiveExecution();
            if (exclusiveExecution) {
                Optional<StepOrder> precedingSibling = order.getPrecedingSibling();
                if (precedingSibling.isPresent()) {
                    boolean wait = activeStepsTracker.isPartOfActiveStepSequence(precedingSibling.get());
                    if (wait) {
                        log.debug("Cannot execute step {} - waiting for preceding exclusive steps to finish", order);
                    }
                    return wait;
                }
            }
        }
        return false;
    }

}
