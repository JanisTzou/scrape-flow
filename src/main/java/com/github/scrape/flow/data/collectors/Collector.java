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

package com.github.scrape.flow.data.collectors;

import com.github.scrape.flow.data.publishing.ScrapedDataListener;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Getter
public class Collector {

    private final Supplier<?> modelSupplier;
    private final Class<?> modelClass;

    private final AccumulatorType accumulatorType;
    private final BiConsumer<Object, Object> accumulator; // <containerType, modelType>
    private final Class<?> containerClass;

    private final ScrapedDataListener<Object> scrapedDataListener;

//    private final boolean onlyCustomTypesAllowed; // TODO ...

    // when this collects the single values parsed directly from the site
//    private final CollectedModelType collectedModelType;

    @SuppressWarnings("unchecked")
    public Collector(Supplier<?> modelSupplier,
                     Class<?> modelClass,
                     AccumulatorType accumulatorType,
                     BiConsumer<?, ?> accumulator,
                     Class<?> containerClass,
                     ScrapedDataListener<?> scrapedDataListener) {
        this.modelSupplier = modelSupplier;
        this.accumulatorType = accumulatorType;
        this.accumulator = (BiConsumer<Object, Object>) accumulator;
        this.modelClass = modelClass;
        this.containerClass = containerClass;
        this.scrapedDataListener = (ScrapedDataListener<Object>) scrapedDataListener;
    }

    public Collector(Supplier<?> modelSupplier, Class<?> modelClass) {
        this(modelSupplier, modelClass, null, null, null, null);
    }

    public Collector(Supplier<?> modelSupplier, Class<?> modelClass, ScrapedDataListener<?> scrapedDataListener) {
        this(modelSupplier, modelClass, null, null, null, scrapedDataListener);
    }

    public Collector(BiConsumer<?, ?> accumulator, Class<?> modelClass, Class<?> containerClass, AccumulatorType accumulatorType) {
        this(null, modelClass, accumulatorType, accumulator, containerClass, null);
    }

    public enum AccumulatorType {
        ONE,
        MANY
    }

    public enum CollectedModelType {
        // models/types that the lib used defines to store the parsed data
        GENERATED_USED_DEFINED,

        // the actual single values parsed from the sites - most often Strings
        PARSED
    }

}
