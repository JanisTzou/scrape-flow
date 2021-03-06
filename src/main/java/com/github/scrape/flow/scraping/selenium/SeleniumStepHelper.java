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

package com.github.scrape.flow.scraping.selenium;

import com.github.scrape.flow.debugging.DebuggingOptions;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.*;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class SeleniumStepHelper extends StepHelperBase {

    private final SeleniumScrapingStep<?> step;
    private final NextStepsHandler nextStepsHandler;

    public SeleniumStepHelper(SeleniumScrapingStep<?> step) {
        this.step = step;
        this.nextStepsHandler = new NextStepsAsDefinedByUser();
    }

    public SeleniumStepHelper(SeleniumScrapingStep<?> step, NextStepsHandler nextStepsHandler) {
        this.step = step;
        this.nextStepsHandler = nextStepsHandler;
    }

    public void execute(ScrapingContext ctx,
                        // TODO remove ...
                        Supplier<List<WebElement>> elementsSearch,
                        StepOrder currStepOrder,
                        StepExecutionCondition condition,
                        ScrapingServices services) {
        try {
            if (!condition.canExecute(step.getName(), ctx.getContextModels())) {
                return;
            }

            List<WebElement> foundElements = elementsSearch.get();
            List<WebElement> filteredElements = FilterUtils.filter(foundElements, step.getFilters(), services.getGlobalDebugging());
            logFoundCount(step.getName(), currStepOrder, filteredElements.size(), services.getGlobalDebugging(), ScrapingStepInternalReader.of(step).getStepDebugging());

            for (WebElement elem : filteredElements) {

                logNodeSourceCode(elem, services.getGlobalDebugging());

                StepModelsHandler modelsHandler = StepModelsHandler.createFor(step);
                StepModels stepModels = modelsHandler.createAndAccumulateModels(currStepOrder, ctx.getContextModels());

                SpawnedSteps spawnedSteps = executeNextSteps(currStepOrder, elem, ctx, stepModels.getNextContextModels(), services);

                handleModels(currStepOrder, services, stepModels, spawnedSteps);
            }

        } catch (Exception e) {
            log.error("{} - {}: Error executing step", currStepOrder, step.getName(), e);
        }
    }


    private void logNodeSourceCode(WebElement element, DebuggingOptions globalDebugging) {
        if (globalDebugging.isLogFoundElementsSource()) {
            // TODO is this even possible ? Seems not to be ... if yes, then only log elements that are not the root .. html tag ...
            log.info("Source for step {} \n{}", step.getName(), element);
        }
    }


    private SpawnedSteps executeNextSteps(StepOrder currStepOrder,
                                          WebElement webElement,
                                          ScrapingContext ctx,
                                          ContextModels nextContextModels,
                                          ScrapingServices services) {
        ScrapingContext nextCtx = new ScrapingContext(
                currStepOrder,
                null,
                webElement,
                nextContextModels.copy(),
                // TODO send parsed text as well? Probably not, the parsed text should be possible to access differently ... (through model)
                ctx.getParsedURL(),
                ctx.getRootLoopedStepOrder()
        );

        // TODO associate web driver id with step order, here?
        //  if yes, then do so only for steps that do not load page? (... those will get their own driver doen the line ...)

        return nextStepsHandler.execute(ScrapingStepInternalReader.of(step).getNextSteps(), currStepOrder, nextCtx, services);
    }

}
