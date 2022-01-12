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

package com.github.scrape.flow.scraping.htmlunit.filters;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitScrapingStep;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Filters nodes acquired in the previous steps by custom conditions
 */
@Log4j2
public class FilterElements extends HtmlUnitScrapingStep<FilterElements> {

    private final Predicate<DomNode> domNodePredicate;

    public FilterElements(Predicate<DomNode> domNodePredicate) {
        this.domNodePredicate = domNodePredicate;
    }

    @Override
    protected FilterElements copy() {
        return copyFieldValuesTo(new FilterElements(domNodePredicate));
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodeSupplier = () -> Stream.of(ctx.getNode()).filter(domNodePredicate).collect(Collectors.toList());
            getHelper().execute(ctx, nodeSupplier, stepOrder, getExecuteIf(), services);
        };

        submitForExecution(stepOrder, runnable, services.getTaskService());

        return stepOrder;
    }

}
