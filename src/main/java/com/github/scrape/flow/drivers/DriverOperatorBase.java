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

public class DriverOperatorBase<T> implements DriverContainer {

    protected final DriverManager<T> driverManager;

    protected DriverOperatorBase(DriverManager<T> driverManager) {
        this.driverManager = driverManager;
    }


    @Override
    public boolean terminateDriver() {
        return driverManager.terminateDriver();
    }

    @Override
    public boolean quitDriverIfIdle() {
        return driverManager.quitDriverIfIdle();
    }

    @Override
    public void restartDriverImmediately() {
        driverManager.restartDriverImmediately();
    }

    @Override
    public boolean restartDriverIfNeeded() {
        return driverManager.restartDriverIfNeeded();
    }

    @Override
    public boolean restartOrQuitDriverIfNeeded() {
        return driverManager.restartOrQuitDriverIfNeeded();
    }

    @Override
    public void goToDefaultPage() {
        driverManager.goToDefaultPage();
    }
}
