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

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.RequestException;
import com.github.scrape.flow.scraping.ScrapingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Log4j2
@RequiredArgsConstructor
public class HtmlUnitFollowLinkRunnable implements Runnable {

    private final ScrapingContext ctx;
    private final StepOrder stepOrder;
    private final HtmlUnitNodeSearchBasedStepHelper helper;
    private final String stepName;

    @Override
    public void run() {
        if (ctx.getNode() instanceof HtmlAnchor) {
            HtmlAnchor anch = (HtmlAnchor) ctx.getNode();
            if (anch.hasAttribute("href")) {
                Supplier<List<DomNode>> nextPageSupplier = clickLinkAndGetNextPage(stepOrder, anch);
                helper.execute(nextPageSupplier, ctx, stepOrder);
            } else {
                logWarn();
            }
        } else {
            logWarn();
        }
    }

    Supplier<List<DomNode>> clickLinkAndGetNextPage(StepOrder stepOrder, HtmlAnchor anch) {
        return () -> {
            try {
                HtmlPage currPage = anch.getHtmlPageOrNull();
                URL currUrl = currPage.getUrl();
                log.debug("{} - {}: Clicking HtmlAnchor element at {}", stepOrder, stepName, anch.getHrefAttribute());

                HtmlPage nextPage = anch.click();
                URL nextUrl = nextPage.getUrl();

                if (currUrl.equals(nextUrl)) {
                    log.info("Page is the same after clicking anchor element! Still at URL {}", currUrl);
                    return Collections.emptyList();
                } else {
//                  System.out.println(nextPage.asXml());
                    log.info("{} - {}: Loaded page URL after anchor clicked: {}", stepOrder, stepName, nextUrl.toString());
                    return List.of(nextPage);
                }

            } catch (Exception e) {
                log.error("{}: Error while clicking element {}", stepName, anch, e);
                throw new RequestException(e);
            }
        };
    }

    private void logWarn() {
        log.warn("{}: No anchor element with href attribute provided -> cannot click element! Check the steps sequence above step {} " +
                "and maybe provide search step for an anchor tag. It might be necessary to use scraping with JS support here", stepName, stepName);
    }

}
