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
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsedElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsingStepResult;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;


// TODO ... in this version we only execute the content of the element ...

@RequiredArgsConstructor
public class ParseElementText extends HtmlUnitParsingStep {

    private final Enum<?> identifier;

    private final boolean removeChildElementsTextContent;

    // TODO support next strategies ...

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Enum<?> identifier) {
        return new Builder().setId(identifier);
    }

    // TODO for text content extraction we could have a dedicated helper class ...
    @Override
    public List<ParsingStepResult> execute(DomNode el) {
        String tc = null;
        if (el instanceof HtmlElement htmlEl) {
            tc = htmlEl.getTextContent();
            if (tc != null) {
                // this should be optional ... used in cases when child elements' content filthies the parent element's content ...
                if (this.removeChildElementsTextContent) {
                    tc = removeChildElementsTextContent(tc, htmlEl);
                }
                tc = tc.trim();
            }
        }
        return List.of(new ParsedElement(identifier, null, tc, el));
    }

    private String removeChildElementsTextContent(String textContent, HtmlElement el) {
        for (DomElement childElement : el.getChildElements()) {
            textContent = textContent.replace(childElement.getTextContent(), "");
        }
        return textContent;
    }


    public static class Builder {

        private Enum<?> identifier;
        private boolean removeChildElementsTextContent = true;

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

        public ParseElementText build() {
            return new ParseElementText(identifier, removeChildElementsTextContent);
        }
    }

}
