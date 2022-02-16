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

public class HtmlUnitParseElementAttributeValue extends HtmlUnitScrapingStep<HtmlUnitParseElementAttributeValue>
        implements CollectingParsedValueToModelStep<HtmlUnitParseElementAttributeValue, String>,
        ParsingStep<HtmlUnitParseElementAttributeValue> {

    private final String attributeName;

    HtmlUnitParseElementAttributeValue(String attributeName, Function<String, String> parsedValueConversion) {
        this.attributeName = attributeName;
        this.parsedValueConversion = Objects.requireNonNullElse(parsedValueConversion, NO_VALUE_CONVERSION);
    }

    HtmlUnitParseElementAttributeValue(String attributeName) {
        this(attributeName, null);
    }

    @Override
    protected HtmlUnitParseElementAttributeValue copy() {
        return copyFieldValuesTo(new HtmlUnitParseElementAttributeValue(attributeName, parsedValueConversion));
    }


    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            if (ctx.getNode() instanceof HtmlElement) {
                HtmlElement el = (HtmlElement) ctx.getNode();
                if (el.hasAttribute(attributeName)) {
                    String value = el.getAttribute(attributeName);
                    if (value != null) {
                        String converted = convertParsedText(value);
                        log.debug("{} - {}: Parsed value: {}", stepOrder, getName(), converted);

                        setParsedValueToModel(this.getCollectors(), ctx, converted, getName(), stepDeclarationLine);

                        Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode()); // just resend the node ...
                        getHelper().execute(ctx, nodesSearch, stepOrder, getExecuteIf(), services);
                    }
                } else {
                    log.trace("{}: Node does not have attribute {}: node: {}", getName(), attributeName, ctx.getNode());
                }
            } else {
                log.trace("{}: Node is not an HtmlElement node: {}", getName(), ctx.getNode());
            }
        };

        submitForExecution(stepOrder, runnable, services.getTaskService(), services.getSeleniumDriversManager());

        return stepOrder;
    }

    @Override
    public <T> HtmlUnitParseElementAttributeValue collectOne(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.ONE));
    }

    @Override
    public <T> HtmlUnitParseElementAttributeValue collectMany(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.MANY));
    }

    @Override
    public HtmlUnitParseElementAttributeValue setValueConversion(Function<String, String> parsedTextMapper) {
        return setParsedValueConversion(parsedTextMapper);
    }

}
