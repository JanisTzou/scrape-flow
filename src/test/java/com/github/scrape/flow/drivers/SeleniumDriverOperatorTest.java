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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

@Ignore
public class SeleniumDriverOperatorTest {

    public static final String CHROME_DRIVER_FILE = "/Users/janis/Projects_Data/scrape-flow/chromedriver";

    @Test
    public void getDriver() {
    }

    @Test
    public void test_that_restartDriverIfNeeded_Restarts_The_Driver_Only_When_Strategy_Condition_Met() throws InterruptedException {

        // given
        SeleniumDriversFactory driversFactory = new SeleniumDriversFactory(CHROME_DRIVER_FILE,false);

        int restartInMillis = 2_000;

        SeleniumDriverOperator operator = new SeleniumDriverOperator(
                1,
                new RestartDriverAfterInterval(restartInMillis),
                new QuitAfterIdleInterval(0),
                driversFactory
        );

        WebDriver driver1 = operator.getDriver();
        Assert.assertNotNull(driver1);

        // when
        int decrem = 50;
        Thread.sleep(restartInMillis - decrem);
        boolean result = operator.restartDriverIfNeeded();

        // then
        Assert.assertFalse(result);

        WebDriver driver2 = operator.getDriver();
        Assert.assertNotNull(driver2);
        Assert.assertEquals(driver1, driver2);

        // when
        Thread.sleep(decrem + 10);

        result = operator.restartDriverIfNeeded();

        // then
        Assert.assertTrue(result);

        WebDriver driver3 = operator.getDriver();
        Assert.assertNotNull(driver3);
        Assert.assertNotEquals(driver2, driver3);

        operator.terminateDriver();
    }

    @Test
    public void test_that_quitWebDriverIfIdle_Restarts_The_Driver_Only_When_Strategy_Condition_Met() throws InterruptedException {

        // given
        SeleniumDriversFactory driversFactory = new SeleniumDriversFactory(CHROME_DRIVER_FILE,false);

        int maxIdleIntervalMillis = 2_000;

        SeleniumDriverOperator operator = new SeleniumDriverOperator(
                1,
                new RestartDriverAfterInterval(0),
                new QuitAfterIdleInterval(maxIdleIntervalMillis),
                driversFactory
        );

        WebDriver driver1 = operator.getDriver();
        Assert.assertNotNull(driver1);

        // when
        int decrem = 50;
        Thread.sleep(maxIdleIntervalMillis - decrem);
        boolean result = operator.quitDriverIfIdle();

        // then
        Assert.assertFalse(result);

        // when
        Thread.sleep(decrem + 10);

        result = operator.quitDriverIfIdle();

        // then
        Assert.assertTrue(result);

        WebDriver driver2 = operator.getDriver();
        Assert.assertNotNull(driver2);
        Assert.assertNotEquals(driver1, driver2);

        operator.terminateDriver();
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

        SeleniumDriversFactory driversFactory = new SeleniumDriversFactory(CHROME_DRIVER_FILE,true);

        for (int i = 0; i < 1; i++) {
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
