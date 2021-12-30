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

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class GetDescendantsByCssSelector extends CommonOperationsStepBase<GetDescendantsByCssSelector>
        implements FilterableByCommonCriteria<GetDescendantsByCssSelector> {

    // this cannot be a filter ... it's more of a "Get" operation ...
    // it would not make sense to first get descendants/children and then this ...

    private final String sccSelector;

    GetDescendantsByCssSelector(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps, String sccSelector) {
        super(nextSteps);
        this.sccSelector = sccSelector;
    }

    GetDescendantsByCssSelector(String sccSelector) {
        this(null, sccSelector);
    }

    @Override
    protected GetDescendantsByCssSelector copy() {
        return copyFieldValuesTo(new GetDescendantsByCssSelector(sccSelector));
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = () -> HtmlUnitUtils.getDescendantsBySccSelector(ctx.getNode(), sccSelector);
            getHelper().execute(ctx, nodesSearch, stepExecOrder, getExecuteIf());
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    @Override
    public GetDescendantsByCssSelector addFilter(Filter filter) {
        return super.addFilter(filter);
    }
}
