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

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.github.scrape.flow.parallelism.StepExecOrder;
import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.scraping.ScrapingServices;
import org.apache.commons.text.StringEscapeUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.github.scrape.flow.data.collectors.Collector.AccumulatorType;

public class ParseElementTextContent extends HtmlUnitScrapingStep<ParseElementTextContent>
        implements CollectingParsedValueToModelStep<ParseElementTextContent, String>,
        ParsingStep<ParseElementTextContent> {

    ParseElementTextContent(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps) {
        super(nextSteps);
    }

    ParseElementTextContent() {
        this(null);
    }

    @Override
    protected ParseElementTextContent copy() {
        return copyFieldValuesTo(new ParseElementTextContent());
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepExecOrder stepExecOrder = services.getStepExecOrderGenerator().genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            String tc = null;
            if (ctx.getNode() instanceof HtmlElement htmlEl) {
                tc = htmlEl.getTextContent();
                if (tc != null) {
                    tc = StringEscapeUtils.unescapeHtml4(tc).trim();
                }
            }

            String transformed = convertParsedText(tc);

            setParsedValueToModel(this.getCollectors(), ctx, transformed, getName(), stepDeclarationLine);
        };

        submitForExecution(stepExecOrder, runnable, services.getTaskService());

        return stepExecOrder;
    }

    @Override
    public <T> ParseElementTextContent collectOne(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.ONE));
    }

    @Override
    public <T> ParseElementTextContent collectMany(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.MANY));
    }


    @Override
    public ParseElementTextContent setValueConversion(Function<String, String> parsedTextMapper) {
        return copyModifyAndGet(copy -> {
            copy.parsedValueConversion = parsedTextMapper;
            return copy;
        });
    }

}
