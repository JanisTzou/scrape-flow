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
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public class Paginate extends CommonOperationsStepBase<Paginate> {

    private HtmlUnitParsingStep<?> paginationTrigger;

    private boolean servicesPropagatedToTrigger = false;

    Paginate(@Nullable List<HtmlUnitParsingStep<?>> nextSteps) {
        super(nextSteps);
    }

    Paginate() {
        this(null);
    }

    /**
     * @param ctx must contain a reference to HtmlPage that might be paginated (contains some for of next link or button)
     */
    @Override
    public <ModelT, ContainerT> StepExecOrder execute(ParsingContext<ModelT, ContainerT> ctx) {

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
                @SuppressWarnings("unchecked")
                HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> wrapper = new HtmlUnitParsingExecutionWrapper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName(), services);
                // important to set the recursiveRootStepExecOrder to null ... the general nextSteps and logic should not be affected by it ... it's only related to pagination
                ParsingContext<ModelT, ContainerT> plainCtx = ctx.toBuilder()
                        .setRecursiveRootStepExecOrder(null)
                        .build();
                wrapper.execute(plainCtx, nodesSearch, stepExecOrder);

                // PAGINATION
                ParsingContext<ModelT, ContainerT> paginatingCtx = ctx.toBuilder()
                        .setPrevStepOrder(stepExecOrder)
                        .setRecursiveRootStepExecOrder(prevStepExecOrder)
                        .setNode(page.get())
                        .build();
                // TODO the pagination sequence does not support models currently ... if it does (e.g. for internal data propagation purposes) it will need to implement some for of data tracking ...
                //  but it is questionably if we would like to design the data propagation as models if it's just for internal purposes ...
//                services.getStepAndDataRelationshipTracker().track(stepExecOrder, generatedSteps, model, (ParsedDataListener<Object>) collecting.getDataListener());
                OnOrderGenerated onOrderGenerated1 = so -> {
                };
                paginationTrigger.execute(paginatingCtx);

            }
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    // TODO rename? To stepsReturningNextPage ? or something like that ...
    /**
     * Steps that trigger the pagination - that is loading the next content.
     * In practice this is most often the action finding the "NEXT" button element and clicking it.
     */
    public Paginate setPaginationTrigger(HtmlUnitParsingStep<?> paginatingSequence) {
        this.paginationTrigger = paginatingSequence;
        return this;
    }

    // TODO ordered version ?
    /**
     * Performs the specified step sequence for content loaded in pagination
     * Same as calling {@link com.github.web.scraping.lib.dom.data.parsing.steps.CommonOperationsStepBase#then(HtmlUnitParsingStep)}
     */
    public Paginate thenForEachPage(HtmlUnitParsingStep<?> stepsForPaginatedContent) {
        then(stepsForPaginatedContent);
        return this;
    }

    // TODO actually implement ...
    public Paginate thenForEachPageOrdered(HtmlUnitParsingStep<?> stepsForPaginatedContent) {
        then(stepsForPaginatedContent);
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
