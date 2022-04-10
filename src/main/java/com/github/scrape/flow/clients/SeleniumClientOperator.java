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

import com.github.scrape.flow.clients.lifecycle.DriverQuitStrategy;
import com.github.scrape.flow.clients.lifecycle.DriverRestartStrategy;
import com.github.scrape.flow.scraping.ClientType;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;


/**
 * NOTE: An instance should only be owned by one actor at a time so it can not happen that
 * some other actor quits the driver when other is in the middle of scraping
 */
@Log4j2
public class SeleniumClientOperator implements ClientOperator<WebDriver> {

    public static final String DEFAULT_PAGE_URL = "data:,";
    private final int clientNo;
    private final DriverRestartStrategy restartStrategy;
    private final DriverQuitStrategy quitStrategy;
    private final SeleniumClientFactory driversFactory;
    private volatile WebDriver driver;
    private volatile boolean reserved = false;
    private volatile long lastUsedTs;
    private volatile long lastRestartTs;

    public SeleniumClientOperator(int clientNo,
                                  DriverRestartStrategy restartStrategy,
                                  DriverQuitStrategy driverQuitStrategy,
                                  SeleniumClientFactory driversFactory) {
        this.clientNo = clientNo;
        this.restartStrategy = restartStrategy;
        this.quitStrategy = driverQuitStrategy;
        this.driversFactory = driversFactory;
        this.lastUsedTs = System.currentTimeMillis();
        this.lastRestartTs = System.currentTimeMillis();
    }


    public WebDriver getClient() {
        lastUsedTs = System.currentTimeMillis();
        if (driver == null) {
            this.driver = startNewDriver();
            return driver;
        } else {
            return driver;
        }
    }

    @Override
    public int getClientNo() {
        return clientNo;
    }

    @Override
    public boolean isReserved() {
        return this.reserved;
    }

    @Override
    public void reserve() {
        this.reserved = true;
    }

    @Override
    public void unReserve() {
        this.reserved = false;
    }

    @Override
    public ClientId getClientId() {
        return new ClientId(ClientType.SELENIUM, getClientNo());
    }


    @Override
    public boolean restartDriverIfNeeded() {
        if (restartStrategy.shouldRestart(lastRestartTs)) {
            restartDriver();
            return true;
        }
        return false;
    }

    @Override
    public boolean quitDriverIfIdle() {
        if (quitStrategy.shouldQuit(lastUsedTs)) {
            return terminateDriver();
        }
        return false;
    }

    // for selenium we always prefer to restart ...
    @Override
    public boolean restartOrQuitDriverIfNeeded() {
        return restartDriverIfNeeded();
    }

    @Override
    public void goToDefaultPage() {
        driver.get(DEFAULT_PAGE_URL);
    }


    @Override
    public void restartDriverImmediately() {
        restartDriver();
    }

    private WebDriver startNewDriver() {

        int count = 100;
        int sleepMillis = 3000;
        WebDriver webDriver = null;

        for (int attemptNo = 1; attemptNo <= count; attemptNo++) {
            try {
                webDriver = driversFactory.startDriver();
                this.lastRestartTs = System.currentTimeMillis();
                return webDriver;
            } catch (Exception e) {
                log.error("Error while starting SeleniumDriver ... attempt no.: {}", attemptNo, e);
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                if (count == attemptNo) {
                    throw e;
                }
            }
        }
        return webDriver;
    }

    private void restartDriver() {
        if (driver != null) {
            terminateDriver();
        }
        driver = startNewDriver();
    }

    @Override
    public boolean terminateDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                log.error("Error quitting selenium driver.");
            }
            this.driver = null;
            return true;
        }
        return false;
    }


}
