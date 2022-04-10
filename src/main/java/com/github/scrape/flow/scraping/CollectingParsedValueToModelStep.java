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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;


public interface CollectingParsedValueToModelStep<C, V> {

    Logger log = LogManager.getLogger(CollectingParsedValueToModelStep.class);

    /**
     * specialisation of {@link CollectingStep#collectValue(BiConsumer, Class, Class)}
     *
     * @return a copy of this step
     */
    <T> C collectValue(BiConsumer<T, V> modelMutation, Class<T> containerType);

    /**
     * specialisation of {@link CollectingStep#collectValues(BiConsumer, Class, Class)}
     *
     * @return a copy of this step
     */
    <T> C collectValues(BiConsumer<T, V> modelMutation, Class<T> containerType);

}
