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

import com.gargoylesoftware.htmlunit.WebClient;
import com.github.scrape.flow.scraping.ClientType;

public class HtmlUnitClientOperator implements ClientOperator<WebClient> {

    private final int clientNo;
    private final WebClient webClient;
    private volatile boolean reserved = false;

    public HtmlUnitClientOperator(int clientNo, HtmlUnitClientFactory driversFactory) {
        this.clientNo = clientNo;
        this.webClient = driversFactory.startDriver();
    }

    @Override
    public int getClientNo() {
        return clientNo;
    }

    @Override
    public ClientId getClientId() {
        return new ClientId(ClientType.HTMLUNIT, getClientNo());
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

    public WebClient getClient() {
        return webClient;
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

    @Override
    public boolean terminateDriver() {
        webClient.close();
        return true;
    }

}
