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
import com.github.scrape.flow.parallelism.StepExecOrder;
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
public class MapElements extends CommonOperationsStepBase<MapElements> {

    private final Function<DomNode, Optional<DomNode>> mapper;

    MapElements(Function<DomNode, Optional<DomNode>> mapper) {
        this.mapper = mapper;
    }

    @Override
    protected MapElements copy() {
        return copyFieldValuesTo(new MapElements(this.mapper));
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepExecOrder stepExecOrder = services.getStepExecOrderGenerator().genNextOrderAfter(ctx.getPrevStepExecOrder());

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

            getHelper().execute(ctx, nodesSearch, stepExecOrder, getExecuteIf(), services);
        };

        submitForExecution(stepExecOrder, runnable, services.getTaskService());

        return stepExecOrder;
    }

}
