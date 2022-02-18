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


import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class HtmlUnitDriversFactory implements DriversFactory<WebClient> {

    @Override
    public WebClient startDriver() {
        final WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setTimeout(15000);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.addRequestHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
        webClient.getOptions().setJavaScriptEnabled(false);
        return webClient;
    }

    @Override
    public int maxDrivers() {
        return Integer.MAX_VALUE;
    }
}
