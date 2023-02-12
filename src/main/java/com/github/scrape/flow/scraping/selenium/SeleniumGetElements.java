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

import com.github.scrape.flow.clients.ClientOperator;
import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.Filter;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterable;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterableByCommonCriteria;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class SeleniumGetElements extends SeleniumScrapingStep<SeleniumGetElements>
        implements SeleniumFilterableByCommonCriteria<SeleniumGetElements>, SeleniumFilterable<SeleniumGetElements> {


    SeleniumGetElements() {
    }

    @Override
    protected SeleniumGetElements copy() {
        return copyFieldValuesTo(new SeleniumGetElements());
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<WebElement>> elementsSearch = () -> {
                Optional<ClientOperator<WebDriver>> operator = services.getClientAccessManager().getSeleniumClient(stepOrder);
                WebElement htmlElement;
                if (operator.isPresent()) {
                    WebDriver client = operator.get().getClient();
                    htmlElement = client.findElement(By.tagName("html"));
                    if (filters.isEmpty()) {
                        return htmlElement.findElements(By.xpath(".//*"));
                    }
                    else {
                        return SeleniumDescendantFiltering.getElementsByFilters(htmlElement, filters);
                    }
                } else {
                    throw new IllegalStateException("No client!");
                }
            };
            getHelper().execute(elementsSearch, ctx, stepOrder, services);
        };

        submitForExecution(stepOrder, runnable, services);

        return stepOrder;
    }

    @Override
    public SeleniumGetElements addFilter(Filter<WebElement> filter) {
        return super.addFilter(filter);
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }

}
