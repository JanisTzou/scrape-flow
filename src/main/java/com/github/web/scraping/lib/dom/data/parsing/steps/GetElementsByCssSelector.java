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
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import com.github.web.scraping.lib.scraping.utils.HtmlUnitUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class GetElementsByCssSelector extends CommonOperationsStepBase<GetElementsByCssSelector> {

    private final String sccSelector;

    GetElementsByCssSelector(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, String sccSelector) {
        super(nextSteps);
        this.sccSelector = sccSelector;
    }

    GetElementsByCssSelector(String sccSelector) {
        this(null, sccSelector);
    }

    @Override
    public StepExecOrder execute(ParsingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = () -> HtmlUnitUtils.getDescendantsBySccSelector(ctx.getNode(), sccSelector);
            HtmlUnitStepHelper helper = new HtmlUnitStepHelper(nextSteps, getName(), services, collectorSetups);
            helper.execute(ctx, nodesSearch, stepExecOrder);
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

}