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

import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.data.collectors.Collectors;
import com.github.scrape.flow.data.collectors.ModelWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static com.github.scrape.flow.data.collectors.Collector.AccumulatorType;

public class ParsedValueToModelCollector {

    private static Logger log = LogManager.getLogger(ParsedValueToModelCollector.class);

    public static <T> void setParsedValueToModel(Collectors collectors, ScrapingContext ctx, T parsedValue, String stepName) {
        try {
            if (parsedValue == null) {
                log.warn("{}: Parsed value is null -> cannot set to model ...", stepName);
            }
            List<Collector> cols = getCollectorsWithAccumulatorsForParsedValueType(collectors, parsedValue);

            for (Collector col : cols) {
                Optional<ModelWrapper> mw = ctx.getContextModels().getModelFor(col.getContainerClass());
                if (mw.isPresent()) {
                    boolean valueIllegallySetMultipleTimes = col.getAccumulatorType().equals(AccumulatorType.ONE) && mw.get().isAlreadyApplied(collectors);
                    if (valueIllegallySetMultipleTimes) {
                        log.error("Wrong parsed data collector setup detected in the step sequence related to model of class type '{}'! " +
                                " The model collector should be declared lower in the set step sequence - at the step where the elements containing data for this model are searched for and provided; Scraped value was: {}", mw.get().getModel().getClass().getSimpleName(), parsedValue);
                    } else {
                        col.getAccumulator().accept(mw.get().getModel(), parsedValue);
                        mw.get().addAppliedAccumulator(collectors);
                    }
                } else {
                    log.error("No model is set up for parsed value in step {}! Cannot collect parsed data to it!", stepName);
                }
            }

        } catch (Exception e) {
            log.error("{}: failed to set parsed value to model", stepName, e);
        }
    }

    @Nonnull
    private static <T> List<Collector> getCollectorsWithAccumulatorsForParsedValueType(Collectors collectors, T parsedValue) {
        return collectors.getAccumulators().stream()
                .filter(co -> co.getModelClass().equals(parsedValue.getClass()))
                .collect(java.util.stream.Collectors.toList());
    }

}
