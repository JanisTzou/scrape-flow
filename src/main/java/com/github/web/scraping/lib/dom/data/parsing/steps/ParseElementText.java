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
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;


// TODO ... in this version we only execute the content of the element ...

//@RequiredArgsConstructor
public class ParseElementText extends HtmlUnitParsingStep {

    @Deprecated // TODO remove ...
    private Enum<?> identifier;

    private boolean removeChildElementsTextContent;
    private BiConsumer<Object, String> setter;
    private final List<HtmlUnitParsingStep> nextSteps = new ArrayList<>();

    public ParseElementText() {
    }

    public ParseElementText(Enum<?> identifier, boolean removeChildElementsTextContent, BiConsumer<?, String> setter) {
        this.identifier = identifier;
        this.removeChildElementsTextContent = removeChildElementsTextContent;
        this.setter = (BiConsumer<Object, String>) setter;
    }

    public static Builder instance(Enum<?> identifier) {
        return new Builder().setId(identifier);
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

        if (setter != null && ctx.getModel() != null) {
            setter.accept(ctx.getModel(), tc);
        }
        ParsedElement parsedElement = new ParsedElement(identifier, null, tc, false, ctx.getNode());
        parsedElement.setModel(ctx.getModel());
        return List.of(parsedElement);
    }

    private String removeChildElementsTextContent(String textContent, HtmlElement el) {
        for (DomElement childElement : el.getChildElements()) {
            textContent = textContent.replace(childElement.getTextContent(), "");
        }
        return textContent;
    }


    public ParseElementText setId(Enum<?> identifier) {
        this.identifier = identifier;
        return this;
    }

    public ParseElementText setRemoveChildElementsTextContent(boolean removeChildElementsTextContent) {
        this.removeChildElementsTextContent = removeChildElementsTextContent;
        return this;
    }

    // TODO should this support next operations? Maybe yes ... in case of dynamic searches (by previously scraped value...) ... but not sure how to utilize this ...
    public ParseElementText then(HtmlUnitParsingStep nextStep) {
        this.nextSteps.add(nextStep);
        return this;
    }

    public <T> ParseElementText collectToModel(BiConsumer<T, String> setter) {
        this.setter = (BiConsumer<Object, String>) setter;
        return this;
    }



    public static class Builder {

        private Enum<?> identifier;
        private boolean removeChildElementsTextContent = true;
        private BiConsumer<?, String> collectionOp;

        // TODO use this one as well ...
        private final List<HtmlUnitParsingStep> nextSteps = new ArrayList<>();

        public Builder setId(Enum<?> identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder setRemoveChildElementsTextContent(boolean removeChildElementsTextContent) {
            this.removeChildElementsTextContent = removeChildElementsTextContent;
            return this;
        }

        // TODO should this support next operations? Maybe yes ... in case of dynamic searches (by previously scraped value...) ... but not sure how to utilize this ...
        public Builder then(HtmlUnitParsingStep nextStep) {
            this.nextSteps.add(nextStep);
            return this;
        }

        public <T> Builder collectToModel(BiConsumer<T, String> collectionOp) {
            this.collectionOp = collectionOp;
            return this;
        }

        public ParseElementText build() {
            return new ParseElementText(identifier, removeChildElementsTextContent, collectionOp);
        }
    }

}
