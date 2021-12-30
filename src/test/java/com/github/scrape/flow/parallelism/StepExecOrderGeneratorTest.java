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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StepExecOrderGeneratorTest {

    @Test
    public void getNextStepOrderForChild() {

        StepExecOrderGenerator sog = new StepExecOrderGenerator();

        StepExecOrder parent = new StepExecOrder(1);

        StepExecOrder so1 = sog.genNextOrderAfter(parent);
        StepExecOrder so2 = sog.genNextOrderAfter(parent);

        assertEquals(new StepExecOrder(1, 1), so1);
        assertEquals(new StepExecOrder(1, 2), so2);

        StepExecOrder so3 = sog.genNextOrderAfter(so2);
        assertEquals(new StepExecOrder(1, 2, 1), so3);

        StepExecOrder so4 = sog.genNextOrderAfter(parent);
        assertEquals(new StepExecOrder(1, 3), so4);
    }
}
