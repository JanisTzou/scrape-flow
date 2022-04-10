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

package com.github.scrape.flow.execution;

import com.github.scrape.flow.scraping.ScrapingStep;
import com.github.scrape.flow.scraping.ScrapingStepInternalReader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StepHierarchyRepository {

    private final Map<ScrapingStep<?>, StepMetadata> map;

    StepHierarchyRepository(Map<ScrapingStep<?>, StepMetadata> map) {
        this.map = new LinkedHashMap<>(map);
    }

    public StepMetadata getMetadataFor(ScrapingStep<?> step) {
        StepMetadata stepMetadata = map.get(step);
        if (stepMetadata == null) {
            throw new IllegalStateException("No metadata was found for step " + step);
        }
        return stepMetadata;
    }

    public static StepHierarchyRepository createFrom(ScrapingStep<?> rootStep) {
        final Map<ScrapingStep<?>, StepMetadata> map = new LinkedHashMap<>();
        StepOrder order = StepOrder.INITIAL;
        map.put(rootStep, createMeta(rootStep, order));
        traverse(rootStep, map, order);
        return new StepHierarchyRepository(map);
    }

    private static void traverse(ScrapingStep<?> parent, Map<ScrapingStep<?>, StepMetadata> map, StepOrder order) {
        ScrapingStepInternalReader<?> reader = getReader(parent);
        final List<ScrapingStep<?>> nextSteps = reader.getNextSteps();
        for (int i = 0; i < nextSteps.size(); i++) {
            if (i == 0) {
                order = order.nextAsChild();
            } else {
                order = order.nextAsSibling();
            }
            final ScrapingStep<?> next = nextSteps.get(i);
            map.put(next, createMeta(next, order));
            traverse(next, map, order);
        }
    }

    private static StepMetadata createMeta(ScrapingStep<?> rootStep, StepOrder order) {
        return new StepMetadata(rootStep, order, getReader(rootStep).getClientReservationType());
    }

    private static ScrapingStepInternalReader<?> getReader(ScrapingStep<?> parent) {
        return ScrapingStepInternalReader.of(parent);
    }

}
