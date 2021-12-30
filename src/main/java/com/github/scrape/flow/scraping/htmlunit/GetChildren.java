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
import com.github.scrape.flow.parallelism.StepExecOrder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class GetChildren extends CommonOperationsStepBase<GetChildren>
        implements FilterableByCommonCriteria<GetChildren>, Filterable<GetChildren> {

    GetChildren(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps) {
        super(nextSteps);
    }

    GetChildren() {
        this(null);
    }

    @Override
    protected GetChildren copy() {
        return copyFieldValuesTo(new GetChildren());
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = () -> {
                // important to include only html elements -> users for not expect to deal with other types when defining filtering operations (e.g. first() ... )
                return ctx.getNode().getChildNodes().stream().filter(n -> n instanceof HtmlElement).toList();
            };
            getHelper().execute(ctx, nodesSearch, stepExecOrder, getExecuteIf());
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }


    @Override
    public GetChildren addFilter(Filter filter) {
        return super.addFilter(filter);
    }

}
