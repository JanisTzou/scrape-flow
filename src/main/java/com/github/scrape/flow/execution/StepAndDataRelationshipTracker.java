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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Tracks the stepOrder of steps whose child step sequence needs tp finish so that the data models can be published to registered listeners
 * <p>
 * the data head in this
 */
@Log4j2
public class StepAndDataRelationshipTracker {

    /**
     * The owning step order is the step that generated the data model object
     * Tha data model object is populated with parsed data in the child steps and until they have all finished the data cannot be published to listeners
     */
    private final Map<StepOrder, SpawnedList> spawnedByParentStep = new ConcurrentHashMap<>();

    private final ActiveStepsTracker activeStepsTracker;


    public StepAndDataRelationshipTracker(ActiveStepsTracker activeStepsTracker) {
        this.activeStepsTracker = activeStepsTracker;
    }

    /**
     * Call this only when the spawnedModel instance was just instantiated (-> do not call this from places where the data model was readily propagated ...)
     *
     * @param parent             step that created the instance of the data model and propagated it downstream to spawned child steps whose job it is to populate it (the spawned steps)
     * @param spawnedSteps       steps created by the parent
     * @param modelToPublishList encapsulates the model object to which parsing steps assign acquired data
     */
    public synchronized void track(StepOrder parent, List<StepOrder> spawnedSteps, List<ModelToPublish> modelToPublishList) {
        Spawned s = new Spawned(parent, spawnedSteps, modelToPublishList);
        log.debug("Tracking {}", s);
        spawnedByParentStep.compute(parent, (parent0, sl) -> {
            if (sl == null) {
                sl = new SpawnedList();
            }
            sl.list.add(s);
            return sl;
        });
    }

    public synchronized void untrack(Spawned spawned) {
        SpawnedList spawnedList = spawnedByParentStep.get(spawned.parent);
        if (spawnedList != null) {
            spawnedList.remove(spawned);
            if (spawnedList.isEmpty()) {
                spawnedByParentStep.remove(spawned.getParent());
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
            boolean anyRelatedStepSeqStillActive = relatedSteps.spawned.steps.stream().anyMatch(activeStepsTracker::isPartOfActiveStepSequence);
            if (anyRelatedStepSeqStillActive) {
                // cannot publish this data
                log.debug("Cannot publish related data for finished step yet: {}", finishedStep);
            } else {
                dtpList.add(new FinalizedModels(relatedSteps.parent, relatedSteps.spawned));
            }
        }

        return dtpList;
    }


    /**
     * Useful after a step has finished executing
     *
     * @param step any child step that has finished and might have completed the whole step hierarchy for some data model ...
     */
    List<RelatedSteps> getAllRelatedStepsTo(StepOrder step) {
        List<RelatedSteps> relatedSteps = new ArrayList<>();
        StepOrder prevParent = step;
        Optional<StepOrder> parent = step.getParent();
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
        log.debug("relatedSteps for {} are : {}", step, relatedSteps);
        return relatedSteps;
    }



    /**
     * Holds the data for which all parsing tasks have been finished and should contained final parsed information
     */
    @RequiredArgsConstructor
    @Getter
    @ToString
    public static class FinalizedModels {

        public static Comparator<FinalizedModels> NATURAL_COMPARATOR = (fm1, fm2) -> {
            StepOrder so1 = fm1.spawned.getSteps().stream().min(StepOrder.NATURAL_COMPARATOR).get(); // must be present
            StepOrder so2 = fm2.spawned.getSteps().stream().min(StepOrder.NATURAL_COMPARATOR).get();
            return StepOrder.NATURAL_COMPARATOR.compare(so1, so2);
        };

        private final StepOrder parent;
        private final Spawned spawned;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FinalizedModels)) return false;
            FinalizedModels that = (FinalizedModels) o;
            return Objects.equals(spawned.getSteps(), that.spawned.getSteps());
        }

        @Override
        public int hashCode() {
            return Objects.hash(spawned.getSteps());
        }
    }

    /**
     * Children spawned by the owning step to which a data model was published.
     * They are the steps directly below the owning step
     */
    @Data
    public static class Spawned {

        private final StepOrder parent;

        // listener associated with the given model so that it can be published
        private final Set<StepOrder> steps = Collections.newSetFromMap(new ConcurrentHashMap<>());

        /**
         * Data modified by the active steps which we wanna publish to registered listeners when all the steps finish
         */
        private final List<ModelToPublish> modelToPublishList;

        public Spawned(StepOrder parent, List<StepOrder> steps, List<ModelToPublish> modelToPublishList) {
            this.parent = parent;
            this.steps.addAll(steps);
            this.modelToPublishList = modelToPublishList;
        }

        public boolean containsSameSteps(Spawned other) {
            if (this.steps.size() != other.steps.size()) {
                return false;
            }
            List<StepOrder> steps1 = this.steps.stream().sorted(StepOrder.NATURAL_COMPARATOR).collect(Collectors.toList());
            List<StepOrder> steps2 = other.steps.stream().sorted(StepOrder.NATURAL_COMPARATOR).collect(Collectors.toList());

            return steps1.equals(steps2);
        }

    }

    @Getter
    static class SpawnedList {
        private final List<Spawned> list = new CopyOnWriteArrayList<>(); // must be thread safe

        Optional<Spawned> getSpawnedContaining(StepOrder order) {
            return list.stream().filter(s -> s.steps.contains(order)).findFirst();
        }

        void remove(Spawned spawned) {
            Optional<Spawned> found = list.stream().filter(s -> s.containsSameSteps(spawned)).findFirst();
            found.ifPresent(list::remove);
        }

        boolean isEmpty() {
            return list.isEmpty();
        }
    }

    @Data
    static class RelatedSteps {

        private final StepOrder parent;
        private final Spawned spawned;

    }


}
