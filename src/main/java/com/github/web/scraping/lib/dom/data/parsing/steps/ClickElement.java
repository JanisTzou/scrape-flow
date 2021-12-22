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

import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

// TODO maybe we should express better that we expect the next page here ... or maybe in a new step ...
@Log4j2
public class ClickElement extends HtmlUnitParsingStep<ClickElement>
        implements HtmlUnitChainableStep<ClickElement> {

    public ClickElement(List<HtmlUnitParsingStep<?>> nextSteps) {
        super(nextSteps);
    }

    public ClickElement() {
        this(null);
    }

    public static ClickElement instance() {
        return new ClickElement();
    }

    @Override
    public <ModelT, ContainerT> StepExecOrder execute(ParsingContext<ModelT, ContainerT> ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            if (ctx.getNode() instanceof HtmlAnchor anch) {

                Supplier<List<DomNode>> nodesSearch = () -> {
                    try {
                        // TODO clean this mess ...
                        HtmlPage currPage = anch.getHtmlPageOrNull();
                        URL currUrl = currPage.getUrl();
                        WebWindow enclosingWindow = currPage.getEnclosingWindow();
                        log.debug("{} - {}: Clicking HtmlAnchor element at {}", stepExecOrder, getName(), anch.getHrefAttribute());
                        HtmlPage nextPage2 = (HtmlPage) anch.click();
                        // TODO we want to propagate this page in the context ... to the next steps ...
                        HtmlPage nextPage = (HtmlPage) enclosingWindow.getEnclosedPage();
                        URL nextUrl = nextPage.getUrl();
                        log.debug("{} - {}: Loaded page URL after anchor clicked: {}", stepExecOrder, getName(), nextUrl.toString());
//                  System.out.println(nextPage.asXml());

                        if (nextPage2 != null) {
                            return List.of(nextPage2);
                        } else {
                            return Collections.emptyList();
                        }
                    } catch (IOException e) {
                        log.error("{}: Error while clicking element {}", getName(), anch, e);
                        return Collections.emptyList();
                    }
                };
                @SuppressWarnings("unchecked")
                HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> wrapper = new HtmlUnitParsingExecutionWrapper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName(), services);
                wrapper.execute(ctx, nodesSearch, stepExecOrder);

            } else {
                log.warn("{}: No HtmlAnchor element provided -> cannot click element! Check the steps sequence above step {} and maybe provide search step for an anchor tag", getName(), getName());
            }
        };

        handleExecution(stepExecOrder, runnable);
        return stepExecOrder;
    }

    @Override
    public ClickElement then(HtmlUnitParsingStep<?> nextStep) {
        this.nextSteps.add(nextStep);
        return this;
    }

    @Override
    public boolean throttlingAllowed() {
        return true;
    }
}
