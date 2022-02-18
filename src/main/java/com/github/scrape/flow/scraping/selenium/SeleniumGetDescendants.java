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

import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.Filter;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterable;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterableByCommonCriteria;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.Supplier;

public class SeleniumGetDescendants extends SeleniumScrapingStep<SeleniumGetDescendants>
        implements SeleniumFilterableByCommonCriteria<SeleniumGetDescendants>, SeleniumFilterable<SeleniumGetDescendants> {


    SeleniumGetDescendants() {
    }

    @Override
    protected SeleniumGetDescendants copy() {
        return copyFieldValuesTo(new SeleniumGetDescendants());
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<WebElement>> nodesSearch = () -> ctx.getWebElement().findElements(By.xpath(".//*"));
            getHelper().execute(ctx, ctx.getDriverNo(), nodesSearch, stepOrder, getExecuteIf(), services);
        };

        submitForExecution(stepOrder, runnable, services.getTaskService(), services.getSeleniumDriversManager());

        return stepOrder;
    }

    @Override
    public SeleniumGetDescendants addFilter(Filter<WebElement> filter) {
        return super.addFilter(filter);
    }

}
