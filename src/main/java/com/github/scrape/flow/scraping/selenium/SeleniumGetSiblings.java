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
import com.github.scrape.flow.scraping.*;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterableByAttribute;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterableByCssClass;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterableByTag;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterableByTextContent;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class SeleniumGetSiblings extends SeleniumScrapingStep<SeleniumGetSiblings>
        implements SeleniumFilterableByAttribute<SeleniumGetSiblings>,
        SeleniumFilterableByTag<SeleniumGetSiblings>,
        SeleniumFilterableByTextContent<SeleniumGetSiblings>,
        SeleniumFilterableByCssClass<SeleniumGetSiblings>,
        FilterableSiblings<SeleniumGetSiblings, WebElement> {

    SeleniumGetSiblings() {
    }

    @Override
    protected SeleniumGetSiblings copy() {
        return copyFieldValuesTo(new SeleniumGetSiblings());
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<WebElement>> nodesSearch = () -> getEligibleSiblings(ctx);
            getHelper().execute(nodesSearch, ctx, stepOrder, services);
        };

        submitForExecution(stepOrder, runnable, services);

        return stepOrder;
    }

    private List<WebElement> getEligibleSiblings(ScrapingContext ctx) {
        if (filters.stream().anyMatch(f -> SiblingFilters.PREV_SIBLINGS_FILTER_CLASSES.contains(f.getClass()))) {
            return SeleniumUtils.findPrevSiblingElements(ctx.getWebElement());
        } else if (filters.stream().anyMatch(f -> SiblingFilters.NEXT_SIBLINGS_FILTER_CLASSES.contains(f.getClass()))) {
            return SeleniumUtils.findNextSiblingElements(ctx.getWebElement());
        } else {
            return SeleniumUtils.findAllSiblingElements(ctx.getWebElement());
        }
    }


    @Override
    public SeleniumGetSiblings addFilter(Filter<WebElement> filter) {
        return super.addFilter(filter);
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }


}
