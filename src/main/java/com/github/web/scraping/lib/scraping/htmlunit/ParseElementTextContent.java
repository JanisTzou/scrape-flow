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

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import org.apache.commons.text.StringEscapeUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.github.web.scraping.lib.scraping.htmlunit.Collector.AccumulatorType;

public class ParseElementTextContent extends HtmlUnitScrapingStep<ParseElementTextContent>
        implements HtmlUnitStepCollectingParsedStringToModel<ParseElementTextContent>,
        HtmlUnitParsingStep<ParseElementTextContent> {

    // TODO provide a set of options to transform/sanitize text ... \n \t .. etc ...

    private boolean excludeChildElementsTextContent;

    ParseElementTextContent(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps,
                            boolean excludeChildElementsTextContent) {
        super(nextSteps);
        this.excludeChildElementsTextContent = excludeChildElementsTextContent;
    }

    ParseElementTextContent(boolean excludeChildElementsTextContent) {
        this(null, excludeChildElementsTextContent);
    }

    ParseElementTextContent() {
        this(null, false);
    }

    @Override
    protected ParseElementTextContent copy() {
        return copyFieldValuesTo(new ParseElementTextContent(excludeChildElementsTextContent));
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            String tc = null;
            if (ctx.getNode() instanceof HtmlElement htmlEl) {
                // TODO look for DomText ...
                tc = htmlEl.getTextContent();
                if (tc != null) {
                    tc = StringEscapeUtils.unescapeHtml4(tc);
                    // this should be optional ... used in cases when child elements' content filthies the parent element's content ...
                    if (this.excludeChildElementsTextContent) {
                        // TODO maybe we can use the text node for this very purpose ?
                        tc = removeChildElementsTextContent(tc, htmlEl);
                    }
                    tc = tc.trim();
                }
            }

            String transformed = convertParsedText(tc);

            setParsedValueToModel(this.getCollectors(), ctx, transformed, getName(), stepDeclarationLine);

            // TODO how to populate the following context?
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    private String removeChildElementsTextContent(String textContent, HtmlElement el) {
        for (DomElement childElement : el.getChildElements()) {
            textContent = textContent.replace(childElement.getTextContent(), "");
        }
        return textContent;
    }


    /**
     * Determines if the text of child elements should be part of the resulting parsed text content
     * set to false by default
     *
     * @return copy of this step
     */
    public ParseElementTextContent excludeChildElements() {
        return copyModifyAndGet(copy -> {
            copy.excludeChildElementsTextContent = true;
            return copy;
        });
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
    public ParseElementTextContent setValueConversion(Function<String, String> parsedTextTransformation) {
        return copyModifyAndGet(copy -> {
            copy.parsedValueConversion = parsedTextTransformation;
            return copy;
        });
    }

}
