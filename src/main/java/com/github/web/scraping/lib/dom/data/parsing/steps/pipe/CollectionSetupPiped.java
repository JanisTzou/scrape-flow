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

package com.github.web.scraping.lib.dom.data.parsing.steps.pipe;

import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.dom.data.parsing.steps.Collecting;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class CollectionSetupPiped extends HtmlUnitParsingStepPipe {

    private Collecting<?, ?> collecting;

    public CollectionSetupPiped(HtmlUnitParsingStepPipe prevStep, Collecting<?, ?> collecting) {
        super(prevStep);
        this.collecting = collecting;
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        return prevStep.execute(ctx);
//        Supplier<List<DomNode>> nodesSearch = () -> HtmlUnitUtils.getDescendantsByClass(ctx.getNode(), cssClassName);
//        return new HtmlUnitParsingExecutionWrapper<>(nextSteps, collecting, getName()).execute(ctx, nodesSearch);
    }

    @Override
    public <R, T> CollectionSetupPiped setCollector(Supplier<T> modelSupplier, Supplier<R> containerSupplier, BiConsumer<R, T> accumulator) {
        mustBeNull(this.collecting);
        this.collecting = new Collecting<>(modelSupplier, containerSupplier, accumulator);
        return this;
    }

    @Override
    public <R, T> CollectionSetupPiped setCollector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator) {
        mustBeNull(this.collecting);
        this.collecting = new Collecting<>(modelSupplier, null, accumulator);
        return this;
    }

}
