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
public class HtmlUnitStepHelper extends StepHelperBase {

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
            if (!condition.canExecute(step, ctx.getContextModels())) {
                return;
            }

            List<DomNode> foundNodes = nodesSearch.get();
            List<DomNode> filteredNodes = FilterUtils.filter(foundNodes, step.getFilters(), services.getGlobalDebugging());
            logFoundCount(currStepOrder, filteredNodes.size(), services.getGlobalDebugging(), step);

            for (DomNode node : filteredNodes) {

                logNodeSourceCode(node, services.getGlobalDebugging());

                StepModelsHandler modelsHandler = StepModelsHandler.createFor(step);
                StepModels stepModels = modelsHandler.createAndAccumulateModels(currStepOrder, ctx.getContextModels());

                SpawnedSteps spawnedSteps = executeNextSteps(currStepOrder, node, ctx, stepModels.getNextContextModels(), services);

                handleModels(currStepOrder, services, stepModels, spawnedSteps);
            }

        } catch (Exception e) {
            log.error("{} - {}: Error executing step", currStepOrder, step.getName(), e);
        }
    }


    private void logNodeSourceCode(DomNode node, DebuggingOptions globalDebugging) {
        if (!(node instanceof Page)
                && (globalDebugging.isLogFoundElementsSource() || ScrapingStepInternalProxy.of(step).getStepDebugging().isLogFoundElementsSource())
        ) {
            log.info("Source for step {} defined at line {} \n{}", step.getName(), ScrapingStepInternalProxy.of(step).getStepDeclarationLine(), node.asXml());
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
                null,
                nextContextModels.copy(),
                ctx.getParsedURL(),
                ctx.getRootLoopedStepOrder()
        );

        return nextStepsHandler.execute(currStepOrder, ScrapingStepInternalProxy.of(step).getNextSteps(), nextCtx, services);
    }

}
