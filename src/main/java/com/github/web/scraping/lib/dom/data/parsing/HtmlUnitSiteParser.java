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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.web.scraping.lib.dom.data.parsing.steps.HtmlUnitParsingStep;
import com.github.web.scraping.lib.drivers.DriverManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;


public class HtmlUnitSiteParser extends SiteParser<WebClient> {

    // TODO should the strategies contain info about how to group the parsed output?
    // TODO parsing sequence vs parsing step(s)
    private final List<HtmlUnitParsingStep> parsingSequences;
    private final HtmlUnitParsingStep paginatingSequence;


    public HtmlUnitSiteParser(DriverManager<WebClient> driverManager,
                              List<HtmlUnitParsingStep> parsingSequences,
                              HtmlUnitParsingStep paginatingSequence) {
        super(driverManager);
        this.parsingSequences = parsingSequences;
        this.paginatingSequence = paginatingSequence;
    }

    public static Builder builder(DriverManager<WebClient> driverManager) {
        return new Builder(driverManager);
    }

    @Override
    public List<ParsedData> parse(String url) {
        final WebClient webClient = driverManager.getDriver();
        final Optional<HtmlPage> page = getHtmlPage(url, webClient);
//        System.out.println(page.get().asXml());
        return page.map(this::parsePage)
                .orElse(Collections.emptyList());
    }

    private List<ParsedData> parsePage(HtmlPage page) {
        Function<HtmlPage, List<ParsedData>> parsing = page1 -> parsingSequences.stream()
                .flatMap(s -> s.execute(new ParsingContext(page1)).stream())
                .map(sr -> {
                    if (sr instanceof ParsedElement parsedElement) {
                        // TODO handle parsed HRef .... references ...
                        return new ParsedData(parsedElement.getModel(), Collections.emptyList());
                    } else if (sr instanceof ParsedElements parsedElements) {
                        return new ParsedData(parsedElements.getContainer(), Collections.emptyList());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (paginatingSequence == null) {
            return parsing.apply(page);
        } else {
            // Hmm ... we need something like do ... while ...
            List<ParsedData> result = new ArrayList<>();
            AtomicReference<HtmlPage> pageRef = new AtomicReference<>(page);
            while (true) {
                result.addAll(parsing.apply(pageRef.get()));
                List<StepResult> paginationResult = paginatingSequence.execute(new ParsingContext(pageRef.get()));

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
        private HtmlUnitParsingStep paginatingStep;
        // TODO somehow we wanna get the driverManager reference here from the outside ...
        private DriverManager<WebClient> driverManager;

        public Builder(DriverManager<WebClient> driverManager) {
            this.driverManager = driverManager;
        }

        public Builder addParsingSequence(HtmlUnitParsingStep parsingStep) {
            this.parsingSteps.add(parsingStep);
            return this;
        }

        public Builder setPaginatingSequence(HtmlUnitParsingStep paginatingStep) {
            this.paginatingStep = paginatingStep;
            return this;
        }

        public HtmlUnitSiteParser build() {
            return new HtmlUnitSiteParser(this.driverManager, parsingSteps, paginatingStep);
        }

    }


}
