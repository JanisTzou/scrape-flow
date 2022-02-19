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

import com.github.scrape.flow.data.collectors.ModelWrapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;
import java.util.function.Predicate;

@Getter
@Log4j2
class ExecuteStepByModelDataCondition implements StepExecutionCondition {

    private final Predicate<Object> predicate;
    private final Class<?> modelType;

    @SuppressWarnings("unchecked")
    public ExecuteStepByModelDataCondition(Predicate<?> predicate, Class<?> modelType) {
        this.predicate = (Predicate<Object>) predicate;
        this.modelType = modelType;
    }

    @Override
    public boolean canExecute(ScrapingStep<?> step, ContextModels contextModels) {
        try {
            Optional<ModelWrapper> model = contextModels.getModelFor(modelType);
            if (model.isPresent()) {
                log.trace("{}: Found model and will execute condition", step.getName());
                boolean canExecute = predicate.test(model.get().getModel());
                if (canExecute) {
                    return true;
                }
            } else {
                log.error("No model is set up for parsed value in step {}! Cannot execute step conditionally based on it!", step.getName());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error evaluating execution condition for step: {} - step will not run", step.getName(), e);
            return false;
        }
    }

}
