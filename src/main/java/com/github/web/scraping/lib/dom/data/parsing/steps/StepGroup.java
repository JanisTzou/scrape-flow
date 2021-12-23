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
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.parallelism.StepExecOrder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Used just to test context propagation
 */
public class StepGroup extends CommonOperationsStepBase<StepGroup>
        implements HtmlUnitSupportingNextStep<StepGroup>, HtmlUnitCollectorSetupStep<StepGroup> {

    StepGroup(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, Collecting<?, ?> collecting) {
        super(nextSteps);
        this.collecting = collecting;
    }

    StepGroup() {
        this(null, null);
    }



    @Override
    public <ModelT, ContainerT> StepExecOrder execute(ParsingContext<ModelT, ContainerT> ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode());
        @SuppressWarnings("unchecked")
        HtmlUnitParsingStepHelper<ModelT, ContainerT> wrapper = new HtmlUnitParsingStepHelper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName(), services);
        wrapper.execute(ctx, nodesSearch, stepExecOrder);

        return stepExecOrder;
    }

}
