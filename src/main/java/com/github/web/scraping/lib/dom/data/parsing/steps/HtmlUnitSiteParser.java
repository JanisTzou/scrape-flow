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

package com.github.web.scraping.lib.dom.data.parsing.steps;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.web.scraping.lib.dom.data.parsing.*;
import com.github.web.scraping.lib.drivers.DriverManager;
import com.github.web.scraping.lib.parallelism.StepOrder;
import lombok.extern.log4j.Log4j2;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class HtmlUnitSiteParser extends SiteParserBase<WebClient> {

    // TODO iportant: check sequence for cyclic dependencies in steps
    private List<HtmlUnitParsingStep<?>> parsingSequences;

    public HtmlUnitSiteParser(DriverManager<WebClient> driverManager,
                              List<HtmlUnitParsingStep<?>> parsingSequences) {
        super(driverManager);
        this.parsingSequences = Objects.requireNonNullElse(parsingSequences, new ArrayList<>());
    }

    public HtmlUnitSiteParser(DriverManager<WebClient> driverManager) {
        this(driverManager, null);
    }

    public HtmlUnitSiteParser setParsingSequence(HtmlUnitParsingStep<?> parsingSequence) {
        this.parsingSequences = List.of(parsingSequence);
        return this;
    }

    @Override
    public List<ParsedData> parse(String url) {
        if (parsingSequences == null) {
            throw new IllegalStateException("parsingSequence not set for SiteParser!");
        }
        for (HtmlUnitParsingStep<?> parsingSequence : parsingSequences) {
            StepsUtils.propagateServicesRecursively(parsingSequence, services, new HashSet<>());
        }
        return loadPage(url)
//                .map(printPageToConsole())
                .map(this::parsePageAndFilterDataResults)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<StepResult> parseInternal(String url, ParsingContext<?, ?> ctx, List<HtmlUnitParsingStep<?>> parsingSequence) {
        return loadPage(url).stream()
//                .map(printPageToConsole())
                .flatMap(page1 -> {
                    ParsingContext<?, ?> nextCtx = ctx.toBuilder().setNode(page1).build();
                    return applyParsingStepsToPage(nextCtx, parsingSequence);
                })
                .collect(Collectors.toList());
    }

    private Optional<HtmlPage> loadPage(String url) {
        final WebClient webClient = driverManager.getDriver();
        return getHtmlPage(url, webClient);
    }

    private List<ParsedData> parsePageAndFilterDataResults(HtmlPage page) {
        Function<HtmlPage, List<ParsedData>> parsing = page1 -> applyParsingStepsToPage(new ParsingContext<>(StepOrder.INITIAL, page1), parsingSequences)
                .map(sr -> {
                    if (sr instanceof ParsedElement parsedElement) {
                        return new ParsedData(parsedElement.getModelProxy());
                    } else if (sr instanceof ParsedElements parsedElements) {
                        return new ParsedData(parsedElements.getContainer());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return parsing.apply(page);

    }

    private Stream<StepResult> applyParsingStepsToPage(ParsingContext<?, ?> ctx, List<HtmlUnitParsingStep<?>> parsingSequences) {
        return parsingSequences.stream()
                .flatMap(s -> s.execute(ctx, ExecutionMode.ASYNC).stream()); // TODO then switch to async ...
    }

    private Function<HtmlPage, HtmlPage> printPageToConsole() {
        return page -> {
            System.out.println(page.asXml());
            return page;
        };
    }

    private Optional<HtmlPage> getHtmlPage(String pageUrl, WebClient webClient) {
        try {
            log.debug("Loading page URL: {}", pageUrl);
            String winName = "window_name";
            URL url = new URL(pageUrl);
            webClient.openWindow(url, winName);
            log.debug("Loaded page URL: {}", pageUrl);
            return Optional.ofNullable((HtmlPage) webClient.getWebWindowByName(winName).getEnclosedPage());
        } catch (MalformedURLException e) {
            log.error("Error when getting htmlPage for URL: {}", pageUrl, e);
            return Optional.empty();
        }
    }

}
