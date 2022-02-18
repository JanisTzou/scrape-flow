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
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.Filter;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.htmlunit.filters.HtmlUnitFilterableByCommonCriteria;

import java.util.List;
import java.util.function.Supplier;

public class HtmlUnitGetDescendantsByCssSelector extends HtmlUnitScrapingStep<HtmlUnitGetDescendantsByCssSelector>
        implements HtmlUnitFilterableByCommonCriteria<HtmlUnitGetDescendantsByCssSelector> {

    // this cannot be a filter ... it's more of a "Get" operation ...
    // it would not make sense to first get descendants/children and then this ...

    private final String sccSelector;

    HtmlUnitGetDescendantsByCssSelector(String sccSelector) {
        this.sccSelector = sccSelector;
    }

    @Override
    protected HtmlUnitGetDescendantsByCssSelector copy() {
        return copyFieldValuesTo(new HtmlUnitGetDescendantsByCssSelector(sccSelector));
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = () -> HtmlUnitUtils.getDescendantsBySccSelector(ctx.getNode(), sccSelector);
            getHelper().execute(ctx, nodesSearch, stepOrder, getExecuteIf(), services);
        };

        submitForExecution(stepOrder, runnable, services.getTaskService(), services.getSeleniumDriversManager());

        return stepOrder;
    }

    @Override
    public HtmlUnitGetDescendantsByCssSelector addFilter(Filter<DomNode> filter) {
        return super.doAddFilter(filter);
    }
}
