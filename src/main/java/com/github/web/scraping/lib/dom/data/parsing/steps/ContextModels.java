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

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds the generated models in a sequence of step executions so they are accessible from the parsingContext
 */
public class ContextModels {

    @Nonnull
    private final ArrayDeque<ModelWrapper> deque;

    public ContextModels() {
        this.deque = new ArrayDeque<>();
    }

    private ContextModels(ArrayDeque<ModelWrapper> deque) {
        this.deque = Objects.requireNonNullElse(deque, new ArrayDeque<>());
    }

    public synchronized ContextModels copy() {
        return new ContextModels(this.deque.clone());
    }

    public synchronized void push(Object model, Class<?> modelClass) {
        deque.push(new ModelWrapper(model, modelClass));
    }

    public synchronized  <T> Optional<ModelWrapper> getModelFor(Class<T> modelClazz) {
        return deque.stream()
                .filter(mw -> mw.getModelClass().equals(modelClazz))
                .findFirst();
    }

}
