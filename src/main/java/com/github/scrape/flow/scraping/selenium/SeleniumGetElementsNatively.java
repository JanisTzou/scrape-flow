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
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Maps nodes acquired in the previous steps to other nodes ... e.g. children/parents/siblings etc ...
 */
@Log4j2
public class SeleniumGetElementsNatively extends SeleniumScrapingStep<SeleniumGetElementsNatively> {

    private final Function<WebElement, Optional<WebElement>> mapper;

    SeleniumGetElementsNatively(Function<WebElement, Optional<WebElement>> mapper) {
        this.mapper = mapper;
    }

    @Override
    protected SeleniumGetElementsNatively copy() {
        return copyFieldValuesTo(new SeleniumGetElementsNatively(this.mapper));
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<WebElement>> elementSearch = () -> {
                Optional<WebElement> mapped = mapper.apply(ctx.getWebElement());
                if (mapped.isPresent()) {
                    log.debug("{} element mapped successfully from {} to {}", getName(), ctx.getWebElement(), mapped.get());
                    return List.of(mapped.get());
                } else {
                    log.debug("{} element could not be mapped from {} to other element", getName(), ctx.getWebElement());
                }
                return Collections.emptyList();
            };

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
