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
import com.github.web.scraping.lib.dom.data.parsing.StepResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Used just to test context propagation
 */
public class EmptyStep extends HtmlUnitParsingStep<EmptyStep>
        implements HtmlUnitChainableStep<EmptyStep>, HtmlUnitCollectorSetupStep<EmptyStep> {

    private Collecting<?, ?> collecting;

    protected EmptyStep(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, Collecting<?, ?> collecting) {
        super(nextSteps);
        this.collecting = collecting;
    }

    public EmptyStep() {
        this(null, null);
    }

    public static EmptyStep instance() {
        return new EmptyStep();
    }

    @Override
    public <ModelT, ContainerT> List<StepResult> execute(ParsingContext<ModelT, ContainerT> ctx) {
        logExecutionStart();
        Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode());
        @SuppressWarnings("unchecked")
        HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> wrapper = new HtmlUnitParsingExecutionWrapper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName());
        return wrapper.execute(ctx, nodesSearch);
    }

    @Override
    public <R, T> EmptyStep collector(Supplier<T> modelSupplier, Supplier<R> containerSupplier, BiConsumer<R, T> accumulator) {
        this.collecting = new Collecting<>(modelSupplier, containerSupplier, accumulator);
        return this;
    }

    @Override
    public <R, T> EmptyStep collector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator) {
        this.collecting = new Collecting<>(modelSupplier, null, accumulator);
        return this;
    }

    @Override
    public EmptyStep then(HtmlUnitParsingStep<?> nextStep) {
        this.nextSteps.add(nextStep);
        return this;
    }


}
