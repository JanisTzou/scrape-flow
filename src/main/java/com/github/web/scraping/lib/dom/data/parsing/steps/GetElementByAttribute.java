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
import com.github.web.scraping.lib.dom.data.parsing.ParsingStepResult;
import com.github.web.scraping.lib.scraping.utils.HtmlUnitUtils;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// TODO maybe we should only have the version "by attribute" which would also support value ?
@RequiredArgsConstructor
public class GetElementByAttribute extends HtmlUnitParsingStep {

    private final String attributeName;
    private final String attributeValue;

    // for each iterated element these strategies will be applied to execute data ...
    private final List<HtmlUnitParsingStep> nextSteps;

    public static Builder builder(String attributeName, String attributeValue) {
        return new Builder(attributeName, attributeValue);
    }

    public static Builder builder(String attributeName) {
        return new Builder(attributeName, null);
    }

    @Override
    public List<ParsingStepResult> execute(DomNode domNode) {
        List<DomNode> nodes;
        if (attributeValue != null) {
            nodes = HtmlUnitUtils.getAllChildElementsByAttributeValue(domNode, attributeName, attributeValue, true);
        } else {
            nodes = HtmlUnitUtils.getAllChildElementsByAttribute(domNode, attributeName);
        }
        return nodes.stream()
                .flatMap(node ->
                    nextSteps.stream().flatMap(s -> s.execute(node).stream())
                )
                .collect(Collectors.toList());
    }


    public static class Builder {

        private String attributeName;
        private String attributeValue;
        private List<HtmlUnitParsingStep> nextSteps = new ArrayList<>();

        Builder(String attributeName, String attributeValue) {
            this.attributeName = attributeName;
            this.attributeValue = attributeValue;
        }

//        public Builder setAttributeName(String attributeName) {
//            this.attributeName = attributeName;
//            return this;
//        }
//
//        public Builder setAttributeValue(String attributeValue) {
//            this.attributeValue = attributeValue;
//            return this;
//        }

        public Builder then(HtmlUnitParsingStep nextStep) {
            this.nextSteps.add(nextStep);
            return this;
        }

        public GetElementByAttribute build() {
            return new GetElementByAttribute(attributeName, attributeValue, nextSteps);
        }
    }

}
