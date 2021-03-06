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

import static org.junit.Assert.assertEquals;

public class StepOrderGeneratorTest {

    @Test
    public void getNextStepOrderForChild() {

        StepOrderGenerator sog = new StepOrderGenerator();

        StepOrder parent = StepOrder.from(1);

        StepOrder so1 = sog.genNextAfter(parent);
        StepOrder so2 = sog.genNextAfter(parent);

        assertEquals(StepOrder.from(1, 1), so1);
        assertEquals(StepOrder.from(1, 2), so2);

        StepOrder so3 = sog.genNextAfter(so2);
        assertEquals(StepOrder.from(1, 2, 1), so3);

        StepOrder so4 = sog.genNextAfter(parent);
        assertEquals(StepOrder.from(1, 3), so4);
    }
}
