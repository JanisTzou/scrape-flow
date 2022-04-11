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
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;

@Log4j2
public class StepHierarchyRepository {

    private final Map<ScrapingStep<?>, StepMetadata> map;
    private final Trie<String, StepMetadata> trie;

    StepHierarchyRepository(Map<ScrapingStep<?>, StepMetadata> map) {
        this.map = new LinkedHashMap<>(map);
        this.trie = new PatriciaTrie<>();
        map.values().forEach(sm -> this.trie.put(getTrieKey(sm), sm));
    }

    public static StepHierarchyRepository createFrom(ScrapingStep<?> rootStep) {
        final Map<ScrapingStep<?>, StepMetadata> map = new LinkedHashMap<>();
        StepOrder order = StepOrder.INITIAL;
        int loadingStepCount = getReader(rootStep).getClientReservationType().isLoading() ? 1 : 0;
        map.put(rootStep, createMeta(rootStep, order, loadingStepCount));
        traverseRecursively(rootStep, map, order, loadingStepCount);
        return new StepHierarchyRepository(map);
    }

    private String getTrieKey(StepMetadata sm) {
        return getTrieKey(sm.getStepHierarchyOrder());
    }

    private String getTrieKey(StepOrder hierarchyOrder) {
        return hierarchyOrder.asString();
    }

    public StepMetadata getMetadataFor(StepOrder hierarchyOrder) {
        StepMetadata stepMetadata = trie.get(getTrieKey(hierarchyOrder));
        if (stepMetadata == null) {
            throw new IllegalStateException("No metadata was found for hierarchyOrder " + hierarchyOrder);
        }
        return stepMetadata;
    }


    public int getRemainingLoadingStepsDepthMax(List<StepOrder> startingHierarchyOrder) {
        return startingHierarchyOrder.stream()
                .map(this::getRemainingLoadingStepsDepth)
                .max(Integer::compareTo)
                .orElse(0);
    }

    /**
     * @param startingHierarchyOrder the depth will be calculated from this step onwards (inclusive)
     */
    public int getRemainingLoadingStepsDepth(StepOrder startingHierarchyOrder) {
        StepMetadata startingMeta = this.trie.get(getTrieKey(startingHierarchyOrder));
        int longestLoadingPath = findLongestLoadingPath(startingHierarchyOrder);
        if (startingMeta.getClientReservationType().isLoading()) {
            return longestLoadingPath - startingMeta.getLoadingStepCountUpToThisStep() + 1; // we want to include the starting step if it is loading
        } else {
            return longestLoadingPath - startingMeta.getLoadingStepCountUpToThisStep();
        }
    }

    private int findLongestLoadingPath(StepOrder hierarchyOrder) {
        SortedMap<String, StepMetadata> subHierarchy = this.trie.prefixMap(getTrieKey(hierarchyOrder));
        Optional<StepMetadata> max = subHierarchy.values().stream().max(StepMetadata.COMPARATOR_BY_LOADING_STEP_COUNT);
        if (max.isPresent()) {
            return max.get().getLoadingStepCountUpToThisStep();
        } else {
            throw new FlowException("Failed to find max StepMetadata!");
        }
    }

    public StepMetadata getMetadataFor(ScrapingStep<?> step) {
        StepMetadata stepMetadata = map.get(step);
        if (stepMetadata == null) {
            throw new IllegalStateException("No metadata was found for step " + step);
        }
        return stepMetadata;
    }

    // TODO cyclic dependency check!
    private static void traverseRecursively(ScrapingStep<?> parent, Map<ScrapingStep<?>, StepMetadata> map, StepOrder order, int loadingStepsCount) {
        ScrapingStepInternalReader<?> reader = getReader(parent);
        final List<ScrapingStep<?>> nextSteps = reader.getNextSteps();
        for (int i = 0; i < nextSteps.size(); i++) {
            if (i == 0) {
                order = order.nextAsChild();
            } else {
                order = order.nextAsSibling();
            }
            final ScrapingStep<?> next = nextSteps.get(i);
            if (getReader(next).getClientReservationType().isLoading()) {
                loadingStepsCount++;
            }
            map.put(next, createMeta(next, order, loadingStepsCount));
            traverseRecursively(next, map, order, loadingStepsCount);
        }
    }

    private static StepMetadata createMeta(ScrapingStep<?> rootStep, StepOrder order, int loadingStepCountUpToThisStep) {
        return new StepMetadata(rootStep, order, getReader(rootStep).getClientReservationType(), loadingStepCountUpToThisStep);
    }

    private static ScrapingStepInternalReader<?> getReader(ScrapingStep<?> parent) {
        return ScrapingStepInternalReader.of(parent);
    }

}
