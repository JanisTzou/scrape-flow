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

import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;

import java.util.function.BiConsumer;

interface HtmlUnitCollectingToModelStep<C> {

    org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(HtmlUnitCollectingToModelStep.class);

    /**
     * Sets up the operation that will set the actual scraped data to model object
     */
    <T> C setCollector(BiConsumer<T, String> modelMutation);


    default void setParsedStringToModel(BiConsumer<Object, String> modelMutation, ParsingContext<?, ?> ctx, String parsedValue, String stepName) {
        try {
            if (modelMutation != null) {
                if (ctx.getModelWrapper() != null) {
                    ModelWrapper<?> modelWrapper = ctx.getModelWrapper();
                    if (modelWrapper.isAlreadyApplied(modelMutation)) {
                        log.error("Wrong parsed data collector setup detected in the step sequence related to model of class type '{}' and somewhere around step {}! " +
                                " The model collector should be declared lower in the set step sequence - at the step where the elements containing data for this model are searched for and provided", modelWrapper.getModel().getClass().getSimpleName(), stepName);
                    } else {
                        modelMutation.accept(modelWrapper.getModel(), parsedValue);
                        modelWrapper.addAppliedAccumulator(modelMutation);
                    }
                } else {
                    log.error("{}: ctx modelProxy is null but modelMutation exists! Check the setting of data collection model.", stepName);
                }
            }
        } catch (Exception e) {
            log.error("{}: failed to set parsed value to model", stepName, e);
        }
    }

}
