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
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.parallelism.StepExecOrder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ParseElementHRef extends HtmlUnitParsingStep<ParseElementHRef>
        implements HtmlUnitChainableStep<ParseElementHRef>,
        HtmlUnitCollectingToModelStep<ParseElementHRef>,
        HtmlUnitStringTransformingStep<ParseElementHRef> {

    private BiConsumer<Object, String> modelMutation;
    // TODO add some filtering logic for the hrefs parsed ...

    // TODO this is basically a specialisation of ParseAttributeValue


    ParseElementHRef(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, Function<String, String> parsedTextTransformation) {
        super(nextSteps);
        this.parsedTextTransformation = parsedTextTransformation;
    }

    ParseElementHRef(Function<String, String> parsedTextTransformation) {
        this(null, parsedTextTransformation);
    }

    ParseElementHRef(@Nullable List<HtmlUnitParsingStep<?>> nextSteps) {
        this(nextSteps, null);
    }

    ParseElementHRef() {
        this(null, null);
    }

    public static ParseElementHRef instance() {
        return new ParseElementHRef();
    }

    @Override
    public <ModelT, ContainerT> StepExecOrder execute(ParsingContext<ModelT, ContainerT> ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            if (ctx.getNode() instanceof HtmlAnchor anch) {
                String href = anch.getHrefAttribute();
                if (href != null) {
                    String transformed = transformParsedText(href);
                    log.debug("{} - {}: Parsed href: {}", stepExecOrder, getName(), transformed);
                    // TODO actually have another transformation that will say something like "transformToFullURL ... and put that one to the context below)
                    setParsedStringToModel(modelMutation, ctx, transformed, getName());
                    Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode()); // just resend the node ... // TODO actually think if this is best ...
                    @SuppressWarnings("unchecked")
                    HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> wrapper = new HtmlUnitParsingExecutionWrapper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName(), services);
                    ParsingContext<ModelT, ContainerT> ctxCopy = ctx.toBuilder().setParsedURL(transformed).build();
                    wrapper.execute(ctxCopy, nodesSearch, stepExecOrder);
                }
            } else {
                log.warn("No HtmlAnchor element provided -> cannot parse href value! Check the steps sequence above step {}", getName());
            }
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
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
    public ParseElementHRef thenNavigate(NavigateToParsedLink nextStep) {
        this.nextSteps.add(nextStep);
        return this;
    }

    @Override
    public ParseElementHRef setTransformation(Function<String, String> parsedTextToNewText) {
        this.parsedTextTransformation = parsedTextToNewText;
        return this;
    }

}
