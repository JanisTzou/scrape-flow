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
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.*;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.scrape.flow.data.collectors.Collector.AccumulatorType;

public class HtmlUnitParseAttributeValue extends HtmlUnitScrapingStep<HtmlUnitParseAttributeValue>
        implements CollectingParsedValueToModelStep<HtmlUnitParseAttributeValue, String>,
        ParsingStep<HtmlUnitParseAttributeValue> {

    private final String attributeName;

    HtmlUnitParseAttributeValue(String attributeName, Function<String, String> parsedValueConversion) {
        this.attributeName = attributeName;
        this.parsedValueMapper = Objects.requireNonNullElse(parsedValueConversion, NO_MAPPING);
    }

    HtmlUnitParseAttributeValue(String attributeName) {
        this(attributeName, null);
    }

    @Override
    protected HtmlUnitParseAttributeValue copy() {
        return copyFieldValuesTo(new HtmlUnitParseAttributeValue(attributeName, parsedValueMapper));
    }


    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            if (ctx.getNode() instanceof HtmlElement) {
                HtmlElement el = (HtmlElement) ctx.getNode();
                if (el.hasAttribute(attributeName)) {
                    String value = el.getAttribute(attributeName);
                    if (value != null) {
                        String mappedVal = mapParsedValue(value);
                        log.debug("{} - {}: Parsed value: {}", stepOrder, getName(), mappedVal);

                        ParsedValueToModelCollector.setParsedValueToModel(this.getCollectors(), ctx, mappedVal, getName());

                        Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode()); // just resend the node ...
                        getHelper(services).execute(nodesSearch, ctx, stepOrder);
                    }
                } else {
                    log.trace("{}: Node does not have attribute {}: node: {}", getName(), attributeName, ctx.getNode());
                }
            } else {
                log.trace("{}: Node is not an HtmlElement node: {}", getName(), ctx.getNode());
            }
        };

        submitForExecution(stepOrder, runnable, services);

        return stepOrder;
    }

    @Override
    public <T> HtmlUnitParseAttributeValue collectValue(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.ONE));
    }

    @Override
    public <T> HtmlUnitParseAttributeValue collectValues(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.MANY));
    }

    @Override
    public HtmlUnitParseAttributeValue setValueMapper(Function<String, String> parsedTextMapper) {
        return setParsedValueMapper(parsedTextMapper);
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }


}
