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

package com.github.web.scraping.lib.dom.data.parsing.steps;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
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

    MapElements(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, Function<DomNode, Optional<DomNode>> mapper) {
        super(nextSteps);
        this.mapper = mapper;
    }

    MapElements(Function<DomNode, Optional<DomNode>> mapper) {
        this(null, mapper);
    }

    @Override
    public <ModelT, ContainerT> StepExecOrder execute(ParsingContext<ModelT, ContainerT> ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {

            Supplier<List<DomNode>> nodesSearch = () -> {
                Optional<DomNode> other = mapper.apply(ctx.getNode());
                if (other.isPresent()) {
                    log.debug("{} element mapped successfully from {} to {}", getName(), ctx.getNode(), other.get());
                    return List.of(other.get());
                } else {
                    log.debug("{} element could not be mapped from {} to other element", getName(), ctx.getNode());
                }
                return Collections.emptyList();
            };

            @SuppressWarnings("unchecked")
            HtmlUnitParsingStepHelper<ModelT, ContainerT> wrapper = new HtmlUnitParsingStepHelper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName(), services);
            wrapper.execute(ctx, nodesSearch, stepExecOrder);
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

}
