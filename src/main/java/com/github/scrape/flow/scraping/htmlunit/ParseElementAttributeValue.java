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
import com.github.scrape.flow.parallelism.StepExecOrder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.scrape.flow.scraping.htmlunit.Collector.AccumulatorType;

public class ParseElementAttributeValue extends CommonOperationsStepBase<ParseElementAttributeValue>
        implements HtmlUnitStepCollectingParsedStringToModel<ParseElementAttributeValue>,
        HtmlUnitParsingStep<ParseElementAttributeValue> {

    private final String attributeName;

    ParseElementAttributeValue(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps, String attributeName, Function<String, String> parsedValueConversion) {
        super(nextSteps);
        this.attributeName = attributeName;
        this.parsedValueConversion = Objects.requireNonNullElse(parsedValueConversion, NO_VALUE_CONVERSION);
    }

    ParseElementAttributeValue(String attributeName, Function<String, String> parsedValueConversion) {
        this(null, attributeName, parsedValueConversion);
    }

    ParseElementAttributeValue(String attributeName) {
        this(null, attributeName, null);
    }

    @Override
    protected ParseElementAttributeValue copy() {
        return copyFieldValuesTo(new ParseElementAttributeValue(attributeName, parsedValueConversion));
    }


    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            if (ctx.getNode() instanceof HtmlElement el && el.hasAttribute(attributeName)) {
                String value = el.getAttribute(attributeName);
                if (value != null) {
                    String converted = convertParsedText(value);
                    log.debug("{} - {}: Parsed value: {}", stepExecOrder, getName(), converted);

                    setParsedValueToModel(this.getCollectors(), ctx, converted, getName(), stepDeclarationLine);

                    Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode()); // just resend the node ...
                    getHelper().execute(ctx, nodesSearch, stepExecOrder, getExecuteIf());
                }
            } else {
                log.trace("{}: Node is not an HtmlElement or does not have attribute {}: node: {}", getName(), attributeName, ctx.getNode());
            }
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    @Override
    public <T> ParseElementAttributeValue collectOne(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.ONE));
    }

    @Override
    public <T> ParseElementAttributeValue collectMany(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.MANY));
    }

    @Override
    public ParseElementAttributeValue setValueConversion(Function<String, String> parsedTextConversion) {
        return setParsedValueConversion(parsedTextConversion);
    }

}