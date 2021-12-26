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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public class Paginate extends CommonOperationsStepBase<Paginate> {

    private HtmlUnitScrapingStep<?> paginationTrigger;

    private boolean servicesPropagatedToTrigger;

    Paginate(List<HtmlUnitScrapingStep<?>> nextSteps, boolean servicesPropagatedToTrigger) {
        super(nextSteps);
        this.servicesPropagatedToTrigger = servicesPropagatedToTrigger;
    }

    public Paginate(boolean servicesPropagatedToTrigger) {
        this(null, servicesPropagatedToTrigger);
    }

    Paginate(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps) {
        super(nextSteps);
    }

    Paginate() {
        this(null);
    }

    @Override
    protected Paginate copy() {
        Paginate copy = new Paginate(servicesPropagatedToTrigger);
        if (this.paginationTrigger != null) {
            copy.paginationTrigger = this.paginationTrigger.copy();
        }
        return copyFieldValuesTo(copy);
    }

    /**
     * @param ctx must contain a reference to HtmlPage that might be paginated (contains some for of next link or button)
     */
    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {

        StepExecOrder prevStepExecOrder = ctx.getRecursiveRootStepExecOrder() == null
                ? ctx.getPrevStepExecOrder()
                : ctx.getRecursiveRootStepExecOrder();

        StepExecOrder stepExecOrder = genNextOrderAfter(prevStepExecOrder);

        checkPaginationTriggerAndLinkItToThisStep();

        Optional<HtmlPage> page = ctx.getNodeAsHtmlPage();
        if (page.isEmpty()) {
            log.error("{} - {}: No HtmlPage provided by previous step! Cannot process page data and paginate to next pages!", stepExecOrder, getName());
        }

        // HERE WE TRIGGER TWO TASKS -> DATA PROCESSING & PAGINATION

        // >>>>>    this part just processes the received page ...

        Runnable runnable = () -> {
            if (page.isPresent()) {
                // GENERAL
                Supplier<List<DomNode>> nodesSearch = () -> List.of(page.get());
                // important to set the recursiveRootStepExecOrder to null ... the general nextSteps and logic should not be affected by it ... it's only related to pagination
                ScrapingContext plainCtx = ctx.toBuilder()
                        .setRecursiveRootStepExecOrder(null)
                        .build();
                getHelper().execute(plainCtx, nodesSearch, stepExecOrder, getExecuteIf());

                // PAGINATION
                ScrapingContext paginatingCtx = ctx.toBuilder()
                        .setPrevStepOrder(stepExecOrder)
                        .setRecursiveRootStepExecOrder(prevStepExecOrder)
                        .setNode(page.get())
                        .build();
                // TODO the pagination sequence does not support models currently ... if it does (e.g. for internal data propagation purposes) it will need to implement some for of data tracking ...
                //  but it is questionably if we would like to design the data propagation as models if it's just for internal purposes ...
//                services.getStepAndDataRelationshipTracker().track(stepExecOrder, generatedSteps, model, (ParsedDataListener<Object>) collecting.getDataListener());

                paginationTrigger.execute(paginatingCtx);

            }
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    /**
     * Steps that trigger the pagination - that is loading the next content.
     * In practice this is most often the action finding the "NEXT" button element and clicking it.
     */
    public Paginate setStepsLoadingNextPage(HtmlUnitScrapingStep<?> paginatingSequence) {
        this.paginationTrigger = paginatingSequence;
        return this;
    }


    private void checkPaginationTriggerAndLinkItToThisStep() {
        if (paginationTrigger == null) {
            throw new IllegalStateException("paginationTrigger must be set for pagination to work!");
        } else {
            Optional<ReturnNextPage> returnNextPageStep = StepsUtils.findStepOfTypeInSequence(paginationTrigger, ReturnNextPage.class);
            if (returnNextPageStep.isEmpty()) {
                throw new IllegalStateException("the paginationTrigger step sequence must contain the step ReturnNextPage to work properly. Cannot execute pagination in this step: " + getName());
            } else {
                if (!servicesPropagatedToTrigger) {
                    StepsUtils.propagateServicesRecursively(paginationTrigger, services, new HashSet<>());
                    returnNextPageStep.get().setCallbackToPageDataProcessingStep(this); // IMPORTANT so that the step can propagate the next page back to this step
                    servicesPropagatedToTrigger = true;
                }
            }
        }
    }


}
