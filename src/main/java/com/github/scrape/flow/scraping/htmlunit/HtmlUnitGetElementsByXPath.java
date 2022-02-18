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
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.Filter;
import com.github.scrape.flow.scraping.htmlunit.filters.HtmlUnitFilterableByCommonCriteria;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HtmlUnitGetElementsByXPath extends HtmlUnitScrapingStep<HtmlUnitGetElementsByXPath>
        implements HtmlUnitFilterableByCommonCriteria<HtmlUnitGetElementsByXPath> {

    private final String xPathExpr;


    HtmlUnitGetElementsByXPath(String xPathExpr) {
        this.xPathExpr = xPathExpr;
    }

    @Override
    protected HtmlUnitGetElementsByXPath copy() {
        return copyFieldValuesTo(new HtmlUnitGetElementsByXPath(xPathExpr));
    }


    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = () ->
                    ctx.getNode().getByXPath(xPathExpr).stream()
                            .flatMap(obj -> HtmlUnitUtils.toDomNode(obj).stream())
                            .filter(domNode -> domNode instanceof HtmlElement)  // important to include only html elements -> users for not expect to deal with other types when defining filtering operations (e.g. first() ... )
                            .collect(Collectors.toList());
            getHelper().execute(ctx, nodesSearch, stepOrder, getExecuteIf(), services);
        };

        submitForExecution(stepOrder, runnable, services.getTaskService(), services.getSeleniumDriversManager());

        return stepOrder;
    }

    @Override
    public HtmlUnitGetElementsByXPath addFilter(Filter<DomNode> filter) {
        return super.doAddFilter(filter);
    }

}
