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


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.concurrent.TimeUnit;

public class SeleniumDriversFactory implements DriversFactory<WebDriver> {

    private static final String DRIVER_NAME_CHROME = "webdriver.chrome.driver";

    private final String chromeDriverDir;
    private final boolean isHeadless;

    public SeleniumDriversFactory(String chromeDriverDir, boolean isHeadless) {
        this.chromeDriverDir = chromeDriverDir;
        this.isHeadless = isHeadless;
    }

    @Override
    public WebDriver startDriver() {
        System.setProperty(DRIVER_NAME_CHROME, chromeDriverDir);
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(isHeadless);
        options.addArguments("--disable-notifications");
        WebDriver driverObj = new ChromeDriver(options);

        driverObj.manage().timeouts().implicitlyWait(10000, TimeUnit.MICROSECONDS);
        return driverObj;
    }

}
