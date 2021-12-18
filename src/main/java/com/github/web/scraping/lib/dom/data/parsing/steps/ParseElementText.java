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
import com.github.web.scraping.lib.dom.data.parsing.ParsedElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

public class ParseElementText extends HtmlUnitParsingStep<ParseElementText>
        implements HtmlUnitChainingStep<ParseElementText>,
        HtmlUnitCollectingToModelStep<ParseElementText> {


    private boolean removeChildElementsTextContent;
    private BiConsumer<Object, String> modelMutation;

    public ParseElementText() {
        this(null, true, null);
    }

    protected ParseElementText(@Nullable List<HtmlUnitParsingStep> nextSteps,
                               boolean removeChildElementsTextContent,
                               @Nullable BiConsumer<Object, String> modelMutation) {
        super(nextSteps);
        this.removeChildElementsTextContent = removeChildElementsTextContent;
        this.modelMutation = modelMutation;
    }

    public static ParseElementText instance() {
        return new ParseElementText();
    }


    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        String tc = null;
        if (ctx.getNode() instanceof HtmlElement htmlEl) {
            tc = htmlEl.getTextContent();
            if (tc != null) {
                // this should be optional ... used in cases when child elements' content filthies the parent element's content ...
                if (this.removeChildElementsTextContent) {
                    tc = removeChildElementsTextContent(tc, htmlEl);
                }
                tc = tc.trim();
            }
        }

        setParsedValueToModel(modelMutation, ctx, tc);

        ParsedElement parsedElement = new ParsedElement(null, null, tc, false, ctx.getNode());
        parsedElement.setModelProxy(ctx.getModelProxy());
        return List.of(parsedElement);
    }



    private String removeChildElementsTextContent(String textContent, HtmlElement el) {
        for (DomElement childElement : el.getChildElements()) {
            textContent = textContent.replace(childElement.getTextContent(), "");
        }
        return textContent;
    }


    public ParseElementText setRemoveChildElementsTextContent(boolean removeChildElementsTextContent) {
        this.removeChildElementsTextContent = removeChildElementsTextContent;
        return this;
    }

    @Override
    public <T> ParseElementText thenCollect(BiConsumer<T, String> modelMutation) {
        this.modelMutation = (BiConsumer<Object, String>) modelMutation;
        return this;
    }

    @Override
    public ParseElementText then(HtmlUnitParsingStep nextStep) {
        this.nextSteps.add(nextStep);
        return this;
    }


}
