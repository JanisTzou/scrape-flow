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

package com.github.web.scraping.lib.dom.data.parsing.steps.pipe;

import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;

import java.util.List;

public class HtmlUnitChainableStepPipe extends HtmlUnitParsingStepPipe {

    protected final HtmlUnitParsingStepPipe nextStep;
    private final HtmlUnitParsingStepPipe prevStep;

    public HtmlUnitChainableStepPipe(HtmlUnitParsingStepPipe prevStep, HtmlUnitParsingStepPipe nextStep) {
        super(prevStep);
        this.nextStep = nextStep;
        this.prevStep = prevStep;
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        List<StepResult> stepResults = prevStep.execute(ctx);
        // Hmmmmmm
        return nextStep.execute(ctx);
    }

}
