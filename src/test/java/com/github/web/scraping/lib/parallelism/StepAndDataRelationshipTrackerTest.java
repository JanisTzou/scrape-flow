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
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.*;

public class StepAndDataRelationshipTrackerTest {

    ParsedDataListener<Object> listener = Mockito.mock(ParsedDataListener.class);

    Object model1 = new Object();
    Object model2 = new Object();

    StepExecOrder step_1 = StepExecOrder.from(1);
    StepExecOrder step_1_1 = StepExecOrder.from(1, 1);
    StepExecOrder step_1_2 = StepExecOrder.from(1, 2);
    StepExecOrder step_1_2_1 = StepExecOrder.from(1, 2, 1);
    StepExecOrder step_1_2_2 = StepExecOrder.from(1, 2, 2);

    ActiveStepsTracker asTracker;
    StepAndDataRelationshipTracker sdrTracker;

    @Before
    public void setUp() throws Exception {
        asTracker = new ActiveStepsTracker();
        sdrTracker = new StepAndDataRelationshipTracker(asTracker);
    }

    @Test
    public void shouldReturnRelatedSteps() {

        sdrTracker.track(step_1, List.of(step_1_1, step_1_2), model1, listener);

        List<StepAndDataRelationshipTracker.RelatedSteps> relatedSteps = sdrTracker.getAllRelatedStepsTo(step_1_2_1);

        assertEquals(1, relatedSteps.size());
        StepAndDataRelationshipTracker.Spawned spawned = relatedSteps.get(0).getSpawned();
        assertEquals(model1, spawned.getModel());

        assertEquals(2, spawned.getSteps().size());
        assertTrue(spawned.getSteps().containsKey(step_1_1));
        assertTrue(spawned.getSteps().containsKey(step_1_2));
    }

    @Test
    public void shouldReturnDataWhenThereAreNoRelatedActiveSteps() {

        asTracker.track(step_1, "");
        asTracker.track(step_1_1, "");
        asTracker.track(step_1_2, "");
        sdrTracker.track(step_1, List.of(step_1_1, step_1_2), model1, listener);

        asTracker.track(step_1_2_1, "");
        asTracker.track(step_1_2_2, "");
        sdrTracker.track(step_1_2, List.of(step_1_2_1, step_1_2_2), model2, listener);

        List<StepAndDataRelationshipTracker.FinalizedModel> data;
        data = sdrTracker.getModelsWithNoActiveSteps(step_1_2_1);
        assertTrue(data.isEmpty());

        // simulate steps finished running ...
        asTracker.untrack(step_1_2_1);
        data = sdrTracker.getModelsWithNoActiveSteps(step_1_2_1);
        assertTrue(data.isEmpty());

        asTracker.untrack(step_1_2_2);
        data = sdrTracker.getModelsWithNoActiveSteps(step_1_2_2);
        assertFalse(data.isEmpty());
        assertEquals(List.of(step_1_2_1, step_1_2_2), data.get(0).getSteps());
        assertEquals(model2, data.get(0).getModel());

    }

    @Test
    public void shouldReturnMultipleDataWhenThereAreNoRelatedActiveStepsForTwoTrackedDataObjects() {

        asTracker.track(step_1, "");
        asTracker.track(step_1_1, "");
        asTracker.track(step_1_2, "");
        sdrTracker.track(step_1, List.of(step_1_1, step_1_2), model1, listener);

        asTracker.track(step_1_2_1, "");
        asTracker.track(step_1_2_2, "");
        sdrTracker.track(step_1_2, List.of(step_1_2_1, step_1_2_2), model2, listener);

        List<StepAndDataRelationshipTracker.FinalizedModel> data;

        data = sdrTracker.getModelsWithNoActiveSteps(step_1_1);
        assertTrue(data.isEmpty());
        data = sdrTracker.getModelsWithNoActiveSteps(step_1_2);
        assertTrue(data.isEmpty());
        data = sdrTracker.getModelsWithNoActiveSteps(step_1_2_1);
        assertTrue(data.isEmpty());
        data = sdrTracker.getModelsWithNoActiveSteps(step_1_2_2);
        assertTrue(data.isEmpty());


        // simulate steps finished running ...
        asTracker.untrack(step_1_1);
        asTracker.untrack(step_1_2);
        asTracker.untrack(step_1_2_1);
        asTracker.untrack(step_1_2_2);

        data = sdrTracker.getModelsWithNoActiveSteps(step_1_2_1);
        assertFalse(data.isEmpty());
        assertEquals(2, data.size());
        assertEquals(model2, data.get(0).getModel());
        assertEquals(model1, data.get(1).getModel());

    }



}
