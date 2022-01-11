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
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.htmlunit.filters.Filter;
import com.github.scrape.flow.scraping.htmlunit.filters.Filterable;
import com.github.scrape.flow.scraping.htmlunit.filters.FilterableByCommonCriteria;

import java.util.List;
import java.util.function.Supplier;

public class GetChildren extends CommonOperationsStepBase<GetChildren>
        implements FilterableByCommonCriteria<GetChildren>, Filterable<GetChildren> {

    GetChildren() {
    }

    @Override
    protected GetChildren copy() {
        return copyFieldValuesTo(new GetChildren());
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = nodesSearch(ctx.getNode());
            getHelper().execute(ctx, nodesSearch, stepOrder, getExecuteIf(), services);
        };

        submitForExecution(stepOrder, runnable, services.getTaskService());

        return stepOrder;
    }

    Supplier<List<DomNode>> nodesSearch(DomNode parent) {
        return () -> {
            // important to include only html elements -> users for not expect to deal with other types when defining filtering operations (e.g. first() ... )
            return parent.getChildNodes().stream().filter(n -> n instanceof HtmlElement).toList();
        };
    }


    @Override
    public GetChildren addFilter(Filter filter) {
        return super.addFilter(filter);
    }

}
