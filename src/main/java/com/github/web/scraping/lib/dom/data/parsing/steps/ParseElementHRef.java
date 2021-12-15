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
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsedElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsingStepResult;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ParseElementHRef extends HtmlUnitParsingStep {

    private final Enum<?> dataType;
    private final String xpath;
    // TODO support next strategies ...

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Enum<?> identifier) {
        return new Builder().setId(identifier);
    }

    @Override
    public List<ParsingStepResult> execute(DomNode domNode) {
        if (domNode instanceof HtmlAnchor anch) {
            String href = anch.getHrefAttribute();
            if (href != null) {
                return List.of(new ParsedElement(dataType, href, null, domNode));
            }
        }
        return Collections.emptyList();
    }

    private String removeNestedElementsTextContent(String textContent, HtmlElement el) {
        for (DomElement childElement : el.getChildElements()) {
            textContent = textContent.replace(childElement.getTextContent(), "");
        }
        return textContent;
    }


    public static class Builder {

        // TODO use this one as well ...
        private final List<HtmlUnitParsingStep> nextSteps = new ArrayList<>();
        private Enum<?> identifier;
        private String xPath;

        public Builder setId(Enum<?> identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder setxPath(String xPath) {
            this.xPath = xPath;
            return this;
        }

        public Builder then(HtmlUnitParsingStep nextStep) {
            this.nextSteps.add(nextStep);
            return this;
        }

        public ParseElementHRef build() {
            return new ParseElementHRef(identifier, xPath);
        }
    }

}
