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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.ScrapingStep;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

// must be called at the end of a paginating sequence!
@Log4j2
public class HtmlUnitReturnNextPage extends HtmlUnitScrapingStep<HtmlUnitReturnNextPage> {

    private boolean callbackStepSet = false;


    HtmlUnitReturnNextPage() {
    }

    @Override
    protected HtmlUnitReturnNextPage copy() {
        HtmlUnitReturnNextPage copy = new HtmlUnitReturnNextPage();
        copy.callbackStepSet = this.callbackStepSet;
        return copyFieldValuesTo(copy);
    }

    /**
     * @param ctx must contain a reference to HtmlPage that might be paginated (contains some for of next link or button)
     * @param services
     */
    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {

        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Optional<HtmlPage> page = ctx.getNodeAsHtmlPage();

            if (page.isPresent()) {
                Supplier<List<DomNode>> nodesSearch = () -> List.of(page.get());
                getHelper().execute(ctx, nodesSearch, stepOrder, getExecuteIf(), services);
            } else {
                log.error("The previous step did not produce an HtmlPage! Cannot process next page data in step {}", getName());
            }
        };

        submitForExecution(stepOrder, runnable, services.getTaskService(), services.getSeleniumDriversManager());

        return stepOrder;
    }

    // TODO remove this and somehow communicate this via the ScrapingContext ... we can have a callback step there ... that can be utilized by some steps
    /**
     * Must be called at runtime - mutates this instance.
     */
    void setCallbackToPageDataProcessingStep(ScrapingStep<?> processingStep) {
        if (!this.callbackStepSet) {
            this.addNextStepMutably(processingStep);
            this.callbackStepSet = true;
        }
    }

}
