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
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.github.web.scraping.lib.dom.data.parsing.ParsedElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.parallelism.StepExecOrder;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParseElementHRef extends HtmlUnitParsingStep<ParseElementHRef>
        implements HtmlUnitChainableStep<ParseElementHRef>,
        HtmlUnitCollectingToModelStep<ParseElementHRef>,
        HtmlUnitStringTransformingStep<ParseElementHRef> {

    private BiConsumer<Object, String> modelMutation;
    // TODO add some filtering logic for the hrefs parsed ...


    protected ParseElementHRef(@Nullable List<HtmlUnitParsingStep<?>> nextSteps) {
        super(nextSteps);
    }

    public ParseElementHRef() {
        this(null);
    }

    public static ParseElementHRef instance() {
        return new ParseElementHRef();
    }

    @Override
    public <ModelT, ContainerT> List<StepResult> execute(ParsingContext<ModelT, ContainerT> ctx, ExecutionMode mode, OnOrderGenerated onOrderGenerated) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder(), onOrderGenerated);

        Callable<List<StepResult>> callable = () -> {
            if (ctx.getNode() instanceof HtmlAnchor anch) {
                String href = anch.getHrefAttribute();
                if (href != null) {
                    String transformed = transformParsedText(href);
                    // TODO actually have another transformation that will say something like "transformToFullURL ... and put that one to the context below)
                    setParsedStringToModel(modelMutation, ctx, transformed, getName());
                    Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode()); // just resend the node ... // TODO actually think if this is best ...
                    @SuppressWarnings("unchecked")
                    HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> wrapper = new HtmlUnitParsingExecutionWrapper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName(), services);
                    ParsingContext<ModelT, ContainerT> ctxCopy = ctx.toBuilder().setParsedURL(transformed).build();
                    List<StepResult> nextResults = wrapper.execute(ctxCopy, nodesSearch, stepExecOrder, mode);
                    return Stream.concat(Stream.of(new ParsedElement(null, transformed, null, true, ctx.getNode())), nextResults.stream()).collect(Collectors.toList());
                }
            }
            return Collections.emptyList();
        };

        return handleExecution(mode, stepExecOrder, callable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParseElementHRef setCollector(BiConsumer<T, String> modelMutation) {
        this.modelMutation = (BiConsumer<Object, String>) modelMutation;
        return this;
    }

    // TODO perhaps we should distinguish between the then() and thenNavigate() steps that come through those methods  and have separate executions for both?
    @Override
    public ParseElementHRef then(HtmlUnitParsingStep<?> nextStep) {
        this.nextSteps.add(nextStep);
        return this;
    }

    // TODO provide JavaDoc
    public ParseElementHRef thenNavigate(NavigateToNewSite nextStep) {
        this.nextSteps.add(nextStep);
        return this;
    }

    @Override
    public ParseElementHRef setTransformation(Function<String, String> parsedTextToNewText) {
        this.parsedTextTransformation = parsedTextToNewText;
        return this;
    }

}
