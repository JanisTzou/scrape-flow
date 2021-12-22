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

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class StepExecOrderTest {


    @Test
    public void testSortingByNaturalComparator() {
        StepExecOrder so1 = new StepExecOrder(1, 1, 1);
        StepExecOrder so2 = new StepExecOrder(1, 2, 1);
        StepExecOrder so3 = new StepExecOrder(1, 2, 2);
        StepExecOrder so4 = new StepExecOrder(1, 2, 2, 4);

        List<StepExecOrder> stepExecOrders = listOf(so1, so2, so3, so4);

        stepExecOrders.sort(StepExecOrder.NATURAL_COMPARATOR.reversed());
        assertEquals(List.of(so4, so3, so2, so1), stepExecOrders);

        stepExecOrders.sort(StepExecOrder.NATURAL_COMPARATOR);
        assertEquals(List.of(so1, so2, so3, so4), stepExecOrders);
    }

    @Test
    public void testSortingByNaturalComparator2() {
        StepExecOrder so1 = new StepExecOrder(0, 1, 1, 1, 1, 2, 1, 1, 2, 1);
        StepExecOrder so2 = new StepExecOrder(0, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1);

        List<StepExecOrder> stepExecOrders = listOf(so1, so2);

        stepExecOrders.sort(StepExecOrder.NATURAL_COMPARATOR);
        assertEquals(List.of(so2, so1), stepExecOrders);

    }

    @Test
    public void nextAtNewLevel() {
        StepExecOrder so1 = new StepExecOrder(1, 1, 1);
        assertEquals(new StepExecOrder(1, 1, 1, 1), so1.nextAsChild());
    }

    @Test
    public void nextAtSameLevel() {
        StepExecOrder so1 = new StepExecOrder(1, 1, 1);
        assertEquals(new StepExecOrder(1, 1, 2), so1.nextAsSibling());
    }

    private List<StepExecOrder> listOf(StepExecOrder... sos) {
        return new ArrayList<>(Arrays.asList(sos));
    }

    @Test
    public void testPerformancesInSortedSets() {

        List<StepExecOrder> stepExecOrders = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    for (int l = 0; l < 10; l++) {
                        for (int m = 0; m < 10; m++) {
                            stepExecOrders.add(new StepExecOrder(i, j, k, l, m));
                        }
                    }
                }
            }
        }

        long runDuration1 = addToSortedSet(stepExecOrders); // warmup
        System.out.println("1 run took " + runDuration1 + " ms");

        long runDuration2 = addToSortedSet(stepExecOrders);
        System.out.println("2 run took " + runDuration2 + " ms");

    }

    private long addToSortedSet(List<StepExecOrder> stepExecOrders) {
        long start = System.currentTimeMillis();
        SortedSet<StepExecOrder> sortedSet = new TreeSet<>(StepExecOrder.NATURAL_COMPARATOR);
        sortedSet.addAll(stepExecOrders);
        long end = System.currentTimeMillis();
        return end - start;
    }

    @Test
    public void asString() {
        assertEquals("1-1-1", new StepExecOrder(1, 1, 1).asString());
    }

    @Test
    public void subOrder() {
        assertEquals(new StepExecOrder(1, 2, 2), new StepExecOrder(1, 2, 2, 4).getSubOrder(3).get());

        assertTrue(new StepExecOrder(1, 2, 2, 4).getSubOrder(5).isEmpty());
    }

    @Test
    public void isParentOf() {
        assertTrue(new StepExecOrder(1, 2, 2).isParentOf(new StepExecOrder(1, 2, 2, 4)));
        assertFalse(new StepExecOrder(1, 2, 2).isParentOf(new StepExecOrder(1, 2, 2)));
        assertFalse(new StepExecOrder(1, 2, 2, 4).isParentOf(new StepExecOrder(1, 2, 2)));
    }

    @Test
    public void isBefore() {
        assertTrue(new StepExecOrder(1, 2, 2).isBefore(new StepExecOrder(1, 2, 2, 4)));
        assertFalse(new StepExecOrder(1, 2, 2, 1).isBefore(new StepExecOrder(1, 2, 2)));
    }

    @Test
    public void isAfter() {
        assertTrue(new StepExecOrder(1, 2, 2, 1).isAfter(new StepExecOrder(1, 2, 2)));
        assertFalse(new StepExecOrder(1, 2, 2).isAfter(new StepExecOrder(1, 2, 2, 4)));
    }
}
