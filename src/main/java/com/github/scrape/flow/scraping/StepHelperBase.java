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

package com.github.scrape.flow.scraping;

import com.github.scrape.flow.debugging.DebuggingOptions;
import com.github.scrape.flow.execution.StepOrder;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class StepHelperBase {

    protected void handleModels(StepOrder currStepOrder, ScrapingServices services, StepModels stepModels, SpawnedSteps spawnedSteps) {
        // TODO this step is what is missing when we call HtmlUnitSiteParser or NavigateToPage step ... from another step ... if it has a collector set to it ...
        //  decide which category of steps absolutely must use this and make it somehow nicely available ...
        if (!stepModels.getModelToPublishList().isEmpty()) { // important
            services.getStepAndDataRelationshipTracker().track(currStepOrder, spawnedSteps.getSteps(), stepModels.getModelToPublishList());
            services.getScrapedDataPublisher().enqueueStepsToAwaitDataPublishing(spawnedSteps);
        }
    }


    protected void logFoundCount(String stepName, StepOrder currStepOrder, int count, DebuggingOptions globalDebugging, DebuggingOptions stepDebugging) {
        if (globalDebugging.isLogFoundElementsCount() || stepDebugging.isLogFoundElementsCount()) {
            log.info("{} - {}: found {} nodes/elements", currStepOrder, stepName, count);
        }
    }

}
