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
import com.github.web.scraping.lib.scraping.utils.HtmlUnitUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class GetElementsByCssClass extends HtmlUnitParsingStep {

    private final String cssClassName;

    private final List<HtmlUnitParsingStep> nextSteps = new ArrayList<>();
    private Collecting<?, ?> collecting;

    public GetElementsByCssClass(String cssClassName) {
        this.cssClassName = cssClassName;
    }

    public static GetElementsByCssClass instance(String cssClassName) {
        return new GetElementsByCssClass(cssClassName);
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        return new HtmlUnitParsingExecutionWrapper<>(nextSteps, collecting)
                .execute(ctx, () -> HtmlUnitUtils.getAllChildElementsByClass(ctx.getNode(), cssClassName));
    }

    public GetElementsByCssClass then(HtmlUnitParsingStep nextStep) {
        this.nextSteps.add(nextStep);
        return this;
    }

    public GetElementsByCssClass collector(Supplier<?> modelSupplier) {
        this.collecting = new Collecting<>(modelSupplier, null, null);
        return this;
    }

    public <R, T> GetElementsByCssClass collector(Supplier<T> modelSupplier, Supplier<R> containerSupplier, BiConsumer<R, T> accumulator) {
        this.collecting = new Collecting<>(modelSupplier, containerSupplier, accumulator);
        return this;
    }

    // this effectively collects to prev. step model ...
    // if a collector exists ...and it is found in the scraping context ...
    public <R, T> GetElementsByCssClass collector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator) {
        this.collecting = new Collecting<>(modelSupplier, null, accumulator);
        return this;
    }


}
