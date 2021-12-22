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

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.web.scraping.lib.dom.data.parsing.*;
import com.github.web.scraping.lib.drivers.DriverManager;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
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
        return loadPage(url, null)
                .map(this::parsePageAndFilterDataResults)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<StepResult> parseInternal(String url, ParsingContext<?, ?> ctx, List<HtmlUnitParsingStep<?>> parsingSequence, StepExecOrder currStepExecOrder) {
        return loadPage(url, currStepExecOrder).stream()
                .flatMap(page1 -> {
                    ParsingContext<?, ?> nextCtx = ctx.toBuilder().setNode(page1).setPrevStepOrder(currStepExecOrder).build();
                    return executeNextSteps(nextCtx, parsingSequence);
                })
                .collect(Collectors.toList());
    }

    private Optional<HtmlPage> loadPage(String url, @Nullable StepExecOrder currStepExecOrder) {
        final WebClient webClient = driverManager.getDriver();
        return loadHtmlPage(url, webClient, currStepExecOrder);
    }

    private List<ParsedData> parsePageAndFilterDataResults(HtmlPage page) {
        Function<HtmlPage, List<ParsedData>> parsing = page1 -> executeNextSteps(new ParsingContext<>(StepExecOrder.INITIAL, page1), parsingSequences)
                .map(sr -> {
                    if (sr instanceof ParsedElement parsedElement) {
                        return new ParsedData(parsedElement.getModelWrapper());
                    } else if (sr instanceof ParsedElements parsedElements) {
                        return new ParsedData(parsedElements.getContainer());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return parsing.apply(page);

    }

    private Stream<StepResult> executeNextSteps(ParsingContext<?, ?> ctx, List<HtmlUnitParsingStep<?>> parsingSequences) {
        return parsingSequences.stream()
                .flatMap(s -> s.execute(ctx, ExecutionMode.ASYNC, o -> { // TODO what to do about this ??? this needs to be fixed ... we need to collect steps and then track them (data) ... if there are any models generated ... (see inside Wrapper ... possibly reuse)
                }).stream());
    }

    private Optional<HtmlPage> loadHtmlPage(String pageUrl, WebClient webClient, @Nullable StepExecOrder currStepExecOrder) {
        String logInfo = currStepExecOrder != null ? currStepExecOrder + " - " : "";
        try {
            log.debug("{}Loading page URL: {}", logInfo, pageUrl);
            URL url = new URL(pageUrl);
            Page page = webClient.getPage(url);
            WebResponse resp = page.getWebResponse();
            int statusCode = resp.getStatusCode();
            if (statusCode >= 400) {
                // TODO think about how to handle this and if we should le the clients to define desired behaviour (for retry logic to work etc ...)
                log.warn("{}Returned status {} - could not load page! Response time {}ms", logInfo, statusCode, resp.getLoadTime());
                return Optional.empty();
            } else {
                if (page.isHtmlPage()) {
                    log.info("{}Loaded page in {}ms at URL: {}", logInfo, resp.getLoadTime(), pageUrl);
                    HtmlPage htmlPage = (HtmlPage) page;
//                    printPageToConsole(htmlPage);
                    return Optional.of(htmlPage);
                }
                log.warn("{}Cannot process non-HTML page!", logInfo);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("{}Error when getting htmlPage for URL: {}", logInfo, pageUrl, e);
            return Optional.empty();
        }
    }

    private void printPageToConsole(HtmlPage page) {
        System.out.println(page.asXml());
    }

}
