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

import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitStepBlock;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Runs the steps wrapped under one exclusive block. Useful in situations where we need to ensure that these steps run first before
 * e.g. the pagination step can proceed
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class NextStepsWrappedInOneExclusiveBlock implements NextStepsHandler {

    private final ExclusiveBlockFactory exclusiveBlockFactory;

    public NextStepsWrappedInOneExclusiveBlock() {
        this(new ExclusiveBlockFactory());
    }

    @Override
    public SpawnedSteps execute(List<ScrapingStep<?>> nextSteps, StepOrder currStepOrder,
                                ScrapingContext nextCtx,
                                ScrapingServices services) {
        StepOrder stepOrder = exclusiveBlockFactory.wrapInExclusiveBlock(nextSteps).execute(nextCtx, services);
        return new SpawnedSteps(currStepOrder, stepOrder);
    }

    static class ExclusiveBlockFactory {
        HtmlUnitStepBlock wrapInExclusiveBlock(List<ScrapingStep<?>> nextSteps) {
            return new HtmlUnitStepBlock(nextSteps).setExclusiveExecution(true);
        }
    }

}
