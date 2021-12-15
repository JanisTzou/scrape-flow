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

package com.github.web.scraping.lib.drivers;

import com.gargoylesoftware.htmlunit.WebClient;

public class HtmlUnitDriverManager implements DriverManager<WebClient> {

    private volatile WebClient webClient = null;
    private final HtmlUnitDriversFactory driversFactory;


    public HtmlUnitDriverManager(HtmlUnitDriversFactory driversFactory) {
        this.driversFactory = driversFactory;
    }


    public WebClient getDriver() {
        if (webClient == null) {
            webClient = startNewDriver();
            return webClient;
        } else {
            return webClient;
        }
    }

    @Override
    public boolean restartDriverIfNeeded() {
        // not needed
        return false;
    }

    // for htmlunit we always ust quit
    @Override
    public boolean restartOrQuitDriverIfNeeded() {
        return terminateDriver();
    }

    @Override
    public boolean quitDriverIfIdle() {
        // not needed
        return false;
    }

    @Override
    public void goToDefaultPage() {
        // not needed
    }

    @Override
    public void restartDriverImmediately() {
        // not needed
    }

    private WebClient startNewDriver() {
        return driversFactory.startDriver();
    }


    @Override
    public boolean terminateDriver() {
        if (webClient != null) {
            webClient.close();
            webClient = null; // important
            return true;
        }
        return false;
    }

}
