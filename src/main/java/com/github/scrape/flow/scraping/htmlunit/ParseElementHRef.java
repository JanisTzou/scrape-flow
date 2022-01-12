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
import com.github.scrape.flow.scraping.*;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.scrape.flow.data.collectors.Collector.AccumulatorType;

public class ParseElementHRef extends HtmlUnitScrapingStep<ParseElementHRef>
        implements CollectingParsedValueToModelStep<ParseElementHRef, String>,
        ParsingStep<ParseElementHRef> {

    ParseElementHRef(Function<String, String> parsedValueConversion) {
        this.parsedValueConversion = Objects.requireNonNullElse(parsedValueConversion, NO_VALUE_CONVERSION);
    }

    ParseElementHRef() {
        this( null);
    }

    @Override
    protected ParseElementHRef copy() {
        return copyFieldValuesTo(new ParseElementHRef(parsedValueConversion));
    }


    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            if (ctx.getNode() instanceof HtmlAnchor) {
                HtmlAnchor anch = (HtmlAnchor) ctx.getNode();
                String href = anch.getHrefAttribute();
                if (href != null) {
                    String converted = convertParsedText(href);
                    log.debug("{} - {}: Parsed href: {}", stepOrder, getName(), converted);

                    setParsedValueToModel(this.getCollectors(), ctx, converted, getName(), stepDeclarationLine);

                    Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode()); // just resend the node ...
                    ScrapingContext ctxCopy = ctx.toBuilder().setParsedURL(converted).build();
                    getHelper().execute(ctxCopy, nodesSearch, stepOrder, getExecuteIf(), services);
                }
            } else {
                log.warn("No HtmlAnchor element provided -> cannot parse href value! Check the steps sequence above step {}", getName());
            }
        };

        submitForExecution(stepOrder, runnable, services.getTaskService());

        return stepOrder;
    }

    @Override
    public <T> ParseElementHRef collectOne(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.ONE));
    }

    @Override
    public <T> ParseElementHRef collectMany(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, String.class, containerType, AccumulatorType.MANY));
    }

    // TODO have two methods ? 1 named nextNavigateToStaticSite and other nextNavigateToDynamicSite
    //  or should this just accept 2 types ? the static and dynamic version of the next step ?
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
