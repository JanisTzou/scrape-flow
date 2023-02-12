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
import com.github.scrape.flow.scraping.*;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Log4j2
public class HtmlUnitPeek extends HtmlUnitScrapingStep<HtmlUnitPeek> {

    private final Consumer<DomNode> consumer;

    HtmlUnitPeek(Consumer<DomNode> consumer) {
        this.consumer = consumer;
    }

    @Override
    protected HtmlUnitPeek copy() {
        return copyFieldValuesTo(new HtmlUnitPeek(consumer));
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());
        Runnable runnable = () -> {
            consumer.accept(ctx.getNode());
            Supplier<List<DomNode>> nodesSearch = () -> ctx.getNode() != null ? List.of(ctx.getNode()) : Collections.emptyList();
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
