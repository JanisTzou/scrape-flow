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

import com.github.scrape.flow.scraping.*;
import com.github.scrape.flow.scraping.filters.Filter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Log4j2
public abstract class SeleniumScrapingStep<C extends SeleniumScrapingStep<C>>
        extends CommonOperationsStepBase<C>
        implements Throttling {

    @Getter
    protected final List<Filter<WebElement>> filters = new CopyOnWriteArrayList<>();

    public SeleniumScrapingStep() {
    }

    public SeleniumScrapingStep(List<ScrapingStepBase<?>> nextSteps) {
        super(nextSteps);
    }

    protected SeleniumStepHelper getHelper() {
        return new SeleniumStepHelper(this);
    }

    protected SeleniumStepHelper getHelper(NextStepsHandler nextStepsHandler) {
        return new SeleniumStepHelper(this, nextStepsHandler);
    }

    protected C addFilter(Filter<WebElement> filter) {
        return copyModifyAndGet(copy -> {
            copy.filters.add(filter);
            return copy;
        });
    }

    protected List<Filter<WebElement>> getFilters() {
        return filters;
    }

    @SuppressWarnings("unchecked")
    protected C copyFieldValuesTo(SeleniumScrapingStep<?> other) {
        super.copyFieldValuesTo(other);
        other.filters.addAll(this.filters);
        return (C) other;
    }

    @Override
    protected ScrapingType getScrapingType() {
        return ScrapingType.SELENIUM;
    }


}
