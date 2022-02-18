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
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the stepOrder of steps whose child step sequence needs to finish so that the data models can be published to registered listeners
 * <p>
 * the data head in this
 */
@Log4j2
@RequiredArgsConstructor
public class StepAndDataRelationshipTracker {

    /**
     * The owning step order is the step that generated the data model object
     * Tha data model object is populated with parsed data in the child steps and until they have all finished the data cannot be published to listeners
     */
    private final Map<StepOrder, SpawnedStepsModelsList> spawnedByParentStep = new ConcurrentHashMap<>();

    private final ActiveStepsTracker activeStepsTracker;

    /**
     * Call this only when the spawnedModel instance was just instantiated (-> do not call this from places where the data model was readily propagated ...)
     *
     * @param parent             step that created the instance of the data model and propagated it downstream to spawned child steps whose job it is to populate it (the spawned steps)
     * @param spawnedSteps       steps created by the parent
     * @param modelToPublishList encapsulates the model object to which parsing steps assign acquired data
     */
    public synchronized void track(StepOrder parent, List<StepOrder> spawnedSteps, List<ModelToPublish> modelToPublishList) {
        SpawnedStepsModels s = new SpawnedStepsModels(parent, spawnedSteps, modelToPublishList);
        log.debug("Tracking {}", s);
        spawnedByParentStep.compute(parent, (parent0, sl) -> {
            if (sl == null) {
                sl = new SpawnedStepsModelsList();
            }
            sl.add(s);
            return sl;
        });
    }

    public synchronized void untrack(SpawnedStepsModels spawnedStepsModels) {
        SpawnedStepsModelsList spawnedStepsModelsList = spawnedByParentStep.get(spawnedStepsModels.getParent());
        if (spawnedStepsModelsList != null) {
            spawnedStepsModelsList.remove(spawnedStepsModels);
            if (spawnedStepsModelsList.isEmpty()) {
                spawnedByParentStep.remove(spawnedStepsModels.getParent());
            }
        }
    }

    /**
     * @param finishedStep step whose execution has just finished.
     *                     This method will search the step hierarchy upwards from this step (through parents ...) and will check if all related step executions
     *                     have been finished ... if yes than the data parsed by all those steps can be returned inside the list of FinalizedData
     */
    public synchronized List<FinalizedModels> getModelsWithNoActiveSteps(StepOrder finishedStep) {
        List<FinalizedModels> dtpList = new ArrayList<>();

        List<RelatedSteps> rsList = getAllRelatedStepsTo(finishedStep);

        for (RelatedSteps relatedSteps : rsList) {
            boolean anyRelatedStepSeqStillActive = relatedSteps.getSpawnedStepsModels().getSteps().stream().anyMatch(activeStepsTracker::isPartOfActiveStepSequence);
            if (anyRelatedStepSeqStillActive) {
                // cannot publish this data
                log.debug("Cannot publish related data for finished step yet: {}", finishedStep);
            } else {
                dtpList.add(new FinalizedModels(relatedSteps.getSpawnedStepsModels()));
            }
        }

        return dtpList;
    }


    /**
     * Useful after a step has finished executing
     *
     * @param step any child step that has finished and might have completed the whole step hierarchy for some data model ...
     */
    @SuppressWarnings("OptionalIsPresent")
    List<RelatedSteps> getAllRelatedStepsTo(StepOrder step) {
        List<RelatedSteps> relatedSteps = new ArrayList<>();
        StepOrder prevParent = step;
        Optional<StepOrder> parent = step.getParent();
        while (parent.isPresent()) {
            SpawnedStepsModelsList sl = spawnedByParentStep.get(parent.get());
            if (sl != null) {
                Optional<SpawnedStepsModels> s = sl.getSpawnedContaining(prevParent);
                if (s.isPresent()) {
                    relatedSteps.add(new RelatedSteps(s.get()));
                }
            }
            prevParent = parent.get();
            parent = parent.get().getParent();
        }
        log.debug("relatedSteps for {} are : {}", step, relatedSteps);
        return relatedSteps;
    }


    @Data
    static class RelatedSteps {
        private final SpawnedStepsModels spawnedStepsModels;
    }

}
