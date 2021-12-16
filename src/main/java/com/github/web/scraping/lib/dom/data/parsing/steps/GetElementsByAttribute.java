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
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.scraping.utils.HtmlUnitUtils;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetElementsByAttribute extends HtmlUnitParsingStep {

    private final String attributeName;
    private final String attributeValue;
    private final boolean matchEntireValue;

    // for each iterated element these strategies will be applied to execute data ...
    private final List<HtmlUnitParsingStep> nextSteps;

    public static Builder builder(String attributeName, String attributeValue) {
        return new Builder(attributeName, attributeValue);
    }

    public static Builder builder(String attributeName) {
        return new Builder(attributeName, null);
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        List<DomNode> nodes;
        if (attributeValue != null) {
            nodes = HtmlUnitUtils.getAllChildElementsByAttributeValue(ctx.getNode(), attributeName, attributeValue, this.matchEntireValue);
        } else {
            nodes = HtmlUnitUtils.getAllChildElementsByAttribute(ctx.getNode(), attributeName);
        }
        return nodes.stream()
                .flatMap(node ->
                    nextSteps.stream().flatMap(s -> s.execute(new ParsingContext(node, null, null)).stream())
                )
                .collect(Collectors.toList());
    }


    public static class Builder {

        private String attributeName;
        private String attributeValue;
        private boolean matchEntireValue = true;
        private List<HtmlUnitParsingStep> nextSteps = new ArrayList<>();

        Builder(String attributeName, String attributeValue) {
            this.attributeName = attributeName;
            this.attributeValue = attributeValue;
        }

        public Builder setMatchEntireValue(boolean matchEntireValue) {
            this.matchEntireValue = matchEntireValue;
            return this;
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

        public GetElementsByAttribute build() {
            return new GetElementsByAttribute(attributeName, attributeValue, matchEntireValue, nextSteps);
        }
    }

}
