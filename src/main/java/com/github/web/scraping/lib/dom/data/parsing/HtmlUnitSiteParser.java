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

import com.github.web.scraping.lib.dom.data.parsing.steps.HtmlUnitParsingStep;
import com.github.web.scraping.lib.drivers.DriverManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;


public class HtmlUnitSiteParser extends SiteParser<WebClient> {

    // TODO should the strategies contain info about how to group the parsed output?
    // TODO parsing sequence vs parsing step(s)
    private final List<HtmlUnitParsingStep> parsingSteps;
    private final List<HtmlUnitParsingStep> paginatingSteps;


    public HtmlUnitSiteParser(DriverManager<WebClient> driverManager,
                              List<HtmlUnitParsingStep> parsingSteps,
                              List<HtmlUnitParsingStep> paginatingSteps) {
        super(driverManager);
        this.parsingSteps = parsingSteps;
        this.paginatingSteps = paginatingSteps;
    }

    public static Builder builder(DriverManager<WebClient> driverManager) {
        return new Builder(driverManager);
    }

    @Override
    public List<ParsedElement> parse(String url) {
        final WebClient webClient = driverManager.getDriver();
        final Optional<HtmlPage> page = getHtmlPage(url, webClient);
//        System.out.println(page.get().asXml());
        return page.map(this::parsePage)
                .orElse(Collections.emptyList());
    }

    private List<ParsedElement> parsePage(HtmlPage page) {

        Function<HtmlPage, List<ParsedElement>> parsing = page1 -> parsingSteps.stream()
                .flatMap(s -> s.execute(page1).stream())
                .map(psr -> {
                    if (psr instanceof ParsedElement parsedElement) {
                        return parsedElement;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (paginatingSteps.isEmpty()) {
            return parsing.apply(page);
        } else {
            // Hmm ... we need something like do ... while ...
            List<ParsedElement> result = new ArrayList<>();
            AtomicReference<HtmlPage> pageRef = new AtomicReference<>(page);
            while (true) {
                // TODO hmm this page will probably always have next button ... the click operation has not destroyed this instance ...
                result.addAll(parsing.apply(pageRef.get()));
                List<StepResult> paginationResult = paginatingSteps.stream()
                        .flatMap(s -> s.execute(pageRef.get()).stream())
                        .collect(Collectors.toList());

                Optional<HtmlPage> nextPage = paginationResult.stream().filter(sr -> sr instanceof ElementClicked).map(sr -> ((ElementClicked) sr).getPageAfterElementClicked()).findFirst();
                if (nextPage.isPresent()) {
                    pageRef.set(nextPage.get());
                } else {
                    break;
                }
            }
            return result;
        }

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

        private final List<HtmlUnitParsingStep> parsingSteps = new ArrayList<>();
        private final List<HtmlUnitParsingStep> paginatingSteps = new ArrayList<>();
        // TODO somehow we wanna get the driverManager reference here from the outside ...
        private DriverManager<WebClient> driverManager;

        public Builder(DriverManager<WebClient> driverManager) {
            this.driverManager = driverManager;
        }

        public Builder addParsingSequence(HtmlUnitParsingStep parsingStep) {
            this.parsingSteps.add(parsingStep);
            return this;
        }

        public Builder addPaginatingSequence(HtmlUnitParsingStep paginatingStep) {
            this.paginatingSteps.add(paginatingStep);
            return this;
        }

        public HtmlUnitSiteParser build() {
            return new HtmlUnitSiteParser(this.driverManager, parsingSteps, paginatingSteps);
        }

    }


}
