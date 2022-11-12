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
import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.*;
import com.github.scrape.flow.scraping.htmlunit.filters.HtmlUnitFilterableByAttribute;
import com.github.scrape.flow.scraping.htmlunit.filters.HtmlUnitFilterableByCssClass;
import com.github.scrape.flow.scraping.htmlunit.filters.HtmlUnitFilterableByTag;
import com.github.scrape.flow.scraping.htmlunit.filters.HtmlUnitFilterableByTextContent;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.function.Supplier;

import static com.github.scrape.flow.scraping.SiblingFilters.*;

@Log4j2
public class HtmlUnitGetSiblings extends HtmlUnitScrapingStep<HtmlUnitGetSiblings>
        implements HtmlUnitFilterableByAttribute<HtmlUnitGetSiblings>,
        HtmlUnitFilterableByTag<HtmlUnitGetSiblings>,
        HtmlUnitFilterableByTextContent<HtmlUnitGetSiblings>,
        HtmlUnitFilterableByCssClass<HtmlUnitGetSiblings>,
        FilterableSiblings<HtmlUnitGetSiblings, DomNode> {

    HtmlUnitGetSiblings() {
    }

    @Override
    protected HtmlUnitGetSiblings copy() {
        return copyFieldValuesTo(new HtmlUnitGetSiblings());
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = () -> getEligibleSiblings(ctx);
            getHelper(services).execute(nodesSearch, ctx, stepOrder);
        };

        submitForExecution(stepOrder, runnable, services);

        return stepOrder;
    }

    private List<DomNode> getEligibleSiblings(ScrapingContext ctx) {
        if (filters.stream().anyMatch(f -> PREV_SIBLINGS_FILTER_CLASSES.contains(f.getClass()))) {
            return HtmlUnitUtils.findPrevSiblingElements(ctx.getNode());
        } else if (filters.stream().anyMatch(f -> NEXT_SIBLINGS_FILTER_CLASSES.contains(f.getClass()))) {
            return HtmlUnitUtils.findNextSiblingElements(ctx.getNode());
        } else if (filters.stream().anyMatch(f -> ALL_SIBLINGS_FILTER_CLASSES.contains(f.getClass()))) {
            return HtmlUnitUtils.findAllSiblingElements(ctx.getNode());
        } else {
            return HtmlUnitUtils.findAllSiblingElements(ctx.getNode());
        }
    }


    @Override
    public HtmlUnitGetSiblings addFilter(Filter<DomNode> filter) {
        return super.doAddFilter(filter);
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }


}
