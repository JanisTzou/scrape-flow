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
import com.github.scrape.flow.scraping.LoadingNewPage;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Log4j2
public class SeleniumPeek extends SeleniumScrapingStep<SeleniumPeek> {

    private final Consumer<WebElement> consumer;

    SeleniumPeek(Consumer<WebElement> consumer) {
        this.consumer = consumer;
    }

    @Override
    protected SeleniumPeek copy() {
        return copyFieldValuesTo(new SeleniumPeek(consumer));
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            consumer.accept(ctx.getWebElement());
            Supplier<List<WebElement>> elementSearch = () -> ctx.getWebElement() != null ? List.of(ctx.getWebElement()) : Collections.emptyList();
            getHelper().execute(elementSearch, ctx, stepOrder, services);
        };

        submitForExecution(stepOrder, runnable, services);

        return stepOrder;
    }


    @Override
    protected boolean throttlingAllowed() {
        return false;
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }

}
