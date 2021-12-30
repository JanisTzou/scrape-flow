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

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.scraping.flow.parallelism.StepExecOrder;
import com.github.scraping.flow.scraping.LoadingNewPage;
import com.github.scraping.flow.scraping.RequestException;
import lombok.extern.log4j.Log4j2;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class FollowLink extends CommonOperationsStepBase<FollowLink>
        implements LoadingNewPage {

    FollowLink(List<HtmlUnitScrapingStep<?>> nextSteps) {
        super(nextSteps);
    }

    FollowLink() {
        this(null);
    }

    @Override
    protected FollowLink copy() {
        return copyFieldValuesTo(new FollowLink());
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            if (ctx.getNode() instanceof HtmlAnchor anch) {

                Supplier<List<DomNode>> nodesSearch = () -> {
                    try {
                        HtmlPage currPage = anch.getHtmlPageOrNull();
                        URL currUrl = currPage.getUrl();
                        log.debug("{} - {}: Clicking HtmlAnchor element at {}", stepExecOrder, getName(), anch.getHrefAttribute());

                        // TODO we want to propagate this page in the context ... to the next steps ...
                        HtmlPage nextPage = anch.click();
                        URL nextUrl = nextPage.getUrl();

                        if (currUrl.equals(nextUrl)) {
                            log.info("Page is the same after clicking anchor element! Still at URL {}", currUrl);
                            return Collections.emptyList();
                        } else {
//                          System.out.println(nextPage.asXml());
                            log.info("{} - {}: Loaded page URL after anchor clicked: {}", stepExecOrder, getName(), nextUrl.toString());
                            return List.of(nextPage);
                        }

                    } catch (Exception e) {
                        log.error("{}: Error while clicking element {}", getName(), anch, e);
                        throw new RequestException(e);
                    }
                };
                getHelper().execute(ctx, nodesSearch, stepExecOrder, getExecuteIf());

            } else {
                log.warn("{}: No HtmlAnchor element provided -> cannot click element! Check the steps sequence above step {} and maybe provide search step for an anchor tag", getName(), getName());
            }
        };

        handleExecution(stepExecOrder, runnable);
        return stepExecOrder;
    }

    @Override
    public boolean throttlingAllowed() {
        return true;
    }
}