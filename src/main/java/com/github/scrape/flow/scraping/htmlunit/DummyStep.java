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
import com.github.scrape.flow.parallelism.StepExecOrder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Used just to test context propagation
 */
public class DummyStep extends CommonOperationsStepBase<DummyStep>
        implements HtmlUnitStepSupportingNext<DummyStep>, HtmlUnitStepSupportingCollection<DummyStep> {

    protected DummyStep(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps) {
        super(nextSteps);
    }

    public DummyStep() {
        this(null);
    }

    public static DummyStep instance() {
        return new DummyStep();
    }

    @Override
    protected DummyStep copy() {
        return copyFieldValuesTo(new DummyStep());
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode());
        getHelper().execute(ctx, nodesSearch, stepExecOrder, getExecuteIf());

        return stepExecOrder;
    }

}
