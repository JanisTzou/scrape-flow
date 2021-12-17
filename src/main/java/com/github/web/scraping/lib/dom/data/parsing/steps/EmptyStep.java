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

import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Used just to test context propagation
 */
@NoArgsConstructor
public class EmptyStep extends HtmlUnitParsingStep implements HtmlUnitCollectorSetupStep<EmptyStep> {

    private final List<HtmlUnitParsingStep> nextSteps = new ArrayList<>();
    private Collecting<?, ?> collecting;

    public static EmptyStep instance(String cssClassName) {
        return new EmptyStep();
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        return new HtmlUnitParsingExecutionWrapper<>(nextSteps, collecting)
                .execute(ctx, () -> List.of(ctx.getNode()));
    }

    public EmptyStep then(HtmlUnitParsingStep nextStep) {
        this.nextSteps.add(nextStep);
        return this;
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


}
