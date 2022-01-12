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

package com.github.scrape.flow.scraping;

import com.github.scrape.flow.debugging.DebuggingOptions;
import com.github.scrape.flow.execution.StepOrder;

import java.util.List;

/**
 * used internally to perform package private operations on a step
 * ... to keep the public api of the step implementations clean of internal stuff ...
 */
public class ScrapingStepInternalProxy<C extends ScrapingStepBase<C>> {

    private final C step;

    private ScrapingStepInternalProxy(C step) {
        this.step = step;
    }

    public static ScrapingStepInternalProxy<?> of(ScrapingStepBase<?> step) {
        return new ScrapingStepInternalProxy(step);
    }

    public StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        return step.execute(ctx, services);

    }

    public C copy() {
        return step.copy();
    }

    public List<ScrapingStepBase<?>> getNextSteps() {
        return step.getNextSteps();
    }

    public StackTraceElement getStepDeclarationLine() {
        return step.getStepDeclarationLine();
    }

    public DebuggingOptions getStepDebugging() {
        return step.getStepDebugging();
    }

}
