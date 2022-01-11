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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.scrape.flow.debugging.DebuggingOptions;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.htmlunit.filters.FilterUtils;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class HtmlUnitStepHelper {

    private final HtmlUnitScrapingStep<?> step;
    private final NextStepsHandler nextStepsHandler;

    public HtmlUnitStepHelper(HtmlUnitScrapingStep<?> step) {
        this.step = step;
        this.nextStepsHandler = new NextStepsAsDefinedByUser();
    }

    public HtmlUnitStepHelper(HtmlUnitScrapingStep<?> step, NextStepsHandler nextStepsHandler) {
        this.step = step;
        this.nextStepsHandler = nextStepsHandler;
    }

    public void execute(ScrapingContext ctx,
                        Supplier<List<DomNode>> nodesSearch,
                        StepOrder currStepOrder,
                        StepExecutionCondition condition,
                        ScrapingServices services) {
        try {
            if (!condition.canExecute(step, ctx)) {
                return;
            }

            List<DomNode> foundNodes = nodesSearch.get();
            List<DomNode> filteredNodes = FilterUtils.filter(foundNodes, step.getFilters(), services.getGlobalDebugging());
            logFoundCount(currStepOrder, filteredNodes.size(), services.getGlobalDebugging());

            for (DomNode node : filteredNodes) {

                logNodeSourceCode(node, services.getGlobalDebugging());

                StepModelsHandler modelsHandler = StepModelsHandler.createFor(step);
                StepModels stepModels = modelsHandler.createAndAccumulateModels(currStepOrder, ctx.getContextModels());

                List<StepOrder> nextStepsOrders = executeNextSteps(currStepOrder, node, ctx, stepModels.getNextContextModels(), services);

                // TODO this step is what is missing when we call HtmlUnitSiteParser or NavigateToPage step ... from another step ... if it has a collector set to it ...
                //  decide which category of steps absolutely must use this and make it somehow nicely available ...
                if (!stepModels.getModelToPublishList().isEmpty()) { // important
                    services.getStepAndDataRelationshipTracker().track(currStepOrder, nextStepsOrders, stepModels.getModelToPublishList());
                    services.getScrapedDataPublisher().enqueueStepsToAwaitDataPublishing(nextStepsOrders);
                }
            }

        } catch (Exception e) {
            log.error("{} - {}: Error executing step", currStepOrder, step.getName(), e);
        }
    }


    private void logFoundCount(StepOrder currStepOrder, int count, DebuggingOptions globalDebugging) {
        if (globalDebugging.isLogFoundElementsCount() || step.getStepDebugging().isLogFoundElementsCount()) {
            log.info("{} - {}: found {} nodes", currStepOrder, step.getName(), count);
        }
    }


    private void logNodeSourceCode(DomNode node, DebuggingOptions globalDebugging) {
        if (!(node instanceof Page)
                && (globalDebugging.isLogFoundElementsSource() || step.getStepDebugging().isLogFoundElementsSource())
        ) {
            log.info("Source for step {} defined at line {} \n{}", step.getName(), step.getStepDeclarationLine(), node.asXml());
        }
    }


    private List<StepOrder> executeNextSteps(StepOrder currStepOrder,
                                             DomNode node,
                                             ScrapingContext ctx,
                                             ContextModels nextContextModels,
                                             ScrapingServices services) {
        // TODO have a nextStepContextHandler ?
        ScrapingContext nextCtx = new ScrapingContext(
                currStepOrder,
                node,
                nextContextModels.copy(),
                null, // TODO send parsed text as well? Probably not, the parsed text should be possible to access differently ... (through model)
                ctx.getParsedURL(),
                ctx.getRootLoopedStepOrder()
        );

        return nextStepsHandler.execute(step.getNextSteps(), nextCtx, services);
    }

}
