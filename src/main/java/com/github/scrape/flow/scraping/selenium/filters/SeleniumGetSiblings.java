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

package com.github.scrape.flow.scraping.selenium.filters;

import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.*;
import com.github.scrape.flow.scraping.selenium.SeleniumScrapingStep;
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

    private static final List<Class<?>> PREV_SIBLINGS_FILTER_CLASSES = List.of(
            FilterSiblingsPrevN.class,
            FilterSiblingsPrevNth.class,
            FilterSiblingsPrevEveryNth.class,
            FilterSiblingsFirst.class
    );

    private static final List<Class<?>> NEXT_SIBLINGS_FILTER_CLASSES = List.of(
            FilterSiblingsNextN.class,
            FilterSiblingsNextNth.class,
            FilterSiblingsNextEveryNth.class,
            FilterSiblingsLast.class
    );

    private static final List<Class<?>> ALL_SIBLINGS_FILTER_CLASSES = List.of(
            FilterSiblingsAll.class
    );

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
            Supplier<List<WebElement>> elementSearch = () -> getEligibleSiblings(ctx);
            getHelper().execute(elementSearch, ctx, stepOrder, services);
        };

        submitForExecution(stepOrder, runnable, services);

        return stepOrder;
    }

    private List<WebElement> getEligibleSiblings(ScrapingContext ctx) {
        throw new UnsupportedOperationException("Not implemented");
//        if (filters.stream().anyMatch(f -> PREV_SIBLINGS_FILTER_CLASSES.contains(f.getClass()))) {
//            return HtmlUnitUtils.findPrevSiblingElements(ctx.getWebElement());
//        } else if (filters.stream().anyMatch(f -> NEXT_SIBLINGS_FILTER_CLASSES.contains(f.getClass()))) {
//            return HtmlUnitUtils.findNextSiblingElements(ctx.getWebElement());
//        } else if (filters.stream().anyMatch(f -> ALL_SIBLINGS_FILTER_CLASSES.contains(f.getClass()))) {
//            return HtmlUnitUtils.findAllSiblingElements(ctx.getWebElement());
//        } else {
//            return HtmlUnitUtils.findAllSiblingElements(ctx.getWebElement());
//        }
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
