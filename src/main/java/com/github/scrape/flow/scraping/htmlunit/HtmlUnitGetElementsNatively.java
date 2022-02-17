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
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Maps nodes acquired in the previous steps to other nodes ... e.g. children/parents/siblings etc ...
 */
@Log4j2
public class HtmlUnitGetElementsNatively extends HtmlUnitScrapingStep<HtmlUnitGetElementsNatively> {

    private final Function<DomNode, Optional<DomNode>> mapper;

    HtmlUnitGetElementsNatively(Function<DomNode, Optional<DomNode>> mapper) {
        this.mapper = mapper;
    }

    @Override
    protected HtmlUnitGetElementsNatively copy() {
        return copyFieldValuesTo(new HtmlUnitGetElementsNatively(this.mapper));
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {

            Supplier<List<DomNode>> nodesSearch = () -> {
                Optional<DomNode> mapped = mapper.apply(ctx.getNode());
                if (mapped.isPresent()) {
                    log.debug("{} element mapped successfully from {} to {}", getName(), ctx.getNode(), mapped.get());
                    return List.of(mapped.get());
                } else {
                    log.debug("{} element could not be mapped from {} to other element", getName(), ctx.getNode());
                }
                return Collections.emptyList();
            };

            getHelper().execute(ctx, nodesSearch, stepOrder, getExecuteIf(), services);
        };

        submitForExecution(stepOrder, runnable, services.getTaskService(), services.getSeleniumDriversManager());

        return stepOrder;
    }

}
