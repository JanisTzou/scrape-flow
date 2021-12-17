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

public class GetElementsByXPath extends HtmlUnitChainableStep<GetElementsByXPath>
        implements HtmlUnitCollectorSetupStep<GetElementsByXPath> {

    private final String xPath;
    private Collecting<?, ?> collecting;

    protected GetElementsByXPath(@Nullable List<HtmlUnitParsingStep> nextSteps, String xPath) {
        super(nextSteps);
        this.xPath = xPath;
    }

    public GetElementsByXPath(String xPath) {
        this(null, xPath);
    }

    public static GetElementsByXPath instance(String xPath) {
        return new GetElementsByXPath(xPath);
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        Supplier<List<DomNode>> nodesSearch = () -> ctx.getNode().getByXPath(xPath);
        return new HtmlUnitParsingExecutionWrapper<>(nextSteps, collecting).execute(ctx, nodesSearch);
    }

    @Override
    public <R, T> GetElementsByXPath collector(Supplier<T> modelSupplier, Supplier<R> containerSupplier, BiConsumer<R, T> accumulator) {
        this.collecting = new Collecting<>(modelSupplier, containerSupplier, accumulator);
        return this;
    }

    @Override
    public <R, T> GetElementsByXPath collector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator) {
        this.collecting = new Collecting<>(modelSupplier, null, accumulator);
        return this;
    }

}
