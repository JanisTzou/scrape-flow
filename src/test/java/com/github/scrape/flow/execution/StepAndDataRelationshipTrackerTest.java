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

import com.github.scrape.flow.data.publishing.ModelToPublish;
import com.github.scrape.flow.data.publishing.ScrapedDataListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class StepAndDataRelationshipTrackerTest {

    ScrapedDataListener<Object> listener = Mockito.mock(ScrapedDataListener.class);

    String model1 = "model1";
    String model2 = "model2";

    StepOrder step_1 = StepOrder.from(1);
    StepOrder step_1_1 = StepOrder.from(1, 1);
    StepOrder step_1_2 = StepOrder.from(1, 2);
    StepOrder step_1_2_1 = StepOrder.from(1, 2, 1);
    StepOrder step_1_2_2 = StepOrder.from(1, 2, 2);

    ActiveStepsTracker asTracker;
    StepAndDataRelationshipTracker sdrTracker;

    @Before
    public void setUp() throws Exception {
        asTracker = new ActiveStepsTracker();
        sdrTracker = new StepAndDataRelationshipTracker(asTracker);
    }

    @Test
    public void shouldReturnRelatedSteps() {

        sdrTracker.track(step_1, List.of(step_1_1, step_1_2), List.of(new ModelToPublish(model1, model1.getClass(), listener)));

        List<StepAndDataRelationshipTracker.RelatedSteps> relatedSteps = sdrTracker.getAllRelatedStepsTo(step_1_2_1);

        assertEquals(1, relatedSteps.size());
        SpawnedStepsModels spawnedStepsModels = relatedSteps.get(0).getSpawnedStepsModels();
        assertEquals(model1, spawnedStepsModels.getModelToPublishList().get(0).getModel());

        assertEquals(2, spawnedStepsModels.getSteps().size());
        assertTrue(spawnedStepsModels.getSteps().contains(step_1_1));
        assertTrue(spawnedStepsModels.getSteps().contains(step_1_2));
    }

    @Test
    public void shouldReturnDataWhenThereAreNoRelatedActiveSteps() {

        asTracker.track(step_1, null, "");
        asTracker.track(step_1_1, null, "");
        asTracker.track(step_1_2, null, "");
        sdrTracker.track(step_1, List.of(step_1_1, step_1_2), List.of(new ModelToPublish(model1, model1.getClass(), listener)));

        asTracker.track(step_1_2_1, null, "");
        asTracker.track(step_1_2_2, null, "");
        sdrTracker.track(step_1_2, List.of(step_1_2_1, step_1_2_2), List.of(new ModelToPublish(model2, model2.getClass(), listener)));

        List<FinalizedModels> data;
        data = sdrTracker.getModelsWithNoActiveSteps(step_1_2_1);
        assertTrue(data.isEmpty());

        // simulate steps finished running ...
        asTracker.untrack(step_1_2_1);
        data = sdrTracker.getModelsWithNoActiveSteps(step_1_2_1);
        assertTrue(data.isEmpty());

        asTracker.untrack(step_1_2_2);
        data = sdrTracker.getModelsWithNoActiveSteps(step_1_2_2);
        assertFalse(data.isEmpty());
        assertEquals(List.of(step_1_2_1, step_1_2_2), new ArrayList<>(data.get(0).getModels().getSteps()));
        assertEquals(model2, data.get(0).getModels().getModelToPublishList().get(0).getModel());

    }

    @Test
    public void shouldReturnMultipleDataWhenThereAreNoRelatedActiveStepsForTwoTrackedDataObjects() {

        asTracker.track(step_1, null, "");
        asTracker.track(step_1_1, null, "");
        asTracker.track(step_1_2, null, "");
        sdrTracker.track(step_1, List.of(step_1_1, step_1_2), List.of(new ModelToPublish(model1, model1.getClass(), listener)));

        asTracker.track(step_1_2_1, null, "");
        asTracker.track(step_1_2_2, null, "");
        sdrTracker.track(step_1_2, List.of(step_1_2_1, step_1_2_2), List.of(new ModelToPublish(model2, model2.getClass(), listener)));

        List<FinalizedModels> data;

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
        assertEquals(model2, data.get(0).getModels().getModelToPublishList().get(0).getModel());
        assertEquals(model1, data.get(1).getModels().getModelToPublishList().get(0).getModel());

    }


}
