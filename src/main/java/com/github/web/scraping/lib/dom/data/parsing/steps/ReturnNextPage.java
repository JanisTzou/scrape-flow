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

    ReturnNextPage(@Nullable List<HtmlUnitParsingStep<?>> nextSteps) {
        super(nextSteps);
    }

    ReturnNextPage() {
        this(null);
    }

    /**
     * @param ctx must contain a reference to HtmlPage that might be paginated (contains some for of next link or button)
     */
    @Override
    public StepExecOrder execute(ParsingContext ctx) {

        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            Optional<HtmlPage> page = ctx.getNodeAsHtmlPage();

            if (page.isPresent()) {
                Supplier<List<DomNode>> nodesSearch = () -> List.of(page.get());
                HtmlUnitStepHelper helper = new HtmlUnitStepHelper(nextSteps, getName(), services, collectorSetups);
                helper.execute(ctx, nodesSearch, stepExecOrder);
            } else {
                log.error("The previous step did not produce an HtmlPage! Cannot process next page data in step {}", getName());
            }
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    void setCallbackToPageDataProcessingStep(HtmlUnitParsingStep<?> processingStep) {
        if (!this.callbackStepSet) {
            this.nextSteps.add(processingStep);
            this.callbackStepSet = true;
        }
    }

}
