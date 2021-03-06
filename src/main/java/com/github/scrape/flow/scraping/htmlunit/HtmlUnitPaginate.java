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
import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.*;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public class HtmlUnitPaginate extends HtmlUnitScrapingStep<HtmlUnitPaginate> {

    private ScrapingStep<?> paginatingSequence;

    private boolean servicesPropagatedToTrigger;

    HtmlUnitPaginate(boolean servicesPropagatedToTrigger) {
        this.servicesPropagatedToTrigger = servicesPropagatedToTrigger;
    }

    HtmlUnitPaginate() {;
    }

    @Override
    protected HtmlUnitPaginate copy() {
        HtmlUnitPaginate copy = new HtmlUnitPaginate(servicesPropagatedToTrigger);
        if (this.paginatingSequence != null) {
            copy.paginatingSequence = ScrapingStepInternalReader.of(this.paginatingSequence).copy();
        }
        return copyFieldValuesTo(copy);
    }

    /**
     * @param ctx must contain a reference to HtmlPage that might be paginated (contains some for of next link or button)
     */
    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {

        StepOrder prevStepOrder = ctx.getRootLoopedStepOrder() == null
                ? ctx.getPrevStepOrder()
                : ctx.getRootLoopedStepOrder();

        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(prevStepOrder);

        checkPaginationTriggerAndLinkItToThisStep();

        Optional<HtmlPage> page = ctx.getNodeAsHtmlPage();
        if (!page.isPresent()) {
            log.error("{} - {}: No HtmlPage provided by previous step! Cannot process page data and paginate to next pages!", stepOrder, getName());
        }

        Runnable runnable = () -> {
            if (page.isPresent()) {

                // GENERAL - just processes the received page
                Supplier<List<DomNode>> nodesSearch = () -> List.of(page.get());
                // important to set the recursiveRootStepOrder to null ... the general nextSteps and logic should not be affected by it ... it's only related to pagination
                ScrapingContext plainCtx = ctx.toBuilder()
                        .setRecursiveRootStepOrder(null)
                        .build();
                NextStepsHandler nextStepsHandler = new NextStepsWrappedInOneExclusiveBlock(); // we need to make absolutely sure that the next steps have finished before we go to next page
                getHelper(services, nextStepsHandler).execute(nodesSearch, plainCtx, stepOrder);

                // PAGINATION
                ScrapingContext paginatingCtx = ctx.toBuilder()
                        .setPrevStepOrder(stepOrder)
                        .setRecursiveRootStepOrder(prevStepOrder) // TODO reconsider?
                        .setNode(page.get())
                        .build();
                // TODO the pagination sequence does not support models currently ... if it does (e.g. for internal data propagation purposes) it will need to implement some for of data tracking ...
                //  but it is questionably if we would like to design the data propagation as models if it's just for internal purposes ...
//                services.getStepAndDataRelationshipTracker().track(stepOrder, generatedSteps, model, (ParsedDataListener<Object>) collecting.getDataListener());

                ScrapingStepInternalReader.of(paginatingSequence).execute(paginatingCtx, services);

            }
        };

        submitForExecution(stepOrder, runnable, services);

        return stepOrder;
    }

    /**
     * Steps that trigger the pagination - that is loading the next content.
     * In practice this is most often the action finding the "NEXT" button element and clicking it.
     */
    public HtmlUnitPaginate setStepsLoadingNextPage(HtmlUnitScrapingStep<?> paginatingSequence) {
        this.paginatingSequence = ScrapingStepInternalReader.of(paginatingSequence).copy();
        return this;
    }


    // TODO this should be removed ... we wanna pass this link via the context ...
    private void checkPaginationTriggerAndLinkItToThisStep() {
        if (paginatingSequence == null) {
            throw new IllegalStateException("paginationTrigger must be set for pagination to work!");
        } else {
            Optional<HtmlUnitReturnNextPage> returnNextPageStep = StepsUtils.findStepOfTypeInSequence(paginatingSequence, HtmlUnitReturnNextPage.class);
            if (!returnNextPageStep.isPresent()) {
                throw new IllegalStateException("the paginationTrigger step sequence must contain the step ReturnNextPage to work properly. Cannot execute pagination in this step: " + getName());
            } else {
                if (!servicesPropagatedToTrigger) {
                    returnNextPageStep.get().setCallbackToPageDataProcessingStep(this); // IMPORTANT so that the step can propagate the next page back to this step
                    servicesPropagatedToTrigger = true;
                }
            }
        }
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.MODIFYING;
    }


}
