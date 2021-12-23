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

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wrapper around the model object that ensures that when the model returns populated with data
 * it is collected only once (thus avoiding duplicates in resulting collections od data)
 */
@Getter
@Setter
public class ModelWrapper {

    private final Object model;
    private final Class<?> modelClass;

    public ModelWrapper(Object model, Class<?> modelClass) {
        this.model = model;
        this.modelClass = modelClass;
    }

    /**
     * Stores all methods used to store parsed data into the model object, so we can provide warnings that a model field is overwritten multiple times.
     * Such a case would mean that the model declaration as a collector in the step sequence is misplaced and should be placed lower down the steps sequence
     */
    private Set<Object> appliedAccumulators = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public boolean isAlreadyApplied(Object accumulator) {
        return appliedAccumulators.contains(accumulator);
    }

    public void addAppliedAccumulator(Object object) {
        this.appliedAccumulators.add(object);
    }

}
