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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Tracks all the steps that are still 'active' - they might be executing or waiting to be executed
 */
@Log4j2
public class ActiveStepsTracker {

    private final Trie<String, TrackedStepOrder> patriciaTrie = new PatriciaTrie<>();
    private final SortedMap<StepOrder, TrackedStepOrder> sortedMap = new TreeMap<>(StepOrder.NATURAL_COMPARATOR);

    /**
     * @return true if this or any child step is still being tracked; false otherwise
     */
    public synchronized boolean isPartOfActiveStepSequence(StepOrder stepOrder) {
        return patriciaTrie.prefixMap(getKey(stepOrder)).size() > 0;
    }

    public synchronized boolean isActive(StepOrder stepOrder) {
        return patriciaTrie.get(getKey(stepOrder)) != null;
    }

    public synchronized void track(StepOrder stepOrder, String stepName, StepOrder hierarchyOrder) {
        log.debug("tracking {} - {}", stepOrder, stepName);
        TrackedStepOrder tracked = new TrackedStepOrder(stepOrder, stepName, hierarchyOrder);
        patriciaTrie.put(getKey(stepOrder), tracked);
        sortedMap.put(stepOrder, tracked);
    }

    public synchronized void untrack(StepOrder stepOrder) {
        log.debug("untracked {}", stepOrder);
        patriciaTrie.remove(getKey(stepOrder)); // maybe enough ?
        sortedMap.remove(stepOrder);
    }

    public synchronized SortedMap<StepOrder, TrackedStepOrder> getPrecedingSteps(StepOrder stepOrder) {
        return new TreeMap<>(sortedMap.headMap(stepOrder));
    }

    private String getKey(StepOrder stepOrder) {
        return stepOrder.asString();
    }

    // TODO maybe here we could have info about the loading type and the level out of x max levels
    @RequiredArgsConstructor
    @Getter
    public static class TrackedStepOrder {
        private final StepOrder stepOrder;
        private final String stepName;
        private final StepOrder hierarchyOrder;
    }


}
