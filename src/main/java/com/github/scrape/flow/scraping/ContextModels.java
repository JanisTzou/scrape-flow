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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Holds the generated models in a sequence of step executions so they are accessible from the parsingContext.
 * Access is synchronised to insure data visibility when switching threads
 */
public class ContextModels {

    private final List<ModelWrapper> models;

    public ContextModels() {
        this.models = new CopyOnWriteArrayList<>();
    }

    private ContextModels(List<ModelWrapper> models) {
        this();
        this.models.addAll(models);
    }

    public ContextModels copy() {
        return new ContextModels(this.models);
    }

    public void add(Object model, Class<?> modelType) {
        models.add(new ModelWrapper(model, modelType));
    }

    public <T> Optional<ModelWrapper> getModelFor(Class<T> modelType) {
        return models.stream()
                .filter(mw -> mw.getModelClass().equals(modelType))
                .findFirst();
    }

}
