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
import com.github.web.scraping.lib.parallelism.StepExecOrder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class GetElementsByTag extends GetElementsStepBase<GetElementsByTag> {

    private final String tagName;

    /**
     * @throws NullPointerException if tagName is null
     */
    GetElementsByTag(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps, String tagName) {
        super(nextSteps);
        Objects.requireNonNull(tagName);
        this.tagName = tagName;
    }

    GetElementsByTag(String tagName) {
        this(null, tagName);
    }

    @Override
    public GetElementsByTag copy() {
        return copyFieldValuesTo(new GetElementsByTag(tagName));
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            // TODO how to use this as a filter ...
            Supplier<List<DomNode>> nodesSearch = () -> filterByTraverseOption(HtmlUnitUtils.getDescendantsByTagName(ctx.getNode(), tagName));
            getHelper().execute(ctx, nodesSearch, i -> true, stepExecOrder, getExecuteIf());
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

}
