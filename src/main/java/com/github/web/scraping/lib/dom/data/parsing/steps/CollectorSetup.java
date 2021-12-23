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

import com.github.web.scraping.lib.parallelism.ParsedDataListener;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Getter
public class CollectorSetup {

    private final Supplier<?> modelSupplier;
    private final Class<?> modelClass;

    private final BiConsumer<Object, Object> accumulator; // <containerType, modelType>
    private final Class<?> containerClass;

    private final ParsedDataListener<Object> parsedDataListener;

//    private final boolean onlyCustomTypesAllowed; // TODO ...

    @SuppressWarnings("unchecked")
    public CollectorSetup(Supplier<?> modelSupplier,
                          Class<?> modelClass,
                          BiConsumer<?, ?> accumulator,
                          Class<?> containerClass,
                          ParsedDataListener<?> parsedDataListener) {
        this.modelSupplier = modelSupplier;
        this.accumulator = (BiConsumer<Object, Object>) accumulator;
        this.modelClass = modelClass;
        this.containerClass = containerClass;
        this.parsedDataListener = (ParsedDataListener<Object>) parsedDataListener;
    }

    public CollectorSetup(Supplier<?> modelSupplier, Class<?> modelClass) {
        this(modelSupplier, modelClass, null, null, null);
    }

    public CollectorSetup(Supplier<?> modelSupplier, Class<?> modelClass, ParsedDataListener<?> parsedDataListener) {
        this(modelSupplier, modelClass, null, null, parsedDataListener);
    }

    public CollectorSetup(BiConsumer<?, ?> accumulator, Class<?> modelClass, Class<?> containerClass) {
        this(null, modelClass, accumulator, containerClass, null);
    }

}
