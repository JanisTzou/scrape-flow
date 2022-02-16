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

package com.github.scrape.flow.scraping.selenium;

import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.CollectingParsedValueToModelStep;
import com.github.scrape.flow.scraping.ParsingStep;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitScrapingStep;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.scrape.flow.data.collectors.Collector.AccumulatorType;

public class SeleniumParseElementHRef extends SeleniumScrapingStep<SeleniumParseElementHRef>
        implements CollectingParsedValueToModelStep<SeleniumParseElementHRef, String>,
        ParsingStep<SeleniumParseElementHRef> {

    SeleniumParseElementHRef(Function<String, String> parsedValueConversion) {
        this.parsedValueConversion = Objects.requireNonNullElse(parsedValueConversion, NO_VALUE_CONVERSION);
    }

    SeleniumParseElementHRef() {
        this( null);
    }

    @Override
    protected SeleniumParseElementHRef copy() {
        return copyFieldValuesTo(new SeleniumParseElementHRef(parsedValueConversion));
    }


    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            if (SeleniumUtils.hasAttribute(ctx.getWebElement(), "href")) {
                String href = ctx.getWebElement().getAttribute("href");
                if (href != null) {
                    String converted = convertParsedText(href);
                    log.debug("{} - {}: Parsed href: {}", stepOrder, getName(), converted);

                    setParsedValueToModel(this.getCollectors(), ctx, converted, getName(), stepDeclarationLine);

                    Supplier<List<WebElement>> nodesSearch = () -> List.of(ctx.getWebElement()); // just resend the node ...
                    ScrapingContext ctxCopy = ctx.toBuilder().setParsedURL(converted).build();
                    getHelper().execute(ctxCopy, ctxCopy.getDriverNo(), nodesSearch, stepOrder, getExecuteIf(), services);
                }
            } else {
                log.warn("No HtmlAnchor element provided -> cannot parse href value! Check the steps sequence above step {}", getName());
            }
        };

        submitForExecution(stepOrder, runnable, services.getTaskService(), services.getSeleniumDriversManager());

        return stepOrder;
    }

    @Override
    public <T> SeleniumParseElementHRef collectOne(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.ONE));
    }

    @Override
    public <T> SeleniumParseElementHRef collectMany(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.MANY));
    }

    @Override
    public SeleniumParseElementHRef setValueConversion(Function<String, String> parsedTextMapper) {
        return setParsedValueConversion(parsedTextMapper);
    }

}
