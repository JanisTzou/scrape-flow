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

package com.github.scrape.flow.scraping.selenium;

import com.github.scrape.flow.execution.StepOrder;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of all the steps and the drivers that those steps depend on
 */
@Log4j2
public class StepAndDriversRelationshipTracker {

    private final Map<Integer, Set<StepOrder>> stepsByUsedDriverNo = new ConcurrentHashMap<>();
    private final Map<StepOrder, Integer> driverNoByUsingSteps = new ConcurrentHashMap<>();

    public void registerNewDriver(int driverNo) {
        if (stepsByUsedDriverNo.containsKey(driverNo)) {
            log.error("Driver no {} is already registered!", driverNo);
        } else {
            stepsByUsedDriverNo.put(driverNo, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        }
    }

    /**
     * @param childStepOrder step that depends on the webdriver of the parent
     *                       and as a result should be registered with the same driver no. as the parent
     */
    public void trackByParent(StepOrder childStepOrder) {
        childStepOrder.getParent().ifPresent(parent -> {
            Integer parentDriverNo = driverNoByUsingSteps.get(parent);
            if (parentDriverNo != null) {
                stepsByUsedDriverNo.get(parentDriverNo).add(childStepOrder);
                driverNoByUsingSteps.put(childStepOrder, parentDriverNo);
            }
        });
    }

    /**
     * @param usedDriverNo must be already registered!
     */
    public void track(StepOrder stepOrder, int usedDriverNo) {
        stepsByUsedDriverNo.get(usedDriverNo).add(stepOrder);
        driverNoByUsingSteps.put(stepOrder, usedDriverNo);
    }


    public void untrack(StepOrder stepOrder) {
        Integer driverNo = driverNoByUsingSteps.remove(stepOrder);
        if (driverNo == null) {
            log.error("No webDriverId associated with the specified stepOrder {} - cannot untrack it!", stepOrder);
        } else {
            Set<StepOrder> stepOrdersForDriver = stepsByUsedDriverNo.get(driverNo);
            if (stepOrdersForDriver == null) {
                log.error("No stepOrders found for webDriverId {}! Cannot untrack stepOrder {}", driverNo, stepOrder);
            } else {
                stepOrdersForDriver.remove(stepOrder);
            }
        }
    }

    public Optional<Integer> getDriverNoFor(StepOrder stepOrder) {
        return Optional.ofNullable(this.driverNoByUsingSteps.get(stepOrder));
    }

    public Optional<Integer> getUnusedDriverNo() {
        return stepsByUsedDriverNo.entrySet().stream().filter(e -> e.getValue().isEmpty()).findFirst().map(Map.Entry::getKey);
    }

    public boolean isDriverUsed(int driverNo) {
        return !this.stepsByUsedDriverNo.getOrDefault(driverNo, Collections.emptySet()).isEmpty();
    }

}
