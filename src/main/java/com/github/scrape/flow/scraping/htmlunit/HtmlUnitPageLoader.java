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

package com.github.scrape.flow.scraping.htmlunit;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.scrape.flow.clients.ClientOperator;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.*;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Log4j2
public class HtmlUnitPageLoader implements PageLoader<ClientOperator<WebClient>> {

    public HtmlUnitPageLoader() {
    }


    @Override
    public void loadPageAndExecuteNextSteps(String url,
                                            ScrapingContext ctx,
                                            List<ScrapingStep<?>> nextSteps,
                                            StepOrder currStepOrder,
                                            ScrapingServices services,
                                            ClientOperator<WebClient> clientOperator) {
        loadPage(url, currStepOrder, clientOperator).ifPresent(page1 -> {
            ScrapingContext nextCtx = ctx.toBuilder().setNode(page1).setPrevStepOrder(currStepOrder).build();
            executeNextSteps(nextCtx, nextSteps, services);
        });
    }

    private Optional<HtmlPage> loadPage(String url, @Nullable StepOrder currStepOrder, ClientOperator<WebClient> clientOperator) {
        return loadHtmlPage(url, clientOperator, currStepOrder);
    }


    private void executeNextSteps(ScrapingContext ctx, List<ScrapingStep<?>> nextSteps, ScrapingServices services) {
        nextSteps.forEach(s -> ScrapingStepInternalAccessor.of(s).execute(ctx, services));
    }

    private Optional<HtmlPage> loadHtmlPage(String pageUrl, ClientOperator<WebClient> clientOperator, @Nullable StepOrder currStepOrder) {
        // TODO someway somehow we need to make this retrievable ...
        String logInfo = currStepOrder != null ? currStepOrder + " - " : "";
        try {
            String windowName = clientOperator.getClient().getCurrentWindow().getName();
            log.info("{}Loading page in client {} at URL: {}", logInfo, clientOperator.getClientId(), pageUrl);
            URL url = new URL(pageUrl);
            Page page = clientOperator.getClient().getPage(url);  // we have one clientOperator instance per thread so this call is ok -> each client will have its own "current top WebWindow"
            WebResponse resp = page.getWebResponse();
            int statusCode = resp.getStatusCode();
            if (statusCode >= 400) {
                // TODO think about how to handle this and if we should le the clients to define desired behaviour (for retry logic to work etc ...)
                log.warn("{}Returned status {} - could not load page! Response time {}ms", logInfo, statusCode, resp.getLoadTime());
                return Optional.empty();
            } else {
                if (page.isHtmlPage()) {
                    log.info("{}Loaded page in {}ms in client {} at URL: {}", logInfo, resp.getLoadTime(), clientOperator.getClientId(), pageUrl);
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
