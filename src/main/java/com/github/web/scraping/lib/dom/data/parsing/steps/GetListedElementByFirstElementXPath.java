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
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.dom.data.parsing.XPathUtils;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetListedElementByFirstElementXPath extends HtmlUnitParsingStep {

    private final Enum<?> dataType;

    // the xPath of the first child
    private final String xPath;

    // for each iterated element these strategies will be applied to execute data ...
    private final List<HtmlUnitParsingStep> nextSteps;

    /*
    example:
    // /html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]
    // /html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[2]
    // /html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/div/div[1]/span[1]
     */

    public static GetListedElementByFirstElementXPath.Builder builder(Enum<?> identifier, String xPath) {
        return new GetListedElementByFirstElementXPath.Builder().setIdentifier(identifier).setxPath(xPath);
    }

    public static GetListedElementByFirstElementXPath.Builder builder(String xPath) {
        return new GetListedElementByFirstElementXPath.Builder().setxPath(xPath);
    }


    @Override
    public List<StepResult> execute(ParsingContext ctx) {

        // figure out the diff between this.xPath and the parent element xPath ... then use that

        String parentXPath = ctx.getNode().getCanonicalXPath();
        String parentBaseXPath = XPathUtils.getXPathSubstrHead(parentXPath, 1);
        // the part of the child's xpath that will be the same through all the parents
        Optional<String> xPathDiff = XPathUtils.getXPathDiff(parentBaseXPath, xPath);
        if (xPathDiff.isEmpty()) {
            return Collections.emptyList();
        }
        String childStaticPartXPath = XPathUtils.getXPathSubstrTailFromStart(xPathDiff.get(), 1);

        String childXPath = XPathUtils.concat(parentXPath, childStaticPartXPath);

        List<StepResult> parsedElements = ctx.getNode().getByXPath(childXPath).stream()
                .filter(o -> o instanceof DomNode)
                .flatMap(parsedEl -> {
                    // TODO are these really children? Might not be at all ... hamdle different levels here ...
                    return nextSteps.stream()
                            .flatMap(s -> s.execute(new ParsingContext ((DomNode) parsedEl, null, null, false)).stream());
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

        public Builder then(HtmlUnitParsingStep parsingStep) {
            this.nextSteps.add(parsingStep);
            return this;
        }

        public GetListedElementByFirstElementXPath build() {
            return new GetListedElementByFirstElementXPath(identifier, xPath, nextSteps);
        }
    }

}
