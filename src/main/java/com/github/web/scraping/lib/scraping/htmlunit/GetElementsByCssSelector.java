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

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class GetElementsByCssSelector extends CommonOperationsStepBase<GetElementsByCssSelector>
        implements FilterableByCommonCriteria<GetElementsByCssSelector> {

    // this cannot be a filter ... it's more of a "Get" operation ...
    // it would not make sense to first get descendants/children and then this ...

    private final String sccSelector;

    GetElementsByCssSelector(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps, String sccSelector) {
        super(nextSteps);
        this.sccSelector = sccSelector;
    }

    GetElementsByCssSelector(String sccSelector) {
        this(null, sccSelector);
    }

    @Override
    protected GetElementsByCssSelector copy() {
        return copyFieldValuesTo(new GetElementsByCssSelector(sccSelector));
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
    public GetElementsByCssSelector addFilter(Filter filter) {
        return super.addFilter(filter);
    }
}
