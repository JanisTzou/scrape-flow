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

package com.github.scrape.flow.scraping.selenium;

import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SeleniumGetAncestor extends SeleniumScrapingStep<SeleniumGetAncestor> {
    // TODO make possible to use general filters (by tag, class, attr ...)

    private final int param;

    SeleniumGetAncestor(int param) {
        this.param = param;
    }

    @Override
    protected SeleniumGetAncestor copy() {
        return copyFieldValuesTo(new SeleniumGetAncestor(param));
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<WebElement>> elementSearch = () -> SeleniumUtils.findNthAncestor(ctx.getWebElement(), param).stream().collect(Collectors.toList());
            getHelper().execute(elementSearch, ctx, stepOrder, services);
        };

        submitForExecution(stepOrder, runnable, services);

        return stepOrder;
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }

}
