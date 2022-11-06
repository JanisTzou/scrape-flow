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
import com.github.scrape.flow.scraping.Filter;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterableByCommonCriteria;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.Supplier;

public class SeleniumGetDescendantsByCssSelector extends SeleniumScrapingStep<SeleniumGetDescendantsByCssSelector>
        implements SeleniumFilterableByCommonCriteria<SeleniumGetDescendantsByCssSelector> {

    // this cannot be a filter ... it's more of a "Get" operation ...
    // it would not make sense to first get descendants/children and then this ...

    private final String sccSelector;

    SeleniumGetDescendantsByCssSelector(String sccSelector) {
        this.sccSelector = sccSelector;
    }

    @Override
    protected SeleniumGetDescendantsByCssSelector copy() {
        return copyFieldValuesTo(new SeleniumGetDescendantsByCssSelector(sccSelector));
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<WebElement>> elementSearch = () -> SeleniumUtils.getDescendantsBySccSelector(ctx.getWebElement(), sccSelector);
            getHelper().execute(elementSearch, ctx, stepOrder, services);
        };

        submitForExecution(stepOrder, runnable, services);

        return stepOrder;
    }

    @Override
    public SeleniumGetDescendantsByCssSelector addFilter(Filter<WebElement> filter) {
        return super.addFilter(filter);
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }

}
