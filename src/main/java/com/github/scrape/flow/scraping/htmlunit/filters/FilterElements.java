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
import com.github.scrape.flow.parallelism.StepExecOrder;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.htmlunit.CommonOperationsStepBase;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitScrapingStep;
import com.github.scrape.flow.scraping.htmlunit.ScrapingContext;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Filters nodes acquired in the previous steps by custom conditions
 */
@Log4j2
public class FilterElements extends CommonOperationsStepBase<FilterElements> {

    private final Predicate<DomNode> domNodePredicate;

    FilterElements(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps, Predicate<DomNode> domNodePredicate) {
        super(nextSteps);
        this.domNodePredicate = domNodePredicate;
    }

    public FilterElements(Predicate<DomNode> domNodePredicate) {
        this(null, domNodePredicate);
    }

    @Override
    protected FilterElements copy() {
        return copyFieldValuesTo(new FilterElements(domNodePredicate));
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepExecOrder stepExecOrder = services.getStepExecOrderGenerator().genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodeSupplier = () -> Stream.of(ctx.getNode()).filter(domNodePredicate).collect(Collectors.toList());
            getHelper().execute(ctx, nodeSupplier, stepExecOrder, getExecuteIf(), services);
        };

        submitForExecution(stepExecOrder, runnable, services.getTaskService());

        return stepExecOrder;
    }

}
