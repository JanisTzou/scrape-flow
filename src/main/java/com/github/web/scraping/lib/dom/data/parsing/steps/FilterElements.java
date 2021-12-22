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

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Filters nodes acquired in the previous steps by custom conditions
 */
@Log4j2
public class FilterElements extends CommonOperationsStepBase<FilterElements> {

    private final Predicate<DomNode> domNodePredicate;

    FilterElements(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, Predicate<DomNode> domNodePredicate) {
        super(nextSteps);
        this.domNodePredicate = domNodePredicate;
    }

    public FilterElements(Predicate<DomNode> domNodePredicate) {
        this(null, domNodePredicate);
    }

    @Override
    public <ModelT, ContainerT> List<StepResult> execute(ParsingContext<ModelT, ContainerT> ctx, ExecutionMode mode, OnOrderGenerated onOrderGenerated) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder(), onOrderGenerated);

        Callable<List<StepResult>> callable = () -> {
            Supplier<List<DomNode>> nodesSearch = () -> {
                if (domNodePredicate.test(ctx.getNode())) {
                    log.debug("{} element passed filter: {}", getName(), ctx.getNode());
                    return List.of(ctx.getNode());
                } else {
                    log.debug("{} element filtered away: {}", getName(), ctx.getNode());
                }
                return Collections.emptyList();
            };

            @SuppressWarnings("unchecked")
            HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> wrapper = new HtmlUnitParsingExecutionWrapper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName(), services);
            return wrapper.execute(ctx, nodesSearch, stepExecOrder, mode);
        };

        return handleExecution(mode, stepExecOrder, callable);
    }

}
