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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tracks the stepOrder of steps whose child step sequence needs tp finish so that the data models can be published to registered listeners
 * <p>
 * the data head in this
 */
public class StepAndDataRelationshipTracker {

    // TODO this should keep the order by which we want to publish the data ... we cannot publish it in just any order ...
    // TODO support enforced data model 'flushing' if we know by that point that the data model has been populated but the following steps are too long to wait to finish ...

    // TODO untrack() method impl ...


    /**
     * The owning step order is the step that generated the data model object
     * Tha data model object is populated with parsed data in the child steps and until they have all finished the data cannot be published to listeners
     */
    private final Map<StepExecOrder, SpawnedList> spawnedByParentStep = new ConcurrentHashMap<>();

    private final ActiveStepsTracker activeStepsTracker;


    public StepAndDataRelationshipTracker(ActiveStepsTracker activeStepsTracker) {
        this.activeStepsTracker = activeStepsTracker;
    }

    /**
     * Call this only when the spawnedModel instance was just instantiated (-> do not call this from places where the data model was readily propagated ...)
     *
     * @param parent       step that created the instance of the data model and propagated it downstream to spawned child steps whose job it is to populate it (the spawned steps)
     * @param spawnedSteps steps created by the parent
     * @param spawnedModel object to which parsed data is bound as the parsing steps proceed doing their job
     */
    public void track(StepExecOrder parent, List<StepExecOrder> spawnedSteps, Object spawnedModel, ParsedDataListener<Object> parsedDataListener) {
        Spawned s = new Spawned(spawnedModel, parsedDataListener, spawnedSteps);
        spawnedByParentStep.compute(parent, (parent0, sl) -> {
            if (sl == null) {
                sl = new SpawnedList(new CopyOnWriteArrayList<>()); // TODO hmm ... think about this ...
            }
            sl.list.add(s);
            return sl;
        });
    }


    // TODO this method or something similar belongs to NotificationService probably ...
    /**
     * Useful after a step has finished executing
     *
     * @param step any child step that has finished and might have completed the whole step hierarchy for some data model ...
     */
    public boolean canPublishRelatedData(StepExecOrder step) {
        throw new NotImplementedException("implement!");

    }

    /**
     * Useful after a step has finished executing
     *
     * @param step any child step that has finished and might have completed the whole step hierarchy for some data model ...
     */
    List<RelatedSteps> getAllRelatedStepsTo(StepExecOrder step) {
        // TODO go through all parents of this step and see if all the spawned steps have been finished ... if yes than return true
        //  multiple sequences can be returned ... in case this steps finishing should cause pblication of multipe models

        List<RelatedSteps> relatedSteps = new ArrayList<>();
        StepExecOrder prevParent = step;
        Optional<StepExecOrder> parent = step.getParent();
        while (parent.isPresent()) {
            SpawnedList sl = spawnedByParentStep.get(parent.get());
            if (sl != null) {
                Optional<Spawned> s = sl.getSpawnedContaining(prevParent);
                if (s.isPresent()) {
                    relatedSteps.add(new RelatedSteps(parent.get(), s.get()));
                }
            }
            prevParent = parent.get();
            parent = parent.get().getParent();
        }

        return relatedSteps;
    }


    // TODO this method might be slow ... so maybe before calling it we can make some checks to invoke it less ...

    /**
     * @param finishedStep step whose execution has just finished.
     *                     This method will search the step hierarchy upwards from this step (through parents ...) and will check if all related step executions
     *                     have been finished ... if yes than the data parsed by all those steps can be returned inside the list of FinalizedData
     */
    public List<FinalizedModel> getModelsWithNoActiveSteps(StepExecOrder finishedStep) {
        // find out if the data can be published
        //  -> yes, if no related steps are still active
        //  -> yes, if the order is for them to go ...  this can be delegated to another service no ?

        List<FinalizedModel> dtpList = new ArrayList<>();

        List<RelatedSteps> rsList = getAllRelatedStepsTo(finishedStep);
        for (RelatedSteps relatedSteps : rsList) {
            boolean anyRelatedStepSeqStillActive = relatedSteps.spawned.steps.values().stream().anyMatch(activeStepsTracker::isActiveOrHasRelatedActiveSteps);
            if (anyRelatedStepSeqStillActive) {
                // cannot publish this data
            } else {
                FinalizedModel fm = new FinalizedModel(
                        relatedSteps.spawned.model,
                        relatedSteps.spawned.modelListener,
                        relatedSteps.spawned.steps.values().stream().toList()
                );
                dtpList.add(fm);
            }
        }

        return dtpList;
    }


    /**
     * Holds the data for which all parsing tasks have been finished and should contained final parsed information
     */
    @RequiredArgsConstructor
    @Getter
    public static class FinalizedModel {
        private final Object model;

        // listener associated with the given model so that it can be published
        private final ParsedDataListener<Object> modelListener;
        private final List<StepExecOrder> steps;
    }

    /**
     * Children spawned by the owning step to which a data model was published.
     * They are the steps directly below the owning step
     */
    @Getter
    static class Spawned {

        /**
         * Data modified by the activeSteps
         */
        private final Object model;

        // listener associated with the given model so that it can be published
        private final ParsedDataListener<Object> modelListener;
        private final Map<StepExecOrder, StepExecOrder> steps = new ConcurrentHashMap<>();

        public Spawned(Object model, ParsedDataListener<Object> modelListener, List<StepExecOrder> steps) {
            this.model = model;
            this.modelListener = modelListener;
            for (StepExecOrder step : steps) {
                this.steps.put(step, step);
            }

        }

    }

    @RequiredArgsConstructor
    @Getter
    static class SpawnedList {
        private final List<Spawned> list;

        private Optional<Spawned> getSpawnedContaining(StepExecOrder order) {
            return list.stream().filter(s -> s.steps.containsKey(order)).findFirst();
        }
    }

    @RequiredArgsConstructor
    @Getter
    static class RelatedSteps {

        private final StepExecOrder parent;
        private final Spawned spawned;

    }


}
