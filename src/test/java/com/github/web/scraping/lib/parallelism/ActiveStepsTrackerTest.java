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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ActiveStepsTrackerTest {

    StepExecOrder step_1 = StepExecOrder.from(1);
    StepExecOrder step_1_1 = StepExecOrder.from(1, 1);
    StepExecOrder step_1_2 = StepExecOrder.from(1, 2);
    StepExecOrder step_1_2_1 = StepExecOrder.from(1, 2, 1);

    ActiveStepsTracker ast;

    @Before
    public void setUp() throws Exception {
        ast = new ActiveStepsTracker();
    }

    @Test
    public void isActiveOrHasRelatedActiveSteps() {

        ast.track(step_1, "");
        assertTrue(ast.isActiveOrHasRelatedActiveSteps(step_1));

        assertFalse(ast.isActiveOrHasRelatedActiveSteps(step_1_1));
        ast.track(step_1_1, "");
        assertTrue(ast.isActiveOrHasRelatedActiveSteps(step_1_1));

        ast.track(step_1_2, "");
    }

    @Test
    public void untrack() {

        ast.track(step_1, "");
        ast.track(step_1_1, "");

        ast.untrack(step_1);
        assertTrue(ast.isActiveOrHasRelatedActiveSteps(step_1_1));
        assertTrue(ast.isActiveOrHasRelatedActiveSteps(step_1));

        ast.untrack(step_1_1);
        assertFalse(ast.isActiveOrHasRelatedActiveSteps(step_1_1));
        assertFalse(ast.isActiveOrHasRelatedActiveSteps(step_1));
    }


}
