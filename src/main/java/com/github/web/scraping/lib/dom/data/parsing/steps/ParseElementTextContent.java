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

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import org.apache.commons.text.StringEscapeUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ParseElementTextContent extends HtmlUnitParsingStep<ParseElementTextContent>
        implements HtmlUnitCollectingToModelStep<ParseElementTextContent>,
        HtmlUnitStringTransformingStep<ParseElementTextContent> {

    // TODO provide a set of options to transform/sanitize text ... \n \t .. etc ...

    private BiConsumer<Object, String> modelMutation;
    private boolean excludeChildElementsTextContent;

    ParseElementTextContent() {
        this(null, null, false);
    }

    ParseElementTextContent(@Nullable List<HtmlUnitParsingStep<?>> nextSteps,
                            @Nullable BiConsumer<Object, String> modelMutation,
                            boolean excludeChildElementsTextContent) {
        super(nextSteps);
        this.excludeChildElementsTextContent = excludeChildElementsTextContent;
        this.modelMutation = modelMutation;
    }


    // TODO we need to be able to scrape another thing based on this parsed value ...
    //  how to communicate the parsed text to next steps? What to put in the context?


    @SuppressWarnings("unchecked")
    @Override
    public <ModelT, ContainerT> StepExecOrder execute(ParsingContext<ModelT, ContainerT> ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            String tc = null;
            if (ctx.getNode() instanceof HtmlElement htmlEl) {
                // TODO look for DomText ...
                tc = htmlEl.getTextContent();
                if (tc != null) {
                    tc = StringEscapeUtils.unescapeHtml4(tc);
                    // this should be optional ... used in cases when child elements' content filthies the parent element's content ...
                    if (this.excludeChildElementsTextContent) {
                        // TODO maybe we can use the text node for this very purpose ?
                        tc = removeChildElementsTextContent(tc, htmlEl);
                    }
                    tc = tc.trim();
                }
            }

            String transformed = transformParsedText(tc);

            setParsedStringToModel(modelMutation, ctx, transformed, getName());

            // TODO how to populate the following context?
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    private String removeChildElementsTextContent(String textContent, HtmlElement el) {
        for (DomElement childElement : el.getChildElements()) {
            textContent = textContent.replace(childElement.getTextContent(), "");
        }
        return textContent;
    }


    /**
     * Determines if the text of child elements should be part of the resulting parsed text content
     * set to false by default
     */
    public ParseElementTextContent excludeChildElements() {
        this.excludeChildElementsTextContent = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParseElementTextContent setCollector(BiConsumer<T, String> modelMutation) {
        this.modelMutation = (BiConsumer<Object, String>) modelMutation;
        return this;
    }

    @Override
    public ParseElementTextContent setTransformation(Function<String, String> parsedTextTransformation) {
        this.parsedTextTransformation = parsedTextTransformation;
        return this;
    }

}