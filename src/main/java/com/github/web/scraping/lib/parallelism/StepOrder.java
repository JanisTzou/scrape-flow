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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO explain notions of sibling, parent and child ...
 */
public class StepOrder {

    public static final StepOrder INITIAL = new StepOrder(List.of(0));

    public static final Comparator<StepOrder> NATURAL_COMPARATOR = (so1, so2) -> {
        List<StepOrder> sortedBySize = Stream.of(so1, so2).sorted(Comparator.comparingInt(StepOrder::size)).collect(Collectors.toList());
        StepOrder smaller = sortedBySize.get(0);
        StepOrder bigger = sortedBySize.get(1);
        for (int idx = 0; idx < smaller.values.size(); idx++) {
            Integer sVal = smaller.values.get(idx);
            Integer bVal = bigger.values.get(idx);
            int compared = Integer.compare(sVal, bVal);
            if (compared != 0) {
                return compared;
            }
        }

        // if we got here the StepOrder instances are either same length or they are equal up to the length of the smaller one
        if (bigger.size() == smaller.size()) {
          return 0;
        } else if (so1 == smaller) {
            return -1;
        } else {
            return 1;
        }
    };

    /**
     * Contains a list of values where the idx of the value in the list represents the order of the step among steps at different levels in a hierarchy of steps.
     * The value itself represents the order among steps as the same level of a hierarchy of steps
     */
    private final List<Integer> values = new ArrayList<>();

    StepOrder(Integer ... values) {
        this.values.addAll(Arrays.asList(values));
    }

    private StepOrder(List<Integer> values) {
        this.values.addAll(values); // important to create a copy!
    }

    StepOrder nextAsSibling() {
        int lastIdx = values.size() - 1;
        int newOrder = 1 + values.get(lastIdx);
        StepOrder next = new StepOrder(values);
        next.values.set(lastIdx, newOrder);
        return next;
    }

    StepOrder nextAsChild() {
        StepOrder next = new StepOrder(values);
        next.values.add(1);
        return next;
    }

    public int size() {
        return this.values.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StepOrder)) return false;
        StepOrder stepOrder = (StepOrder) o;
        return Objects.equals(values, stepOrder.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return "StepOrder{" +
                "values=" + values +
                '}';
    }
}
