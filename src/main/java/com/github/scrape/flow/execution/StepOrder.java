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

import java.util.*;
import java.util.stream.Collectors;

public class StepOrder {

    public static final StepOrder ROOT = new StepOrder(List.of(0));

    public static final Comparator<StepOrder> NATURAL_COMPARATOR = (so1, so2) -> {
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


    public static StepOrder from(Integer ... values) {
        return new StepOrder(values);
    }

    private StepOrder(Integer ... values) {
        this(Arrays.asList(values));
    }

    private StepOrder(List<Integer> values) {
        checkInvariants(values.size());
        this.values.addAll(values); // important to create a copy!
    }

    private void checkInvariants(int length) {
        if (length == 0) {
            throw new IllegalArgumentException("StepOrder must contain at least one value!");
        }
    }

    public Optional<StepOrder> getPrecedingSibling() {
        if (hasParent()) {
            int lastIdx = values.size() - 1;
            int lastValue = values.get(lastIdx);
            if (lastValue > 1) {
                int newLastValue = lastValue - 1;
                StepOrder preceding = new StepOrder(values);
                preceding.values.set(lastIdx, newLastValue);
                return Optional.of(preceding);
            }
        }
        return Optional.empty();
    }

    public StepOrder getFollowingSibling() {
        int lastIdx = values.size() - 1;
        int newOrder = values.get(lastIdx) + 1;
        StepOrder next = new StepOrder(values);
        next.values.set(lastIdx, newOrder);
        return next;
    }

    public StepOrder getFirstChild() {
        StepOrder next = new StepOrder(values);
        next.values.add(1);
        return next;
    }

    boolean hasParent() {
        return size() > 1; // position 0 is root ...
    }

    public Optional<StepOrder> getParent() {
        if (hasParent()) {
            StepOrder parent = new StepOrder(values.subList(0, values.size() - 1));
            return Optional.of(parent);
        }
        return Optional.empty();
    }

    public boolean isParentOf(StepOrder other) {
        if (other.size() > this.size()) {
            Optional<StepOrder> subOrder = other.getSubOrder(size());
            if (subOrder.isPresent()) {
                return this.equals(subOrder.get());
            }
        }
        return false;
    }

    Optional<StepOrder> getSubOrder(int valuesToInclude) {
        if (valuesToInclude <= 0) {
            throw new IllegalStateException("Specified value must be greater than 0!");
        }
        if (size() < valuesToInclude) {
            return Optional.empty();
        } else if (size() == valuesToInclude) {
            return Optional.of(this);
        } else {
            return Optional.of(new StepOrder(this.values.subList(0, valuesToInclude)));
        }
    }

    public boolean isBefore(StepOrder other) {
        return NATURAL_COMPARATOR.compare(this, other) < 0;
    }

    public boolean isAfter(StepOrder other) {
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
        return "step-order-" + asString();
    }

}
