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

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class StepOrderTest {


    @Test
    public void testSortingByNaturalComparator() {
        StepOrder so1 = new StepOrder(1, 1, 1);
        StepOrder so2 = new StepOrder(1, 2, 1);
        StepOrder so3 = new StepOrder(1, 2, 2);
        StepOrder so4 = new StepOrder(1, 2, 2, 4);

        List<StepOrder> stepOrders = listOf(so1, so2, so3, so4);

        stepOrders.sort(StepOrder.NATURAL_COMPARATOR.reversed());
        assertEquals(List.of(so4, so3, so2, so1), stepOrders);

        stepOrders.sort(StepOrder.NATURAL_COMPARATOR);
        assertEquals(List.of(so1, so2, so3, so4), stepOrders);
    }

    @Test
    public void nextAtNewLevel() {
        StepOrder so1 = new StepOrder(1, 1, 1);
        assertEquals(new StepOrder(1, 1, 1, 1), so1.nextAtNewLevel());
    }

    @Test
    public void nextAtSameLevel() {
        StepOrder so1 = new StepOrder(1, 1, 1);
        assertEquals(new StepOrder(1, 1, 2), so1.nextAtSameLevel());
    }

    private List<StepOrder> listOf(StepOrder... sos) {
        return new ArrayList<>(Arrays.asList(sos));
    }

    @Test
    public void testPerformancesInSortedSets() {

        List<StepOrder> stepOrders = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    for (int l = 0; l < 10; l++) {
                        for (int m = 0; m < 10; m++) {
                            stepOrders.add(new StepOrder(i, j, k, l, m));
                        }
                    }
                }
            }
        }

        long runDuration1 = addToSortedSet(stepOrders); // warmup
        System.out.println("1 run took " + runDuration1 + " ms");

        long runDuration2 = addToSortedSet(stepOrders);
        System.out.println("2 run took " + runDuration2 + " ms");

    }

    private long addToSortedSet(List<StepOrder> stepOrders) {
        long start = System.currentTimeMillis();
        SortedSet<StepOrder> sortedSet = new TreeSet<>(StepOrder.NATURAL_COMPARATOR);
        sortedSet.addAll(stepOrders);
        long end = System.currentTimeMillis();
        return end - start;
    }


}
