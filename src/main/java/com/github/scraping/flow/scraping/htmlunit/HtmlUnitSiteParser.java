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

package com.github.scraping.flow.scraping.htmlunit;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.scraping.flow.drivers.DriverManager;
import com.github.scraping.flow.parallelism.StepExecOrder;
import com.github.scraping.flow.scraping.RequestException;
import com.github.scraping.flow.scraping.SiteParserBase;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Log4j2
public class HtmlUnitSiteParser extends SiteParserBase<WebClient> {

    public HtmlUnitSiteParser(DriverManager<WebClient> driverManager) {
        super(driverManager);
    }

    @Override
    public void parse(String url, HtmlUnitScrapingStep<?> parsingSequence) {
        if (parsingSequence == null) {
            throw new IllegalStateException("parsingSequence not set for SiteParser!");
        }
        loadPage(url, null).ifPresent(page -> parsePageAndFilterDataResults(page, List.of(parsingSequence)));
    }

    @Override
    public void parse(String url, ScrapingContext ctx, List<HtmlUnitScrapingStep<?>> parsingSequences, StepExecOrder currStepExecOrder) {
        loadPage(url, currStepExecOrder).ifPresent(page1 -> {
            ScrapingContext nextCtx = ctx.toBuilder().setNode(page1).setPrevStepOrder(currStepExecOrder).build();
            executeNextSteps(nextCtx, parsingSequences);
        });
    }

    private Optional<HtmlPage> loadPage(String url, @Nullable StepExecOrder currStepExecOrder) {
        final WebClient webClient = driverManager.getDriver();
        return loadHtmlPage(url, webClient, currStepExecOrder);
    }

    private void parsePageAndFilterDataResults(HtmlPage page, List<HtmlUnitScrapingStep<?>> parsingSequences) {
        executeNextSteps(new ScrapingContext(StepExecOrder.INITIAL, page), parsingSequences);
    }

    private void executeNextSteps(ScrapingContext ctx, List<HtmlUnitScrapingStep<?>> parsingSequences) {
        parsingSequences.forEach(s -> s.execute(ctx));
    }

    private Optional<HtmlPage> loadHtmlPage(String pageUrl, WebClient webClient, @Nullable StepExecOrder currStepExecOrder) {
        // TODO someway somehow we need to make this retriable ...
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
            throw new RequestException(e);
        }
    }

    private void printPageToConsole(HtmlPage page) {
        System.out.println(page.asXml());
    }

}