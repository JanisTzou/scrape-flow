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

package com.github.web.scraping.lib.dom.data.parsing;

import com.github.web.scraping.lib.drivers.DriverManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class HtmlUnitSiteParser extends SiteParser<WebClient> {

    // TODO should the strategies contain info about how to group the parsed output?
    private final List<HtmlUnitParsingStrategy> parsingStrategies;

    public HtmlUnitSiteParser(DriverManager<WebClient> driverManager, List<HtmlUnitParsingStrategy> parsingStrategies) {
        super(driverManager);
        this.parsingStrategies = parsingStrategies;
    }

    public static Builder builder(DriverManager<WebClient> driverManager) {
        return new Builder(driverManager);
    }

    @Override
    public List<ParsedElement> parse(String url) {
        final WebClient webClient = driverManager.getDriver();
        final Optional<HtmlPage> page = getHtmlPage(url, webClient);
//        System.out.println(page.get().asXml());
        return page.map(this::parsePage).orElse(Collections.emptyList());
    }

    private List<ParsedElement> parsePage(HtmlPage page) {
        return parsingStrategies.stream().flatMap(s -> s.parse(page).stream()).collect(Collectors.toList());
    }

    private Optional<HtmlPage> getHtmlPage(String inzeratUrl, WebClient webClient) {
        try {
            String winName = "window_name";
            URL url = new URL(inzeratUrl);
            webClient.openWindow(url, winName);
            return Optional.ofNullable((HtmlPage) webClient.getWebWindowByName(winName).getEnclosedPage());
        } catch (MalformedURLException e) {
            // TODO log something ...
            return Optional.empty();
        }
    }

    public static class Builder {

        // TODO somehow we wanna get the driverManager reference here from the outside ...
        private DriverManager<WebClient> driverManager;
        private final List<HtmlUnitParsingStrategy> parsingStrategies = new ArrayList<>();

        public Builder(DriverManager<WebClient> driverManager) {
            this.driverManager = driverManager;
        }

        public Builder addStrategy(HtmlUnitParsingStrategy strategy) {
            this.parsingStrategies.add(strategy);
            return this;
        }

        public Builder addStrategies(HtmlUnitParsingStrategy ... strategies) {
            this.parsingStrategies.addAll(Arrays.asList(strategies));
            return this;
        }

        public HtmlUnitSiteParser build() {
            return new HtmlUnitSiteParser(this.driverManager, parsingStrategies);
        }

    }


}
