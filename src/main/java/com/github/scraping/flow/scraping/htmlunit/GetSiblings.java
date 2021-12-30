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
import com.github.scraping.flow.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class GetSiblings extends CommonOperationsStepBase<GetSiblings>
        implements FilterableByAttribute<GetSiblings>,
        FilterableByTag<GetSiblings>,
        FilterableByTextContent<GetSiblings>,
        FilterableByCssClass<GetSiblings>,
        FilterableSiblings<GetSiblings>,
        Filterable<GetSiblings> {

    private static final List<Class<?>> PREV_SIBLINGS_FILTER_CLASSES = List.of(
            FilterSiblingsPrevN.class,
            FilterSiblingsPrevNth.class,
            FilterSiblingsPrevEveryNth.class,
            FilterSiblingsFirst.class
    );

    private static final List<Class<?>> NEXT_SIBLINGS_FILTER_CLASSES = List.of(
            FilterSiblingsNextN.class,
            FilterSiblingsNextNth.class,
            FilterSiblingsNextEveryNth.class,
            FilterSiblingsLast.class
    );

    GetSiblings(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps) {
        super(nextSteps);
    }

    GetSiblings() {
        this(null);
    }

    @Override
    protected GetSiblings copy() {
        return copyFieldValuesTo(new GetSiblings());
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = () -> getEligibleSiblings(ctx);
            getHelper().execute(ctx, nodesSearch, stepExecOrder, getExecuteIf());
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    private List<DomNode> getEligibleSiblings(ScrapingContext ctx) {
        if (filters.stream().anyMatch(f -> PREV_SIBLINGS_FILTER_CLASSES.contains(f.getClass()))) {
            return HtmlUnitUtils.findPrevSiblingElements(ctx.getNode());
        } else if (filters.stream().anyMatch(f -> NEXT_SIBLINGS_FILTER_CLASSES.contains(f.getClass()))) {
            return HtmlUnitUtils.findNextSiblingElements(ctx.getNode());
        } else {
            log.error("Failed to find a matching filter for siblings in {}. Will return all child nodes.", getName());
            return ctx.getNode().getChildNodes();
        }
    }


    @Override
    public GetSiblings addFilter(Filter filter) {
        return super.addFilter(filter);
    }

}
