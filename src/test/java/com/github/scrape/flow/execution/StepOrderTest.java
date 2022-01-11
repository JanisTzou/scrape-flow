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
    public void testSortingByNaturalComparator2() {
        StepOrder so1 = new StepOrder(0, 1, 1, 1, 1, 2, 1, 1, 2, 1);
        StepOrder so2 = new StepOrder(0, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1);

        List<StepOrder> stepOrders = listOf(so1, so2);

        stepOrders.sort(StepOrder.NATURAL_COMPARATOR);
        assertEquals(List.of(so2, so1), stepOrders);

    }

    @Test
    public void testSortingByNaturalComparator3() {
        StepOrder so1 = new StepOrder(0, 1, 1, 2, 1, 1, 3);
        StepOrder so2 = new StepOrder(0, 1, 1, 2, 1, 1, 2);
        StepOrder so3 = new StepOrder(0, 1, 1);
        StepOrder so4 = new StepOrder(0, 1, 2, 2);

        Queue<StepOrder> publishingOrder = new PriorityQueue<>(100, StepOrder.NATURAL_COMPARATOR);

        publishingOrder.add(so4);
        publishingOrder.add(so3);
        publishingOrder.add(so2);
        publishingOrder.add(so1);

        assertEquals(so3, publishingOrder.poll());
        assertEquals(so2, publishingOrder.poll());
        assertEquals(so1, publishingOrder.poll());
        assertEquals(so4, publishingOrder.poll());
    }

    @Test
    public void nextAtNewLevel() {
        StepOrder so1 = new StepOrder(1, 1, 1);
        assertEquals(new StepOrder(1, 1, 1, 1), so1.nextAsChild());
    }

    @Test
    public void nextAtSameLevel() {
        StepOrder so1 = new StepOrder(1, 1, 1);
        assertEquals(new StepOrder(1, 1, 2), so1.nextAsSibling());
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

    @Test
    public void asString() {
        assertEquals("1-1-1-", new StepOrder(1, 1, 1).asString());
    }

    @Test
    public void subOrder() {
        assertEquals(new StepOrder(1, 2, 2), new StepOrder(1, 2, 2, 4).getSubOrder(3).get());

        assertFalse(new StepOrder(1, 2, 2, 4).getSubOrder(5).isPresent());
    }

    @Test
    public void isParentOf() {
        assertTrue(new StepOrder(1, 2, 2).isParentOf(new StepOrder(1, 2, 2, 4)));
        assertFalse(new StepOrder(1, 2, 2).isParentOf(new StepOrder(1, 2, 2)));
        assertFalse(new StepOrder(1, 2, 2, 4).isParentOf(new StepOrder(1, 2, 2)));
    }

    @Test
    public void isBefore() {
        assertTrue(new StepOrder(1, 2, 2).isBefore(new StepOrder(1, 2, 2, 4)));
        assertFalse(new StepOrder(1, 2, 2, 1).isBefore(new StepOrder(1, 2, 2)));
    }

    @Test
    public void isAfter() {
        assertTrue(new StepOrder(1, 2, 2, 1).isAfter(new StepOrder(1, 2, 2)));
        assertFalse(new StepOrder(1, 2, 2).isAfter(new StepOrder(1, 2, 2, 4)));
    }
}
