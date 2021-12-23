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

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Used just to test context propagation
 */
public class StepGroup extends CommonOperationsStepBase<StepGroup>
        implements HtmlUnitSupportingNextStep<StepGroup>, HtmlUnitStepSupportingCollection<StepGroup> {

    StepGroup(@Nullable List<HtmlUnitParsingStep<?>> nextSteps) {
        super(nextSteps);
    }

    StepGroup() {
        this(null);
    }



    @Override
    public  StepExecOrder execute(ParsingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode());
        HtmlUnitParsingStepHelper wrapper = new HtmlUnitParsingStepHelper(nextSteps, getName(), services, collectorSetups);
        wrapper.execute(ctx, nodesSearch, stepExecOrder);

        return stepExecOrder;
    }

}
