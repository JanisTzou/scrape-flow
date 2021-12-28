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

package com.github.web.scraping.lib.scraping.htmlunit;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
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

    FilterElements(Predicate<DomNode> domNodePredicate) {
        this(null, domNodePredicate);
    }

    @Override
    protected FilterElements copy() {
        return copyFieldValuesTo(new FilterElements(domNodePredicate));
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodeSupplier = () -> Stream.of(ctx.getNode()).filter(domNodePredicate).collect(Collectors.toList());
            getHelper().execute(ctx, nodeSupplier, stepExecOrder, getExecuteIf());
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

}
