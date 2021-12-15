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

package com.github.web.scraping.lib.dom.data.parsing;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HtmlUnitParsingStepIteratedChildByFullXPath extends HtmlUnitParsingStep {

    private final Enum<?> dataType;

    // the xPath of the first child
    private final String xPath;

    // for each iterated element these strategies will be applied to parse data ...
    private final List<HtmlUnitParsingStep> nextSteps;

    /*
    example:
    // /html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]
    // /html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[2]
    // /html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/div/div[1]/span[1]
     */

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<ParsedElement> parse(DomNode parentElement) {

        // figure out the diff between this.xPath and the parent element xPath ... then use that

        String parentXPath = parentElement.getCanonicalXPath();
        String parentBaseXPath = XPathUtils.getXPathSubstrHead(parentXPath, 1);
        // the part of the child's xpath that will be the same through all the parents
        Optional<String> xPathDiff = XPathUtils.getXPathDiff(parentBaseXPath, xPath);
        if (xPathDiff.isEmpty()) {
            return Collections.emptyList();
        }
        String childStaticPartXPath = XPathUtils.getXPathSubstrTailFromStart(xPathDiff.get(), 1);

        String childXPath = XPathUtils.concat(parentXPath, childStaticPartXPath);

        List<ParsedElement> parsedElements = parentElement.getByXPath(childXPath).stream()
                .map(el -> {
                    String href = null;
                    String tc = null;
                    if (el instanceof HtmlAnchor anch) {
                        href = anch.getHrefAttribute();
                    }
                    if (el instanceof HtmlElement htmlEl) {
                        tc = htmlEl.getTextContent();
                        if (tc != null) {
                            // this should be optional ... used in cases when child elements' content filthies the parent element's content ...
                            tc = removeNestedElementsTextContent(tc, htmlEl);
                            tc = tc.trim();
                        }
                    }
                    if (href != null || tc != null) {
                        return new ParsedElement(dataType, href, tc, el);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(parsedEl -> {
                    // TODO are these really children? Might not be at all ... hamdle different levels here ...
                    Stream<ParsedElement> childParsedEls = nextSteps.stream()
                            .flatMap(s -> s.parse((DomNode) parsedEl.getElement()).stream());
                    return Stream.concat(Stream.of(parsedEl) , childParsedEls);
                })
                .collect(Collectors.toList());


        return parsedElements;
    }

    private String removeNestedElementsTextContent(String textContent, HtmlElement el) {
        for (DomElement childElement : el.getChildElements()) {
            textContent = textContent.replace(childElement.getTextContent(), "");
        }
        return textContent;
    }

    public static class Builder {

        private Enum<?> identifier;
        private String xPath;
        private final List<HtmlUnitParsingStep> nextSteps = new ArrayList<>();

        public Builder setIdentifier(Enum<?> identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder setxPath(String xPath) {
            this.xPath = xPath;
            return this;
        }

        public Builder addNextStep(HtmlUnitParsingStep parsingStep) {
            this.nextSteps.add(parsingStep);
            return this;
        }

        public HtmlUnitParsingStepIteratedChildByFullXPath build() {
            return new HtmlUnitParsingStepIteratedChildByFullXPath(identifier, xPath, nextSteps);
        }
    }

}
