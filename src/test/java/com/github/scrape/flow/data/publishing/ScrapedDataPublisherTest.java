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

package com.github.scrape.flow.data.publishing;

import com.github.scrape.flow.execution.FinalizedModels;
import com.github.scrape.flow.execution.SpawnedStepsModels;
import com.github.scrape.flow.execution.StepAndDataRelationshipTracker;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.SpawnedSteps;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ScrapedDataPublisherTest {

    private final StepOrder step_1 = StepOrder.from(1);
    private final StepOrder step_1_1 = StepOrder.from(1, 1);
    private final StepOrder step_1_2 = StepOrder.from(1, 2);
    private final List<StepOrder> spawned = List.of(this.step_1_1, step_1_2);

    private final ScrapedDataListener mockListener = Mockito.mock(ScrapedDataListener.class);
    private final ModelToPublish modelToPublish = new ModelToPublish("data", String.class, mockListener);
    private final List<ModelToPublish> data = List.of(modelToPublish);
    private final SpawnedStepsModels models = new SpawnedStepsModels(step_1, spawned, data);
    private final FinalizedModels finalizedModels = new FinalizedModels(models);

    @Test
    public void dataShouldGetPublishedIfThereAreNoRelatedActiveSteps() {

        StepAndDataRelationshipTracker tracker = Mockito.mock(StepAndDataRelationshipTracker.class);
        Mockito.when(tracker.getModelsWithNoActiveSteps(this.step_1_1)).thenReturn(Collections.emptyList());
        Mockito.when(tracker.getModelsWithNoActiveSteps(this.step_1_2)).thenReturn(List.of(finalizedModels));
        ScrapedDataPublisher publisher = new ScrapedDataPublisher(tracker);
        publisher.enqueueStepsToAwaitDataPublishing(new SpawnedSteps(step_1, spawned));

        publisher.publishDataAfterStepFinished(this.step_1_1);
        Mockito.verifyNoInteractions(mockListener);
        publisher.publishDataAfterStepFinished(this.step_1_2);
        Mockito.verify(mockListener, Mockito.times(1)).onScrapedData(Mockito.eq(modelToPublish.getModel()));

    }

    @Test
    public void dataShouldGetPublishedIfThereAreStillActiveSteps() {

        StepAndDataRelationshipTracker tracker = Mockito.mock(StepAndDataRelationshipTracker.class);
        Mockito.when(tracker.getModelsWithNoActiveSteps(this.step_1_1)).thenReturn(Collections.emptyList());
        ScrapedDataPublisher publisher = new ScrapedDataPublisher(tracker);
        publisher.enqueueStepsToAwaitDataPublishing(new SpawnedSteps(step_1, spawned));

        publisher.publishDataAfterStepFinished(this.step_1_1);

        Mockito.verify(mockListener, Mockito.times(0));

    }

}
