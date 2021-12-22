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
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import com.github.web.scraping.lib.scraping.utils.HtmlUnitUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class GetElementsByTag extends CommonOperationsStepBase<GetElementsByTag> {

    private final String tagName;

    /**
     * @param nextSteps
     * @param tagName
     * @throws NullPointerException if tagName is null
     */
    public GetElementsByTag(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, String tagName) {
        super(nextSteps);
        Objects.requireNonNull(tagName);
        this.tagName = tagName;
    }

    public GetElementsByTag(String tagName) {
        this(null, tagName);
    }

    public static GetElementsByTag instance(String tagName) {
        return new GetElementsByTag(tagName);
    }

    public static GetElementsByTag div() {
        return new GetElementsByTag("div");
    }

    public static GetElementsByTag anchor() {
        return new GetElementsByTag("a");
    }

    public static GetElementsByTag li() {
        return new GetElementsByTag("li");
    }

    public static GetElementsByTag span() {
        return new GetElementsByTag("span");
    }

    public static GetElementsByTag img() {
        return new GetElementsByTag("img");
    }


    @Override
    public <ModelT, ContainerT> List<StepResult> execute(ParsingContext<ModelT, ContainerT> ctx, ExecutionMode mode, OnOrderGenerated onOrderGenerated) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder(), onOrderGenerated);

        Callable<List<StepResult>> callable = () -> {
            Supplier<List<DomNode>> nodesSearch = () -> HtmlUnitUtils.getDescendantsByTagName(ctx.getNode(), tagName);
            @SuppressWarnings("unchecked")
            HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> wrapper = new HtmlUnitParsingExecutionWrapper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName(), services);
            return wrapper.execute(ctx, nodesSearch, stepExecOrder, mode);
        };

        return handleExecution(mode, stepExecOrder, callable);
    }

}
