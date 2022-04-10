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

import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.*;
import org.apache.commons.text.StringEscapeUtils;
import org.openqa.selenium.WebElement;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.github.scrape.flow.data.collectors.Collector.AccumulatorType;

public class SeleniumParseElementTextContent extends SeleniumScrapingStep<SeleniumParseElementTextContent>
        implements CollectingParsedValueToModelStep<SeleniumParseElementTextContent, String>,
        ParsingStep<SeleniumParseElementTextContent> {

    SeleniumParseElementTextContent() {
    }

    @Override
    protected SeleniumParseElementTextContent copy() {
        return copyFieldValuesTo(new SeleniumParseElementTextContent());
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            WebElement webElement = ctx.getWebElement();
            String tc = webElement.getText();
            if (tc != null) {
                tc = StringEscapeUtils.unescapeHtml4(tc).trim();
            }

            String mappedVal = mapParsedValue(tc);

            ParsedValueToModelCollector.setParsedValueToModel(this.getCollectors(), ctx, mappedVal, getName());
        };

        submitForExecution(stepOrder, runnable, services.getTaskService());

        return stepOrder;
    }

    @Override
    public <T> SeleniumParseElementTextContent collectValue(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.ONE));
    }

    @Override
    public <T> SeleniumParseElementTextContent collectValues(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.MANY));
    }


    @Override
    public SeleniumParseElementTextContent setValueMapper(Function<String, String> parsedTextMapper) {
        return copyModifyAndGet(copy -> {
            copy.parsedValueMapper = parsedTextMapper;
            return copy;
        });
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }


}
