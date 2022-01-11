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

import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.data.collectors.Collectors;
import com.github.scrape.flow.data.collectors.ModelWrapper;
import com.github.scrape.flow.data.publishing.ModelToPublish;
import com.github.scrape.flow.data.publishing.ScrapedDataListener;
import com.github.scrape.flow.execution.StepOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@Log4j2
@RequiredArgsConstructor
public class StepModelsHandler {

    private final String stepName;
    private final Class<?> stepType;
    private final Collectors stepCollectors;

    public static StepModelsHandler createFor(HtmlUnitScrapingStep<?> step) {
        return new StepModelsHandler(step.getName(), step.getClass(), step.getCollectors());
    }

    StepModels createAndAccumulateModels(StepOrder currStepOrder, ContextModels currContextModels) {

        ContextModels nextContextModels = currContextModels.copy();
        List<ModelToPublish> modelToPublishList = new ArrayList<>();

        // generate models
        for (Collector co : stepCollectors.getModelSuppliers()) {
            Object model = co.getModelSupplier().get();
            Class<?> modelClass = co.getModelClass();
            ScrapedDataListener<Object> scrapedDataListener = co.getScrapedDataListener();
            if (scrapedDataListener != null) {
                modelToPublishList.add(new ModelToPublish(model, modelClass, scrapedDataListener));
            }
            nextContextModels.push(model, modelClass);
        }

        // populate containers with generated models ...
        for (Collector op : stepCollectors.getAccumulators()) {
            BiConsumer<Object, Object> accumulator = op.getAccumulator();

            Class<?> containerClass = op.getContainerClass();
            Class<?> modelClass = op.getModelClass();

            Optional<ModelWrapper> container = nextContextModels.getModelFor(containerClass);
            Optional<ModelWrapper> accumulatedModel = nextContextModels.getModelFor(modelClass);

            if (container.isPresent() && accumulatedModel.isPresent()) {
                accumulator.accept(container.get().getModel(), accumulatedModel.get().getModel());
            } else if (container.isPresent()) {
                if (CollectingParsedValueToModelStep.class.isAssignableFrom(stepType)) {
                    // has its own handling ...
                } else {
                    log.warn("{} - {}: Failed to find modelWrappers for containerClass and/or modelClass!", currStepOrder, stepName);
                }
            }
        }

        return new StepModels(modelToPublishList, nextContextModels);
    }

}
