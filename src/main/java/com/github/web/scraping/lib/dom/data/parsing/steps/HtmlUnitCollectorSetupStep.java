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

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface HtmlUnitCollectorSetupStep<C> {


    /**
     * Sets up custom models that will be used to store the scraped data
     * @param modelSupplier a factory for new model instances that parsed data can be set to in the steps following this one
     * @param containerSupplier a factory for containers that will be used to store model instances populated with parsed data
     * @param accumulator operation that inserts a model instance into the container
     */
    <R, T> C setCollector(Supplier<T> modelSupplier, Supplier<R> containerSupplier, BiConsumer<R, T> accumulator);

    /**
     * Sets up custom models that will be used to store the scraped data.
     * The supplied model instance will be set/inserted into the model of the previous step
     * @param modelSupplier a factory for new model instances that parsed data can be set to in the steps following this one
     * @param accumulator operation that inserts a model instance into the container
     */
    <R, T> C setCollector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator);

    <R, T> C setCollector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator, ParsedDataListener<T> parsedDataListener);

    <R, T> C setCollector(Supplier<T> modelSupplier,  Supplier<R> containerSupplier, BiConsumer<R, T> accumulator, ParsedDataListener<T> parsedDataListener);

    /**
     * checks invariants
     */
    default void mustBeNull(Collecting<?, ?> collecting) {
        if (collecting != null) {
            throw new IllegalStateException("Collection has already been set");
        }
    }

}
