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

import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.debugging.DebuggingOptions;
import com.github.scrape.flow.execution.StepOrder;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * used internally to perform package private operations on a step
 * ... to keep the public api of the step implementations clean of internal stuff ...
 */
@RequiredArgsConstructor
public class ScrapingStepInternalReader<C extends ScrapingStep<C>> {

    private final C step;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static ScrapingStepInternalReader<?> of(ScrapingStep<?> step) {
        return new ScrapingStepInternalReader(step);
    }

    public StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        return step.execute(ctx, services);

    }

    public C copy() {
        return step.copy();
    }

    public List<ScrapingStep<?>> getNextSteps() {
        return step.getNextSteps();
    }

    public StepExecutionCondition getExecuteIf() {
        return step.getExecuteIf();
    }

    public DebuggingOptions getStepDebugging() {
        return step.getStepDebugging();
    }

    public ClientType getClientType() {
        return step.getClientType();
    }

    public ClientReservationType getClientReservationType() {
        return step.getClientReservationType();
    }

}
