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

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.github.web.scraping.lib.dom.data.parsing.ParsedElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class ParseElementHRef extends HtmlUnitParsingStep<ParseElementHRef>
        implements HtmlUnitChainingStep<ParseElementHRef>,
        HtmlUnitCollectingToModelStep<ParseElementHRef>{

    private final Enum<?> identifier;
    private BiConsumer<Object, String> modelMutation;
    // TODO add some filtering logic for the hrefs parsed ...


    protected ParseElementHRef(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, Enum<?> identifier) {
        super(nextSteps);
        this.identifier = identifier;
    }

    public ParseElementHRef(Enum<?> identifier) {
        this(null, identifier);
    }

    public static ParseElementHRef instance(Enum<?> identifier) {
        return new ParseElementHRef(identifier);
    }

    @Override
    public <ModelT, ContainerT> List<StepResult> execute(ParsingContext<ModelT, ContainerT> ctx) {
        if (ctx.getNode() instanceof HtmlAnchor anch) {
            String href = anch.getHrefAttribute();
            if (href != null) {
                setParsedStringToModel(modelMutation, ctx, href);
                return List.of(new ParsedElement(identifier, href, null, true, ctx.getNode()));
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParseElementHRef thenCollect(BiConsumer<T, String> modelMutation) {
        this.modelMutation = (BiConsumer<Object, String>) modelMutation;
        return this;
    }

    @Override
    public ParseElementHRef then(HtmlUnitParsingStep<?> nextStep) {
        this.nextSteps.add(nextStep);
        return this;
    }

}
