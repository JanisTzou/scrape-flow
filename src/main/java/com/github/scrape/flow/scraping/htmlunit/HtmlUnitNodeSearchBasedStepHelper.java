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
import com.github.scrape.flow.scraping.*;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class HtmlUnitNodeSearchBasedStepHelper extends StepHelperBase {

    private final String stepName;
    private final NextStepsHandler nextStepsHandler;
    private final DebuggingOptions globalDebugging;
    private final DebuggingOptions stepDebugging;
    private final List<Filter<DomNode>> filters;
    private final List<ScrapingStep<?>> nextSteps;
    private final StepModelsHandler modelsHandler;
    private final StepExecutionCondition executionCondition;
    private final ScrapingServices services;

    public static HtmlUnitNodeSearchBasedStepHelper createFor(HtmlUnitScrapingStep<?> step, DebuggingOptions globalDebugging, ScrapingServices services) {
        return createFor(step, globalDebugging, services, new NextStepsAsDefinedByUser());
    }

    public static HtmlUnitNodeSearchBasedStepHelper createFor(HtmlUnitScrapingStep<?> step, DebuggingOptions globalDebugging, ScrapingServices services, NextStepsHandler nextStepsHandler) {
        return new HtmlUnitNodeSearchBasedStepHelper(
                step.getName(),
                nextStepsHandler,
                globalDebugging,
                ScrapingStepInternalReader.of(step).getStepDebugging(),
                step.getFilters(),
                ScrapingStepInternalReader.of(step).getNextSteps(),
                StepModelsHandler.createFor(step),
                ScrapingStepInternalReader.of(step).getExecuteIf(),
                services
        );
    }

    public HtmlUnitNodeSearchBasedStepHelper(String stepName,
                                             NextStepsHandler nextStepsHandler,
                                             DebuggingOptions globalDebugging,
                                             DebuggingOptions stepDebugging,
                                             List<Filter<DomNode>> filters,
                                             List<ScrapingStep<?>> nextSteps,
                                             StepModelsHandler modelsHandler,
                                             StepExecutionCondition executionCondition,
                                             ScrapingServices services) {
        this.stepName = stepName;
        this.nextStepsHandler = nextStepsHandler;
        this.globalDebugging = globalDebugging;
        this.stepDebugging = stepDebugging;
        this.filters = filters;
        this.nextSteps = nextSteps;
        this.modelsHandler = modelsHandler;
        this.executionCondition = executionCondition;
        this.services = services;
    }

    public void execute(Supplier<List<DomNode>> nodesSearch, ScrapingContext ctx, StepOrder currStepOrder) {
        try {
            if (!executionCondition.canExecute(stepName, ctx.getContextModels())) {
                return;
            }

            List<DomNode> foundNodes = nodesSearch.get();
            List<DomNode> filteredNodes = FilterUtils.filter(foundNodes, filters, globalDebugging);
            logFoundCount(stepName, currStepOrder, filteredNodes.size(), globalDebugging, stepDebugging);

            for (DomNode node : filteredNodes) {

                logNodeSourceCode(node, globalDebugging);

                StepModels stepModels = modelsHandler.createAndAccumulateModels(currStepOrder, ctx.getContextModels());

                SpawnedSteps spawnedSteps = executeNextSteps(currStepOrder, node, ctx, stepModels.getNextContextModels(), services);

                handleModels(currStepOrder, services, stepModels, spawnedSteps);
            }

        } catch (Exception e) {
            log.error("{} - {}: Error executing step", currStepOrder, stepName, e);
        }
    }


    private void logNodeSourceCode(DomNode node, DebuggingOptions globalDebugging) {
        if (!(node instanceof Page)
                && (globalDebugging.isLogFoundElementsSource() || stepDebugging.isLogFoundElementsSource())
        ) {
            log.info("Source for step {} \n{}", stepName, node.asXml());
        }
    }


    private SpawnedSteps executeNextSteps(StepOrder currStepOrder,
                                          DomNode node,
                                          ScrapingContext ctx,
                                          ContextModels nextContextModels,
                                          ScrapingServices services) {
        ScrapingContext nextCtx = new ScrapingContext(
                currStepOrder,
                node,
                null,
                nextContextModels.copy(),
                ctx.getParsedURL(),
                ctx.getRootLoopedStepOrder()
        );

        return nextStepsHandler.execute(nextSteps, currStepOrder, nextCtx, services);
    }

}
