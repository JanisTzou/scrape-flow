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

package com.github.scrape.flow.scraping.htmlunit;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.scrape.flow.scraping.*;
import com.github.scrape.flow.scraping.filters.Filter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Log4j2
public abstract class HtmlUnitScrapingStep<C extends HtmlUnitScrapingStep<C>>
        extends CommonOperationsStepBase<C>
        implements Throttling {

    protected final List<Filter<DomNode>> filters = new CopyOnWriteArrayList<>();

    public HtmlUnitScrapingStep() {
    }

    public HtmlUnitScrapingStep(List<ScrapingStepBase<?>> nextSteps) {
        super(nextSteps);
    }

    protected HtmlUnitStepHelper getHelper() {
        return new HtmlUnitStepHelper(this);
    }

    protected HtmlUnitStepHelper getHelper(NextStepsHandler nextStepsHandler) {
        return new HtmlUnitStepHelper(this, nextStepsHandler);
    }

    protected C addFilter(Filter<DomNode> filter) {
        return copyModifyAndGet(copy -> {
            copy.filters.add(filter);
            return copy;
        });
    }

    protected List<Filter<DomNode>> getFilters() {
        return filters;
    }

    @SuppressWarnings("unchecked")
    protected C copyFieldValuesTo(HtmlUnitScrapingStep<?> other) {
        super.copyFieldValuesTo(other);
        other.filters.addAll(this.filters);
        return (C) other;
    }

    @Override
    protected ScrapingType getScrapingType() {
        return ScrapingType.HTMLUNIT;
    }
}
