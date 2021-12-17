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
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetElementsByXPath extends HtmlUnitParsingStep {

    private final Enum<?> dataType;
    private final String xPath;
    private final List<HtmlUnitParsingStep> nextSteps;

    public static Builder builder(String xPath) {
        return new Builder().setxPath(xPath);
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        return ctx.getNode().getByXPath(xPath).stream()
                .filter(o -> o instanceof DomNode)
                .flatMap(node ->
                        nextSteps.stream().flatMap(s -> s.execute(new ParsingContext ((DomNode) node, null, null, false)).stream())
                )
                .collect(Collectors.toList());
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

        public Builder then(HtmlUnitParsingStep nextStep) {
            this.nextSteps.add(nextStep);
            return this;
        }

        public GetElementsByXPath build() {
            return new GetElementsByXPath(identifier, xPath, nextSteps);
        }
    }

}
