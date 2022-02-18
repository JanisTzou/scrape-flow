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

package com.github.scrape.flow.drivers;

import com.github.scrape.flow.drivers.lifecycle.QuitAfterIdleInterval;
import com.github.scrape.flow.drivers.lifecycle.RestartDriverAfterInterval;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.selenium.StepAndDriversRelationshipTracker;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class SeleniumDriversManager implements DriversManager<WebDriver> {

    private final SeleniumDriversFactory seleniumDriversFactory;

    private final StepAndDriversRelationshipTracker stepAndDriversRelationshipTracker = new StepAndDriversRelationshipTracker();
    private final Map<Integer, DriverOperator<WebDriver>> driverOperators = new ConcurrentHashMap<>();

    @Override
    public Optional<DriverOperator<WebDriver>> getDriver(StepOrder stepOrder) {
        return stepAndDriversRelationshipTracker.getDriverNoFor(stepOrder)
                .map(driverOperators::get);
    }

    /**
     * @param stepOrder if the driver was reserved for this stepOrder successfully
     */
    @SuppressWarnings("OptionalIsPresent")
    public boolean reserveUnusedDriverFor(StepOrder stepOrder) {
        createNewDriverIfNotAtMaxLimit();
        Optional<Integer> unusedDriverNo = stepAndDriversRelationshipTracker.getUnusedDriverNo();
        if (unusedDriverNo.isPresent()) {
            stepAndDriversRelationshipTracker.track(stepOrder, unusedDriverNo.get());
            return true;
        }
        return false;
    }

    // TODO where should this happen? When next tasks get enqueued?
    public void reserveUsedDriverFor(StepOrder stepOrder) {
        stepAndDriversRelationshipTracker.trackByParent(stepOrder);
    }

    // TODO actualy this is more complicated ... any child steps might still use this driver ... and we need to track all of them ...

    public void unreserveUsedDriverBy(StepOrder stepOrder) {
        stepAndDriversRelationshipTracker.untrack(stepOrder);
    }



    private void createNewDriverIfNotAtMaxLimit() {
        if (driverOperators.size() < seleniumDriversFactory.maxDrivers()) {
            int nextDriverNo = driverOperators.size() + 1;
            SeleniumDriverOperator driverOperator = new SeleniumDriverOperator(
                    nextDriverNo,
                    new RestartDriverAfterInterval(Long.MAX_VALUE),
                    new QuitAfterIdleInterval(Long.MAX_VALUE),
                    seleniumDriversFactory
            );
            driverOperators.put(nextDriverNo, driverOperator);
            stepAndDriversRelationshipTracker.registerNewDriver(nextDriverNo);
        }
    }


}
