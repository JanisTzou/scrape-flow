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
import lombok.extern.log4j.Log4j2;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class HtmlUnitSiteParser extends SiteParserBase<WebClient> {

    private final List<HtmlUnitParsingStep<?>> parsingSequences;

    public HtmlUnitSiteParser(DriverManager<WebClient> driverManager,
                              List<HtmlUnitParsingStep<?>> parsingSequences) {
        super(driverManager);
        this.parsingSequences = Objects.requireNonNullElse(parsingSequences, new ArrayList<>());
    }

    public static Builder builder(DriverManager<WebClient> driverManager) {
        return new Builder(driverManager);
    }

    @Override
    public List<ParsedData> parse(String url) {
        if (parsingSequences == null) {
            throw new IllegalStateException("parsingSequence not set for SiteParser!");
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
        Function<HtmlPage, List<ParsedData>> parsing = page1 -> applyParsingStepsToPage(new ParsingContext<>(page1), parsingSequences)
                .map(sr -> {
                    if (sr instanceof ParsedElement parsedElement) {
                        // TODO handle parsed HRef .... references ...
                        return new ParsedData(parsedElement.getModelProxy(), Collections.emptyList());
                    } else if (sr instanceof ParsedElements parsedElements) {
                        return new ParsedData(parsedElements.getContainer(), Collections.emptyList());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return parsing.apply(page);

    }

    private Stream<StepResult> applyParsingStepsToPage(ParsingContext<?, ?> ctx, List<HtmlUnitParsingStep<?>> parsingSequences) {
        return parsingSequences.stream().flatMap(s -> s.execute(ctx).stream());
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

    public static class Builder {

        private HtmlUnitParsingStep<?> parsingSequence;
        // TODO somehow we wanna get the driverManager reference here from the outside ...
        private final DriverManager<WebClient> driverManager;

        public Builder(DriverManager<WebClient> driverManager) {
            this.driverManager = driverManager;
        }

        // TODO maybe there should only be one parsing sequence? ... no need for more ... probably
        public Builder setParsingSequence(HtmlUnitParsingStep<?> seq) {
            this.parsingSequence = seq;
            return this;
        }

        public HtmlUnitSiteParser build() {
            List<HtmlUnitParsingStep<?>> seqs = parsingSequence != null ? List.of(parsingSequence) : null;
            return new HtmlUnitSiteParser(this.driverManager, seqs);
        }

    }


}
