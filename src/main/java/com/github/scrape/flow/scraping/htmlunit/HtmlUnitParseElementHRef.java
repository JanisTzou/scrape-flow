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
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.CollectingParsedValueToModelStep;
import com.github.scrape.flow.scraping.ParsingStep;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.scrape.flow.data.collectors.Collector.AccumulatorType;

public class HtmlUnitParseElementHRef extends HtmlUnitScrapingStep<HtmlUnitParseElementHRef>
        implements CollectingParsedValueToModelStep<HtmlUnitParseElementHRef, String>,
        ParsingStep<HtmlUnitParseElementHRef> {

    HtmlUnitParseElementHRef(Function<String, String> parsedValueConversion) {
        this.parsedValueMapper = Objects.requireNonNullElse(parsedValueConversion, NO_MAPPING);
    }

    HtmlUnitParseElementHRef() {
        this( null);
    }

    @Override
    protected HtmlUnitParseElementHRef copy() {
        return copyFieldValuesTo(new HtmlUnitParseElementHRef(parsedValueMapper));
    }


    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            if (ctx.getNode() instanceof HtmlAnchor) {
                HtmlAnchor anch = (HtmlAnchor) ctx.getNode();
                String href = anch.getHrefAttribute();
                if (href != null) {
                    String converted = mapParsedValue(href);
                    log.debug("{} - {}: Parsed href: {}", stepOrder, getName(), converted);

                    setParsedValueToModel(this.getCollectors(), ctx, converted, getName());

                    Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode()); // just resend the node ...
                    ScrapingContext ctxCopy = ctx.toBuilder().setParsedURL(converted).build();
                    getHelper().execute(ctxCopy, nodesSearch, stepOrder, getExecuteIf(), services);
                }
            } else {
                log.warn("No HtmlAnchor element provided -> cannot parse href value! Check the steps sequence above step {}", getName());
            }
        };

        submitForExecution(stepOrder, runnable, services.getTaskService(), services.getSeleniumDriversManager());

        return stepOrder;
    }

    @Override
    public <T> HtmlUnitParseElementHRef collectOne(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.ONE));
    }

    @Override
    public <T> HtmlUnitParseElementHRef collectMany(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.MANY));
    }

    @Override
    public HtmlUnitParseElementHRef setValueMapper(Function<String, String> parsedTextMapper) {
        return setParsedValueMapper(parsedTextMapper);
    }

}
