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

import com.github.scrape.flow.scraping.ScrapingStep;
import org.junit.Test;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExclusiveExecutionHandlerTest {

    @Test
    public void shouldWaitForExclusiveSteps() {
        ActiveStepsTracker activeStepsTracker = new ActiveStepsTracker();
        ExclusiveExecutionHandler exclusiveExecutionHandler = new ExclusiveExecutionHandler(activeStepsTracker);

        ScrapingStep<?> sequence = Flow.asBlock() // 0-1
                .nextBranch(Flow.asBlock() // 0-1-1
                        .nextBranchExclusively(Flow.asBlock()) // 0-1-1-1,  0-1-1-3   EXCLUSIVE + 2 iterations
                        .nextBranch(Flow.asBlock()) //  0-1-1-2,  0-1-1-4
                )
                .nextBranchExclusively(Flow.asBlock()  // 0-1-2   EXCLUSIVE + 2 iterations
                        .nextBranch(Flow.asBlock()) // 0-1-2-1
                )
                .nextBranch(Flow.asBlock()); // 0-1-3

        StepHierarchyRepository stepHierarchyRepository = StepHierarchyRepository.createFrom(sequence);
        exclusiveExecutionHandler.setStepHierarchyRepository(stepHierarchyRepository);

        StepOrder step_0_1 = StepOrder.from(0, 1);
        StepOrder step_0_1_1 = StepOrder.from(0, 1, 1);
        StepOrder step_0_1_1_1 = StepOrder.from(0, 1, 1, 1);
        StepOrder step_0_1_1_2 = StepOrder.from(0, 1, 1, 2);
        StepOrder step_0_1_1_3 = StepOrder.from(0, 1, 1, 3);
        StepOrder step_0_1_1_4 = StepOrder.from(0, 1, 1, 4);
        StepOrder step_0_1_2 = StepOrder.from(0, 1, 2);
        StepOrder step_0_1_2_1 = StepOrder.from(0, 1, 2, 1);
        StepOrder step_0_1_3 = StepOrder.from(0, 1, 3);

        // 1
        // all steps active
        activeStepsTracker.track(step_0_1, step_0_1, null);
        activeStepsTracker.track(step_0_1_1, step_0_1_1, null);
        activeStepsTracker.track(step_0_1_2, step_0_1_2, null);
        activeStepsTracker.track(step_0_1_3, step_0_1_3, null);
        activeStepsTracker.track(step_0_1_1_1, step_0_1_1_1, null);
        activeStepsTracker.track(step_0_1_1_2, step_0_1_1_2, null);
        activeStepsTracker.track(step_0_1_1_3, step_0_1_1_1, null); // second iteration
        activeStepsTracker.track(step_0_1_1_4, step_0_1_1_2, null); // second iteration
        activeStepsTracker.track(step_0_1_2_1, step_0_1_2_1, null);

        assertFalse(exclusiveExecutionHandler.shouldWaitForExclusiveSteps(step_0_1_1, step_0_1_1));
        assertFalse(exclusiveExecutionHandler.shouldWaitForExclusiveSteps(step_0_1_2, step_0_1_2));
        assertTrue(exclusiveExecutionHandler.shouldWaitForExclusiveSteps(step_0_1_3, step_0_1_3));
        assertFalse(exclusiveExecutionHandler.shouldWaitForExclusiveSteps(step_0_1_1_1, step_0_1_1_1));
        assertTrue(exclusiveExecutionHandler.shouldWaitForExclusiveSteps(step_0_1_1_2, step_0_1_1_2));
        assertFalse(exclusiveExecutionHandler.shouldWaitForExclusiveSteps(step_0_1_1_3, step_0_1_1_1));
        assertTrue(exclusiveExecutionHandler.shouldWaitForExclusiveSteps(step_0_1_1_4, step_0_1_1_2));
        assertFalse(exclusiveExecutionHandler.shouldWaitForExclusiveSteps(step_0_1_2_1, step_0_1_2_1));

        // 2
        // preceding exclusive step finishes
        activeStepsTracker.untrack(step_0_1_1_1);
        assertFalse(exclusiveExecutionHandler.shouldWaitForExclusiveSteps(step_0_1_1_2, step_0_1_1_2));

        // 3
        // preceding exclusive step does not finish completely (child steps still active)
        activeStepsTracker.untrack(step_0_1_2);
        assertTrue(exclusiveExecutionHandler.shouldWaitForExclusiveSteps(step_0_1_3, step_0_1_3));

        // preceding exclusive step finishes completely
        activeStepsTracker.untrack(step_0_1_2_1);
        assertFalse(exclusiveExecutionHandler.shouldWaitForExclusiveSteps(step_0_1_3, step_0_1_3));

    }

}
