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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

// must be called at the end of a paginating sequence!
@Log4j2
public class ReturnNextPage extends CommonOperationsStepBase<ReturnNextPage> {

    private boolean callbackStepSet = false;

    ReturnNextPage(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps) {
        super(nextSteps);
    }

    ReturnNextPage() {
        this(null);
    }

    @Override
    public ReturnNextPage copy() {
        ReturnNextPage copy = new ReturnNextPage();
        copy.callbackStepSet = this.callbackStepSet;
        return copyFieldValuesTo(copy);
    }

    /**
     * @param ctx must contain a reference to HtmlPage that might be paginated (contains some for of next link or button)
     */
    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {

        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            Optional<HtmlPage> page = ctx.getNodeAsHtmlPage();

            if (page.isPresent()) {
                Supplier<List<DomNode>> nodesSearch = () -> List.of(page.get());
                getHelper().execute(ctx, nodesSearch, i -> true, stepExecOrder, getExecuteIf());
            } else {
                log.error("The previous step did not produce an HtmlPage! Cannot process next page data in step {}", getName());
            }
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    // TODO remove this and somehow communicate this via the ScrapingContext ... we can have a callback step there ... thet can be utlized by some steps
    /**
     * Must be called at runtime - mutates this instance.
     */
    void setCallbackToPageDataProcessingStep(HtmlUnitScrapingStep<?> processingStep) {
        if (!this.callbackStepSet) {
            this.addNextStepMutably(processingStep);
            this.callbackStepSet = true;
        }
    }

}
