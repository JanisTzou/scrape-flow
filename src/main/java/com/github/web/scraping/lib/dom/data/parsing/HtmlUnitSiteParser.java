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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("OptionalIsPresent")
public class HtmlUnitSiteParser extends SiteParser<WebClient> {

    private final List<HtmlUnitParsingStrategy> parsingStrategies;

    public HtmlUnitSiteParser(DriverManager<WebClient> driverManager,
                              List<HtmlUnitParsingStrategy> parsingStrategies) {
        super(driverManager);
        this.parsingStrategies = parsingStrategies;
    }

    @Override
    public List<ParsingResult> parse(String url) {
        final WebClient webClient = driverManager.getDriver();
        final Optional<HtmlPage> page = getHtmlPage(url, webClient);
        if (page.isPresent()) {
            return parsingStrategies.stream().flatMap(s -> s.parse(page.get()).stream()).collect(Collectors.toList());
        }
        return Collections.emptyList();
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

}
