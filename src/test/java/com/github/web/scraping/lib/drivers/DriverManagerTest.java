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

import com.github.web.scraping.lib.drivers.lifecycle.QuitAfterIdleInterval;
import com.github.web.scraping.lib.drivers.lifecycle.RestartDriverAfterInterval;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import com.github.web.scraping.lib.AppConfig;

import java.util.ArrayList;
import java.util.List;

@Ignore
public class DriverManagerTest {

    @Test
    public void getDriver() {
    }

    @Test
    public void test_that_restartDriverIfNeeded_Restarts_The_Driver_Only_When_Strategy_Condition_Met() throws InterruptedException {

        // given
        SeleniumDriversFactory driversFactory = new SeleniumDriversFactory(AppConfig.getChromeDriverDir(),false);

        int restartInMillis = 2_000;

        SeleniumDriverManager driverManager = new SeleniumDriverManager(
                new RestartDriverAfterInterval(restartInMillis),
                new QuitAfterIdleInterval(0),
                driversFactory);

        WebDriver driver1 = driverManager.getDriver();
        Assert.assertNotNull(driver1);

        // when
        int decrem = 50;
        Thread.sleep(restartInMillis - decrem);
        boolean result = driverManager.restartDriverIfNeeded();

        // then
        Assert.assertFalse(result);

        WebDriver driver2 = driverManager.getDriver();
        Assert.assertNotNull(driver2);
        Assert.assertEquals(driver1, driver2);

        // when
        Thread.sleep(decrem + 10);

        result = driverManager.restartDriverIfNeeded();

        // then
        Assert.assertTrue(result);

        WebDriver driver3 = driverManager.getDriver();
        Assert.assertNotNull(driver3);
        Assert.assertNotEquals(driver2, driver3);

        driverManager.terminateDriver();
    }

    @Test
    public void test_that_quitWebDriverIfIdle_Restarts_The_Driver_Only_When_Strategy_Condition_Met() throws InterruptedException {

        // given
        SeleniumDriversFactory driversFactory = new SeleniumDriversFactory(AppConfig.getChromeDriverDir(),false);

        int maxIdleIntervalMillis = 2_000;

        SeleniumDriverManager driverManager = new SeleniumDriverManager(
                new RestartDriverAfterInterval(0),
                new QuitAfterIdleInterval(maxIdleIntervalMillis),
                driversFactory);

        WebDriver driver1 = driverManager.getDriver();
        Assert.assertNotNull(driver1);

        // when
        int decrem = 50;
        Thread.sleep(maxIdleIntervalMillis - decrem);
        boolean result = driverManager.quitDriverIfIdle();

        // then
        Assert.assertFalse(result);

        // when
        Thread.sleep(decrem + 10);

        result = driverManager.quitDriverIfIdle();

        // then
        Assert.assertTrue(result);

        WebDriver driver2 = driverManager.getDriver();
        Assert.assertNotNull(driver2);
        Assert.assertNotEquals(driver1, driver2);

        driverManager.terminateDriver();
    }

    @Test
    public void goToDefaultPage() {
    }

    @Test
    public void restartDriverImmediately() {
    }

    @Test
    public void test_how_memory_is_released_when_we_goto_default_page_after_loading_many_sites() throws InterruptedException {

        List<WebDriver> webDriverList = new ArrayList<>();

        SeleniumDriversFactory driversFactory = new SeleniumDriversFactory(AppConfig.getChromeDriverDir(),false);

        for (int i = 0; i < 5; i++) {
            WebDriver webDriver = driversFactory.startDriver();
            webDriver.get("https://www.sreality.cz/hledani/prodej/domy/praha?cena-od=0&cena-do=3000000&bez-aukce=1");
            webDriverList.add(webDriver);
//            webDriver.quit();
        }

        Thread.sleep(1_000);

        for (WebDriver webDriver : webDriverList) {
            webDriver.get("data:,");
        }

        Thread.sleep(15_000);

        for (WebDriver webDriver : webDriverList) {
            webDriver.quit();
        }

    }


}
