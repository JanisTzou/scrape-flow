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

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HtmlUnitGetAncestor extends HtmlUnitScrapingStep<HtmlUnitGetAncestor> {
    // TODO make possible to use general filters (by tag, class, attr ...)

    private final int param;

    HtmlUnitGetAncestor(int param) {
        this.param = param;
    }

    @Override
    protected HtmlUnitGetAncestor copy() {
        return copyFieldValuesTo(new HtmlUnitGetAncestor(param));
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            DomNode node = ctx.getNode();
            Supplier<List<DomNode>> nodesSearch = () -> HtmlUnitUtils.findNthAncestor(node, param).stream().collect(Collectors.toList());
            getHelper(services).execute(nodesSearch, ctx, stepOrder);
        };
        submitForExecution(stepOrder, runnable, services);

        return stepOrder;
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }

}
