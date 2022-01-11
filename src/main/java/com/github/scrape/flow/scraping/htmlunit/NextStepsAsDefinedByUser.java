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

import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ScrapingServices;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Runs the steps the way they were defined by the user (in terms of order, exclusiveness etc.)
 */
public class NextStepsAsDefinedByUser implements NextStepsHandler {

    @Override
    public List<StepOrder> execute(List<HtmlUnitScrapingStep<?>> nextSteps,
                                   ScrapingContext nextCtx,
                                   ScrapingServices services) {
        return nextSteps.stream()
                .map(step -> step.execute(nextCtx, services))
                .collect(Collectors.toList());
    }

}
