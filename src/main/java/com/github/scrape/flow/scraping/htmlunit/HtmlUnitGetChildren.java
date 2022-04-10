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
import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.Filter;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.htmlunit.filters.HtmlUnitFilterableByCommonCriteria;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HtmlUnitGetChildren extends HtmlUnitScrapingStep<HtmlUnitGetChildren>
        implements HtmlUnitFilterableByCommonCriteria<HtmlUnitGetChildren> {

    HtmlUnitGetChildren() {
    }

    @Override
    protected HtmlUnitGetChildren copy() {
        return copyFieldValuesTo(new HtmlUnitGetChildren());
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = nodesSearch(ctx.getNode());
            getHelper(services).execute(nodesSearch, ctx, stepOrder);
        };

        submitForExecution(stepOrder, runnable, services.getTaskService());

        return stepOrder;
    }

    Supplier<List<DomNode>> nodesSearch(DomNode parent) {
        return () -> {
            // important to include only html elements -> users for not expect to deal with other types when defining filtering operations (e.g. first() ... )
            return parent.getChildNodes().stream().filter(n -> n instanceof HtmlElement).collect(Collectors.toList());
        };
    }


    @Override
    public HtmlUnitGetChildren addFilter(Filter<DomNode> filter) {
        return super.doAddFilter(filter);
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }

}
