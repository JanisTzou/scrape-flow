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

package com.github.web.scraping.lib.parallelism;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

/**
 * Tracks all the steps that are still active - they might be executing or waiting to be executed
 */
@Log4j2
public class ActiveStepsTracker {

    private final Trie<String, TrackedStepOrder> patriciaTrie = new PatriciaTrie<>();

    /**
     * @return true if this or any child step is still being tracked; false otherwise
     */
    public synchronized boolean isPartOfActiveStepSequence(StepExecOrder stepOrder) {
        return patriciaTrie.prefixMap(stepOrder.asString()).size() > 0;
    }

    public synchronized boolean isActive(StepExecOrder stepOrder) {
        return patriciaTrie.get(stepOrder.asString()) != null;
    }

    public synchronized void track(StepExecOrder stepExecOrder, String name) {
        log.debug("tracking {} - {}", stepExecOrder, name);
        patriciaTrie.put(stepExecOrder.asString(), new TrackedStepOrder(stepExecOrder, name));
    }

    public synchronized void untrack(StepExecOrder stepExecOrder) {
        log.debug("untracked {}", stepExecOrder);
        patriciaTrie.remove(stepExecOrder.asString()); // maybe enough ?
    }


    @Deprecated
    @RequiredArgsConstructor
    public static class TrackedStepOrder {
        private final StepExecOrder stepExecOrder;
        private final String name;
    }


}
