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

package com.github.web.scraping.lib.scraping.htmlunit;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.github.web.scraping.lib.scraping.htmlunit.CollectorSetup.AccumulatorType;

interface HtmlUnitStepCollectingParsedValueToModel<C, V> {

    org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(HtmlUnitStepCollectingParsedValueToModel.class);

    /**
     * specialisation of {@link HtmlUnitStepSupportingCollection#collectOne(BiConsumer, Class, Class)}
     *
     * @return a copy of this step
     */
    <T> C collectOne(BiConsumer<T, V> modelMutation, Class<T> containerType);

    /**
     * specialisation of {@link HtmlUnitStepSupportingCollection#collectMany(BiConsumer, Class, Class)}
     *
     * @return a copy of this step
     */
    <T> C collectMany(BiConsumer<T, V> modelMutation, Class<T> containerType);


    default <T> void setParsedValueToModel(CollectorSetups collectorSetups, ScrapingContext ctx, T parsedValue, String stepName, StackTraceElement stepDeclarationLine) {
        try {
            List<CollectorSetup> stringCollectors = collectorSetups.getAccumulators().stream()
                    .filter(co -> co.getModelClass().equals(parsedValue.getClass()))
                    .collect(Collectors.toList());

            for (CollectorSetup collectorSetup : stringCollectors) {
                Class<?> contCls = collectorSetup.getContainerClass();
                AccumulatorType accumulatorType = collectorSetup.getAccumulatorType();
                Optional<ModelWrapper> model = ctx.getContextModels().getModelFor(contCls);
                if (model.isPresent()) {
                    boolean valueIllegallySetMultipleTimes = accumulatorType.equals(AccumulatorType.ONE) && model.get().isAlreadyApplied(collectorSetups);
                    if (valueIllegallySetMultipleTimes) {
                        log.error("Wrong parsed data collector setup detected in the step sequence related to model of class type '{}' related to line: {}! " +
                                " The model collector should be declared lower in the set step sequence - at the step where the elements containing data for this model are searched for and provided", model.get().getModel().getClass().getSimpleName(), stepDeclarationLine);
                    } else {
                        collectorSetup.getAccumulator().accept(model.get().getModel(), parsedValue);
                        model.get().addAppliedAccumulator(collectorSetups);
                    }
                } else {
                    log.error("No model is set up for parsed value in step {}! Cannot collect parsed data to it!", stepName);
                }
            }

            if (stringCollectors.isEmpty()) {
                // this is ok ... when we e.g. parse HRef but do not put it here ...
                log.debug("No collecting operation is set up for parsed value in step {}! Cannot collect parsed data!", stepName);
            }

        } catch (Exception e) {
            log.error("{}: failed to set parsed value to model", stepName, e);
        }
    }

}
