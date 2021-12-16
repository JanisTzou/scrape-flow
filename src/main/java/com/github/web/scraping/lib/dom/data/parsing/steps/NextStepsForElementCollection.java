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
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

// TODO does it make sense to have this asa separate super class?
@RequiredArgsConstructor
public class NextStepsForElementCollection {

    private final List<HtmlUnitParsingStep> nextSteps;

    public List<StepResult> execute(DomNode domNode, StepResult prevStepResult) {
        return null;
    }

    public List<StepResult> execute(DomNode domNode, @Nullable Object resultCollector) {
        return Collections.emptyList();
    }

}
