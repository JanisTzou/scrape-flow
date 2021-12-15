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

@RequiredArgsConstructor
public class GetElementByCssClass extends HtmlUnitParsingStep {

    private final String cssClassName;

    // for each iterated element these strategies will be applied to execute data ...
    private final List<HtmlUnitParsingStep> nextSteps;

    public static Builder builder(String cssClassName) {
        return new Builder(cssClassName);
    }

    @Override
    public List<ParsingStepResult> execute(DomNode domNode) {
        return HtmlUnitUtils.getAllChildElementsByClass(domNode, cssClassName)
                .stream()
                .flatMap(node ->
                    nextSteps.stream().flatMap(s -> s.execute(node).stream())
                )
                .collect(Collectors.toList());
    }


    public static class Builder {

        private String cssClassName;
        private List<HtmlUnitParsingStep> nextSteps = new ArrayList<>();

        Builder(String cssClassName) {
            this.cssClassName = cssClassName;
        }


        public Builder then(HtmlUnitParsingStep nextStep) {
            this.nextSteps.add(nextStep);
            return this;
        }

        public GetElementByCssClass build() {
            return new GetElementByCssClass(cssClassName, nextSteps);
        }
    }

}
