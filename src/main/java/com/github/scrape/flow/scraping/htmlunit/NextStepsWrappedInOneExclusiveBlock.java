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

package com.github.scrape.flow.scraping.htmlunit;

import com.github.scrape.flow.parallelism.StepExecOrder;
import com.github.scrape.flow.scraping.ScrapingServices;

import java.util.List;

/**
 * Runs the steps wrapped under one exclusive block. Useful in situations where we need to ensure that these steps run first before
 * e.g. the pagination step can proceed
 */
public class NextStepsWrappedInOneExclusiveBlock implements NextStepsHandler {

    @Override
    public List<StepExecOrder> execute(List<HtmlUnitScrapingStep<?>> nextSteps,
                                       ScrapingContext nextCtx,
                                       ScrapingServices services) {
        StepExecOrder execOrder = new StepBlock(nextSteps).setExclusiveExecution(true).execute(nextCtx, services);
        return List.of(execOrder);
    }

}
