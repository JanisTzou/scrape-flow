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

package com.github.scrape.flow.clients;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Log4j2
public class SeleniumClientFactory implements ClientFactory<WebDriver> {

    private static final String DRIVER_NAME_CHROME = "webdriver.chrome.driver";

    private final String chromeDriverDir;
    private final boolean isHeadless;

    @Override
    public WebDriver startDriver() {
        System.setProperty(DRIVER_NAME_CHROME, chromeDriverDir);
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(isHeadless);
        options.addArguments("--disable-notifications");
        WebDriver driverObj = new ChromeDriver(options);
        driverObj.manage().timeouts().implicitlyWait(10000, TimeUnit.MICROSECONDS);
        log.debug("Started new client: {}", driverObj);
        return driverObj;
    }

    @Override
    public int maxClients() {
        return 2; // TODO make user defined! ...
    }
}
