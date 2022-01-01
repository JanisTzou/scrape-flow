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
import com.github.scrape.flow.parallelism.StepExecOrder;
import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.scraping.ScrapingServices;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.scrape.flow.data.collectors.Collector.AccumulatorType;

public class ParseElementHRef extends CommonOperationsStepBase<ParseElementHRef>
        implements CollectingParsedValueToModelStep<ParseElementHRef, String>,
        ParsingStep<ParseElementHRef> {

    ParseElementHRef(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps, Function<String, String> parsedValueConversion) {
        super(nextSteps);
        this.parsedValueConversion = Objects.requireNonNullElse(parsedValueConversion, NO_VALUE_CONVERSION);
    }

    ParseElementHRef(Function<String, String> parsedValueConversion) {
        this(null, parsedValueConversion);
    }

    ParseElementHRef() {
        this(null, null);
    }

    @Override
    protected ParseElementHRef copy() {
        return copyFieldValuesTo(new ParseElementHRef(parsedValueConversion));
    }


    @Override
    protected StepExecOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepExecOrder stepExecOrder = services.getStepExecOrderGenerator().genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            if (ctx.getNode() instanceof HtmlAnchor anch) {
                String href = anch.getHrefAttribute();
                if (href != null) {
                    String converted = convertParsedText(href);
                    log.debug("{} - {}: Parsed href: {}", stepExecOrder, getName(), converted);

                    setParsedValueToModel(this.getCollectors(), ctx, converted, getName(), stepDeclarationLine);

                    Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode()); // just resend the node ...
                    ScrapingContext ctxCopy = ctx.toBuilder().setParsedURL(converted).build();
                    getHelper().execute(ctxCopy, nodesSearch, stepExecOrder, getExecuteIf(), services);
                }
            } else {
                log.warn("No HtmlAnchor element provided -> cannot parse href value! Check the steps sequence above step {}", getName());
            }
        };

        submitForExecution(stepExecOrder, runnable, services.getTaskService());

        return stepExecOrder;
    }

    @Override
    public <T> ParseElementHRef collectOne(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.ONE));
    }

    @Override
    public <T> ParseElementHRef collectMany(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.MANY));
    }

    /**
     * Same as {@link ChainedStep#next(HtmlUnitScrapingStep)} but with a more meaningful name for the purpose.
     * For more specialised versions of <code>next()</code> see and use these the ones defined here {@link ChainedStep}
     *
     * @return copy of this step
     */
    public ParseElementHRef nextNavigate(NavigateToParsedLink nextStep) {
        return addNextStep(nextStep);
    }

    @Override
    public ParseElementHRef setValueConversion(Function<String, String> parsedTextMapper) {
        return setParsedValueConversion(parsedTextMapper);
    }

}
