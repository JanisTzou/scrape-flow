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
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.dom.data.parsing.XPathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GetListedElementsByFirstElementXPath extends HtmlUnitParsingStep {

    private final Enum<?> identifier;
    private final String xPath;

    // for each iterated element these strategies will be applied to execute data ...
    private final List<HtmlUnitParsingStep> nextSteps;

    // TODO optionally specify pagination strategy ? that will be called at the end ?

    public GetListedElementsByFirstElementXPath(Enum<?> identifier,
                                                String xPath,
                                                List<HtmlUnitParsingStep> nextSteps) {
        this.identifier = identifier;
        this.xPath = xPath;
        this.nextSteps = nextSteps;
    }

    public static Builder builder(Enum<?> identifier, String xPath) {
        return new Builder().setIdentifier(identifier).setxPath(xPath);
    }

    public static Builder builder(String xPath) {
        return new Builder().setxPath(xPath);
    }

    @Override
    public List<StepResult> execute(DomNode domNode) {

        // TODO improve working with XPath ...
        String parentXPath = XPathUtils.getXPathSubstrHead(xPath, 1);
        String xPathTail = XPathUtils.getXPathSubstrTail(xPath, 1).replaceAll("\\d+", "\\\\d+");
        String pattern = XPathUtils.regexEscape(XPathUtils.concat(parentXPath, xPathTail));

        return domNode.getByXPath(parentXPath)
                .stream()
                .flatMap(el -> {
                    // child elements ...
                    if (el instanceof HtmlElement htmlEl) {
                        return StreamSupport.stream(htmlEl.getChildElements().spliterator(), false);
                    }
                    return Stream.empty();
                })
                .filter(el -> {
                    if (el instanceof HtmlElement htmlEl) {
                        String xPath = htmlEl.getCanonicalXPath();
                        boolean matches = xPath.matches(pattern);
//                        if (matches) {
//                            System.out.println("Matched listing xPath = " + xPath);
//                        } else {
//                            System.out.println("Unmatched listing xPath = " + xPath);
//                        }
                        return matches;
                    }
                    return false;
                })
                .flatMap(el -> {
                    if (el instanceof HtmlElement htmlEl) {
                        return nextSteps.stream().flatMap(s -> s.execute(htmlEl).stream());
                    }
                    return Stream.empty();
                })
                .collect(Collectors.toList());


        // here we want to identify all the elements that will then be processed by the strategies above?
        // ... so we can for example apply specific HtmlUnitParsingStrategyByFullXPath on each one of them ... BUT the expaths will need to be dynamic as the root will change for each listed item ....

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

        // TODO only allow steps that operate on element collections ...
        public Builder then(HtmlUnitParsingStep nextStep) {
            this.nextSteps.add(nextStep);
            return this;
        }

        public GetListedElementsByFirstElementXPath build() {
            return new GetListedElementsByFirstElementXPath(identifier, xPath, nextSteps);
        }
    }

}
