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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.SiteParserBase;
import com.github.web.scraping.lib.drivers.DriverManager;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.*;

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
    public void parse(String url) {
        if (parsingSequences == null) {
            throw new IllegalStateException("parsingSequence not set for SiteParser!");
        }
        for (HtmlUnitParsingStep<?> parsingSequence : parsingSequences) {
            StepsUtils.propagateServicesRecursively(parsingSequence, services, new HashSet<>());
        }
        loadPage(url, null).ifPresent(this::parsePageAndFilterDataResults);
    }

    @Override
    public void parseInternal(String url, ParsingContext<?, ?> ctx, List<HtmlUnitParsingStep<?>> parsingSequence, StepExecOrder currStepExecOrder) {
        loadPage(url, currStepExecOrder).ifPresent(page1 -> {
            ParsingContext<?, ?> nextCtx = ctx.toBuilder().setNode(page1).setPrevStepOrder(currStepExecOrder).build();
            executeNextSteps(nextCtx, parsingSequence);
        });
    }

    private Optional<HtmlPage> loadPage(String url, @Nullable StepExecOrder currStepExecOrder) {
        final WebClient webClient = driverManager.getDriver();
        return loadHtmlPage(url, webClient, currStepExecOrder);
    }

    private void parsePageAndFilterDataResults(HtmlPage page) {
        executeNextSteps(new ParsingContext<>(StepExecOrder.INITIAL, page), parsingSequences);
    }

    private void executeNextSteps(ParsingContext<?, ?> ctx, List<HtmlUnitParsingStep<?>> parsingSequences) {
        parsingSequences.forEach(s -> s.execute(ctx));
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
