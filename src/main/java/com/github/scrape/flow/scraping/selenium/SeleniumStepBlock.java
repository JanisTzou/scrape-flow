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

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.*;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitScrapingStep;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Used just to test context propagation
 */
public class SeleniumStepBlock extends SeleniumScrapingStep<SeleniumStepBlock>
        implements ChainedStep<SeleniumStepBlock>, CollectingStep<SeleniumStepBlock> {

    public SeleniumStepBlock(@Nullable List<ScrapingStep<?>> nextSteps) {
        super(nextSteps);
    }

    SeleniumStepBlock() {
        this(null);
    }

    @Override
    protected SeleniumStepBlock copy() {
        return copyFieldValuesTo(new SeleniumStepBlock());
    }

    @Override
    public StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<WebElement>> nodesSearch = () -> List.of(ctx.getWebElement());
            getHelper().execute(nodesSearch, ctx, stepOrder, services);
        };

        submitForExecution(stepOrder, runnable, services);

        return stepOrder;
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }

}
