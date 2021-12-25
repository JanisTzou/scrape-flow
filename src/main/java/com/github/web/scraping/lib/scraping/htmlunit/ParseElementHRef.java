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

package com.github.web.scraping.lib.scraping.htmlunit;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.github.web.scraping.lib.parallelism.StepExecOrder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.web.scraping.lib.scraping.htmlunit.CollectorSetup.*;

public class ParseElementHRef extends CommonOperationsStepBase<ParseElementHRef>
        implements HtmlUnitStepCollectingParsedStringToModel<ParseElementHRef>,
        HtmlUnitParsingStep<ParseElementHRef> {

    // TODO add some filtering logic for the hrefs parsed ...

    // TODO this is basically a specialisation of ParseAttributeValue


    ParseElementHRef(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps, Function<String, String> parsedTextTransformation) {
        super(nextSteps);
        this.parsedTextTransformation = Objects.requireNonNullElse(parsedTextTransformation, NO_TEXT_TRANSFORMATION);
    }

    ParseElementHRef(Function<String, String> parsedTextTransformation) {
        this(null, parsedTextTransformation);
    }

    ParseElementHRef() {
        this(null, null);
    }

    @Override
    protected ParseElementHRef copy() {
        return copyFieldValuesTo(new ParseElementHRef(parsedTextTransformation));
    }


    @Override
    public StepExecOrder execute(ScrapingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            if (ctx.getNode() instanceof HtmlAnchor anch) {
                String href = anch.getHrefAttribute();
                if (href != null) {
                    String transformed = transformParsedText(href);
                    log.debug("{} - {}: Parsed href: {}", stepExecOrder, getName(), transformed);
                    // TODO actually have another transformation that will say something like "transformToFullURL ... and put that one to the context below)

                    setParsedValueToModel(this.getCollectorSetups(), ctx, transformed, getName()); // TODO let this be handled by the helper?

                    Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode()); // just resend the node ...
                    HtmlUnitStepHelper helper = new HtmlUnitStepHelper(getNextSteps(), getName(), services, getCollectorSetups());
                    ScrapingContext ctxCopy = ctx.toBuilder().setParsedURL(transformed).build();
                    helper.execute(ctxCopy, nodesSearch, stepExecOrder, getExecuteIf());
                }
            } else {
                log.warn("No HtmlAnchor element provided -> cannot parse href value! Check the steps sequence above step {}", getName());
            }
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    @Override
    public <T> ParseElementHRef collectOne(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollectorSetup(new CollectorSetup(modelMutation, String.class, containerType, AccumulatorType.ONE));
    }

    @Override
    public <T> ParseElementHRef collectMany(BiConsumer<T, String> modelMutation, Class<T> containerType) {
        return addCollectorSetup(new CollectorSetup(modelMutation, String.class, containerType, AccumulatorType.MANY));
    }

    /**
     * Same as {@link HtmlUnitStepSupportingNext#next(HtmlUnitScrapingStep)} but with a more meaningful name for the purpose.
     * For more specialised versions of <code>next()</code> see and use these the ones defined here {@link HtmlUnitStepSupportingNext}
     *
     * @return copy of this step
     */
    public ParseElementHRef nextNavigate(NavigateToParsedLink nextStep) {
        return addNextStep(nextStep);
    }

    @Override
    public ParseElementHRef setTransformation(Function<String, String> parsedTextToNewText) {
        return setParsedTextTransformation(parsedTextToNewText);
    }

}
