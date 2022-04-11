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
import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.function.Predicate;

/**
 * Filters nodes acquired in the previous steps by custom conditions
 */
@Log4j2
@RequiredArgsConstructor
public class HtmlUnitFilterElementsNatively extends HtmlUnitScrapingStep<HtmlUnitFilterElementsNatively> {

    private final Predicate<DomNode> domNodePredicate;

    @Override
    protected HtmlUnitFilterElementsNatively copy() {
        return copyFieldValuesTo(new HtmlUnitFilterElementsNatively(domNodePredicate));
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());
        Runnable runnable = new HtmlUnitFilterElementsNativelyRunnable(domNodePredicate, ctx, stepOrder, getHelper(services));
        submitForExecution(stepOrder, runnable, services);
        return stepOrder;
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }

}
