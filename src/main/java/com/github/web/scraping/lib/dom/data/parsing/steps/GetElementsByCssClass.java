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
import com.github.web.scraping.lib.scraping.utils.HtmlUnitUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class GetElementsByCssClass extends HtmlUnitChainableStep<GetElementsByCssClass> implements HtmlUnitCollectingStep<GetElementsByCssClass> {

    private final String cssClassName;
    private Collecting<?, ?> collecting;

    public GetElementsByCssClass(@Nullable List<HtmlUnitParsingStep> nextSteps, String cssClassName) {
        super(nextSteps);
        this.cssClassName = cssClassName;
    }

    public GetElementsByCssClass(String cssClassName) {
        this(null, cssClassName);
    }

    public static GetElementsByCssClass instance(String cssClassName) {
        return new GetElementsByCssClass(cssClassName);
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        Supplier<List<DomNode>> nodesSearch = () -> HtmlUnitUtils.getAllChildElementsByClass(ctx.getNode(), cssClassName);
        return new HtmlUnitParsingExecutionWrapper<>(nextSteps, collecting).execute(ctx, nodesSearch);
    }

    @Override
    public <R, T> GetElementsByCssClass collector(Supplier<T> modelSupplier, Supplier<R> containerSupplier, BiConsumer<R, T> accumulator) {
        this.collecting = new Collecting<>(modelSupplier, containerSupplier, accumulator);
        return this;
    }

    @Override
    public <R, T> GetElementsByCssClass collector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator) {
        this.collecting = new Collecting<>(modelSupplier, null, accumulator);
        return this;
    }


}
