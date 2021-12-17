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

package com.github.web.scraping.lib.dom.data.parsing.steps;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.scraping.utils.HtmlUnitUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class GetElementsByAttribute extends HtmlUnitChainableStep<GetElementsByAttribute>
    implements HtmlUnitCollectorSetupStep<GetElementsByAttribute> {

    private final String attributeName;
    private final String attributeValue;
    private boolean matchEntireValue;
    private Collecting<?, ?> collecting;
    private static final boolean MATCH_ENTIRE_VALUE_DEFAULT = true;

    protected GetElementsByAttribute(@Nullable List<HtmlUnitParsingStep> nextSteps,
                                     String attributeName,
                                     @Nullable String attributeValue,
                                     boolean matchEntireValue) {
        super(nextSteps);
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.matchEntireValue = matchEntireValue;
    }

    protected GetElementsByAttribute(String attributeName,
                                     @Nullable String attributeValue) {
        this(null, attributeName, attributeValue, MATCH_ENTIRE_VALUE_DEFAULT);
    }

    protected GetElementsByAttribute(String attributeName) {
        this(null, attributeName, null, MATCH_ENTIRE_VALUE_DEFAULT);
    }

    public static GetElementsByAttribute instance(String attributeName, String attributeValue) {
        return new GetElementsByAttribute(attributeName, attributeValue);
    }

    public static GetElementsByAttribute instance(String attributeName) {
        return new GetElementsByAttribute(attributeName);
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        Supplier<List<DomNode>> nodesSearch = () -> {
            if (attributeValue != null) {
                return HtmlUnitUtils.getAllChildElementsByAttributeValue(ctx.getNode(), attributeName, attributeValue, this.matchEntireValue);
            } else {
                return HtmlUnitUtils.getAllChildElementsByAttribute(ctx.getNode(), attributeName);
            }
        };
        return new HtmlUnitParsingExecutionWrapper<>(nextSteps, collecting).execute(ctx, nodesSearch);
    }


    public GetElementsByAttribute setMatchEntireValue(boolean matchEntireValue) {
        this.matchEntireValue = matchEntireValue;
        return this;
    }

    @Override
    public <R, T> GetElementsByAttribute collector(Supplier<T> modelSupplier, Supplier<R> containerSupplier, BiConsumer<R, T> accumulator) {
        this.collecting = new Collecting<>(modelSupplier, containerSupplier, accumulator);
        return this;
    }

    @Override
    public <R, T> GetElementsByAttribute collector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator) {
        this.collecting = new Collecting<>(modelSupplier, null, accumulator);
        return this;
    }


}
