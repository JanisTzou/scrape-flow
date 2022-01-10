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

package com.github.scrape.flow.parallelism;

import java.util.*;
import java.util.stream.Collectors;

public class StepExecOrder {

    public static final StepExecOrder INITIAL = new StepExecOrder(List.of(0));

    public static final Comparator<StepExecOrder> NATURAL_COMPARATOR = (so1, so2) -> {
        for (int idx = 0; idx < Math.min(so1.size(), so2.size()); idx++) {
            Integer sVal = so1.values.get(idx);
            Integer bVal = so2.values.get(idx);
            int compared = Integer.compare(sVal, bVal);
            if (compared != 0) {
                return compared;
            }
        }

        // if we got here the StepOrder instances are either same length or they are equal up to the length of the smaller one
        if (so1.size() == so2.size()) {
          return 0;
        } else if (so1.size() < so2.size()) {
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


    static StepExecOrder from(Integer ... values) {
        return new StepExecOrder(values);
    }

    StepExecOrder(Integer ... values) {
        this(Arrays.asList(values));
    }

    private StepExecOrder(List<Integer> values) {
        checkInvariants(values.size());
        this.values.addAll(values); // important to create a copy!
    }

    private void checkInvariants(int length) {
        if (length == 0) {
            throw new IllegalArgumentException("StepOrder must contain at least one value!");
        }
    }

    StepExecOrder nextAsSibling() {
        int lastIdx = values.size() - 1;
        int newOrder = 1 + values.get(lastIdx);
        StepExecOrder next = new StepExecOrder(values);
        next.values.set(lastIdx, newOrder);
        return next;
    }

    StepExecOrder nextAsChild() {
        StepExecOrder next = new StepExecOrder(values);
        next.values.add(1);
        return next;
    }

    boolean hasParent() {
        return size() > 1; // position 0 is root ...
    }

    Optional<StepExecOrder> getParent() {
        if (hasParent()) {
            StepExecOrder parent = new StepExecOrder(values.subList(0, values.size() - 1));
            return Optional.of(parent);
        }
        return Optional.empty();
    }

    public boolean isParentOf(StepExecOrder other) {
        if (other.size() > this.size()) {
            Optional<StepExecOrder> subOrder = other.getSubOrder(size());
            if (subOrder.isPresent()) {
                return this.equals(subOrder.get());
            }
        }
        return false;
    }

    Optional<StepExecOrder> getSubOrder(int valuesToInclude) {
        if (valuesToInclude <= 0) {
            throw new IllegalStateException("Specified value must be greater than 0!");
        }
        if (size() < valuesToInclude) {
            return Optional.empty();
        } else if (size() == valuesToInclude) {
            return Optional.of(this);
        } else {
            return Optional.of(new StepExecOrder(this.values.subList(0, valuesToInclude)));
        }
    }

    public boolean isBefore(StepExecOrder other) {
        return NATURAL_COMPARATOR.compare(this, other) < 0;
    }

    public boolean isAfter(StepExecOrder other) {
        return NATURAL_COMPARATOR.compare(this, other) > 0;
    }

    public int size() {
        return this.values.size();
    }

    // error-prone having this as a field ... review methods modifying the list before doinf so ...
    public String asString() {
        return this.values.stream().map(Object::toString).collect(Collectors.joining("-", "", "-")); // IMPORTANT - the suffix is needed for prefix matching to work when tracking active steps/tasks...
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StepExecOrder)) return false;
        StepExecOrder stepExecOrder = (StepExecOrder) o;
        return Objects.equals(values, stepExecOrder.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return "step-order-" + asString();
    }

}
