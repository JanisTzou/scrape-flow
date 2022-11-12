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

import com.github.scrape.flow.scraping.ClientType;
import com.github.scrape.flow.scraping.ScrapingStep;
import com.github.scrape.flow.scraping.ScrapingStepInternalAccessor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

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

    public static StepHierarchyRepository createFrom(ScrapingStep<?> firstStep) {
        Map<ScrapingStep<?>, StepMetadata> hierarchy = new LinkedHashMap<>();
        StepOrder first = StepOrder.ROOT.getFirstChild();
        Map<ClientType, Integer> loadingStepCount = getLoadingStepCountBase(firstStep);
        hierarchy.put(firstStep, createMeta(firstStep, first, loadingStepCount));
        traverseRecursively(firstStep, hierarchy, first, loadingStepCount);
        return new StepHierarchyRepository(hierarchy);
    }

    // TODO cyclic dependency check!
    private static void traverseRecursively(ScrapingStep<?> parent,
                                            Map<ScrapingStep<?>, StepMetadata> map,
                                            StepOrder order,
                                            Map<ClientType, Integer> loadingStepsCount) {
        ScrapingStepInternalAccessor<?> accessor = ScrapingStepInternalAccessor.of(parent);
        List<ScrapingStep<?>> nextSteps = new ArrayList<>();

        nextSteps.addAll(accessor.getAdditionalStepsExecutedBeforeNextSteps());
        nextSteps.addAll(accessor.getNextSteps());
        nextSteps.addAll(accessor.getAdditionalStepsExecutedAfterNextSteps());

        StepOrder nextOrder = order;
        for (int i = 0; i < nextSteps.size(); i++) {
            nextOrder = getStepOrder(nextOrder, i);
            ScrapingStep<?> next = nextSteps.get(i);
            Map<ClientType, Integer> newCounts = calcLoadingCounts(loadingStepsCount, next);
            map.put(next, createMeta(next, nextOrder, newCounts));
            traverseRecursively(next, map, nextOrder, newCounts);
        }
    }

    public int size() {
        return map.size();
    }

    public StepMetadata getMetadataFor(StepOrder hierarchyOrder) {
        StepMetadata stepMetadata = trie.get(getTrieKey(hierarchyOrder));
        if (stepMetadata == null) {
            throw new IllegalStateException("No metadata was found for hierarchyOrder " + hierarchyOrder);
        }
        return stepMetadata;
    }

    public StepMetadata getMetadataFor(ScrapingStep<?> step) {
        StepMetadata stepMetadata = map.get(step);
        if (stepMetadata == null) {
            throw new IllegalStateException("No metadata was found for step " + ScrapingStepInternalAccessor.of(step).getName() + " " + step);
        }
        return stepMetadata;
    }

    /**
     * @param startingHierarchyOrder the depth will be calculated from this step onwards (inclusive)
     */
    public int getRemainingLoadingStepsDepthMax(List<StepOrder> startingHierarchyOrder, ClientType clientType) {
        return startingHierarchyOrder.stream()
                .map(sho -> getRemainingLoadingPathDepth(sho, clientType))
                .max(Integer::compareTo)
                .orElse(0);
    }

    /**
     * @param startingHierarchyOrder the depth will be calculated from this step onwards (inclusive)
     */
    int getRemainingLoadingPathDepth(StepOrder startingHierarchyOrder, ClientType clientType) {
        StepMetadata startingMeta = this.trie.get(getTrieKey(startingHierarchyOrder));
        int longestLoadingPath = findLongestLoadingPathDepth(startingHierarchyOrder, clientType);
        int correction = clientType.equals(startingMeta.getClientType()) && startingMeta.getClientReservationType().isLoading() ? 1 : 0;
        return longestLoadingPath - startingMeta.getLoadingStepCountUpToThisStep(clientType) + correction;
    }

    /**
     * @param startingHierarchyOrder the depth will be calculated from this step onwards (inclusive)
     */
    int findLongestLoadingPathDepth(StepOrder startingHierarchyOrder, ClientType clientType) {
        SortedMap<String, StepMetadata> subHierarchy = this.trie.prefixMap(getTrieKey(startingHierarchyOrder));
        return subHierarchy.values().stream()
                .filter(sm -> sm.getClientType().equals(clientType))
                .max(StepMetadata.getComparatorByLoadingStepCount(clientType))
                .map(stepMetadata -> stepMetadata.getLoadingStepCountUpToThisStep(clientType))
                .orElse(0);
    }

    private static Map<ClientType, Integer> getLoadingStepCountBase(ScrapingStep<?> rootStep) {
        Map<ClientType, Integer> loadingStepCount = new HashMap<>();
        ScrapingStepInternalAccessor<?> accessor = ScrapingStepInternalAccessor.of(rootStep);
        if (accessor.getClientReservationType().isLoading()) {
            loadingStepCount.put(accessor.getClientType(), 1);
        } else {
            loadingStepCount.put(accessor.getClientType(), 0);
        }
        return loadingStepCount;
    }

    private static StepOrder getStepOrder(StepOrder order, int i) {
        if (i == 0) {
            order = order.getFirstChild();
        } else {
            order = order.getFollowingSibling();
        }
        return order;
    }

    private static Map<ClientType, Integer> calcLoadingCounts(Map<ClientType, Integer> loadingStepsCount, ScrapingStep<?> step) {
        ScrapingStepInternalAccessor<?> accessor = ScrapingStepInternalAccessor.of(step);
        if (accessor.getClientReservationType().isLoading()) {
            ClientType clientType = accessor.getClientType();
            HashMap<ClientType, Integer> copy = new HashMap<>(loadingStepsCount);
            copy.compute(clientType, (ct, count) -> {
                if (count == null) {
                    return 1;
                } else {
                    return ++count;
                }
            });
            return copy;
        }
        return loadingStepsCount;
    }

    private static StepMetadata createMeta(ScrapingStep<?> step, StepOrder order, Map<ClientType, Integer> loadingStepCountUpToThisStep) {
        return new StepMetadata(
                step,
                order,
                ScrapingStepInternalAccessor.of(step).getClientType(),
                ScrapingStepInternalAccessor.of(step).getClientReservationType(),
                loadingStepCountUpToThisStep
        );
    }

    private String getTrieKey(StepMetadata sm) {
        return getTrieKey(sm.getStepHierarchyOrder());
    }

    private String getTrieKey(StepOrder hierarchyOrder) {
        return hierarchyOrder.asString();
    }

}
