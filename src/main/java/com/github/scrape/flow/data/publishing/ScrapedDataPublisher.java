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
import com.github.scrape.flow.execution.StepAndDataRelationshipTracker;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.SpawnedSteps;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Log4j2
public class ScrapedDataPublisher {

    private final StepAndDataRelationshipTracker stepAndDataRelationshipTracker;

    // determines the order in which data can be published for all steps
    private final Queue<StepOrder> publishingOrderQueue = new PriorityQueue<>(100, StepOrder.NATURAL_COMPARATOR);

    // contains data that is waiting to be published if some earlier step is yet to finished and get published first
    private final Queue<FinalizedModels> waitingToPublishQueue = new PriorityQueue<>(100, FinalizedModels.NATURAL_COMPARATOR);
    private final Set<StepOrder> waitingToPublishSet = Collections.newSetFromMap(new ConcurrentHashMap<>());


    public ScrapedDataPublisher(StepAndDataRelationshipTracker stepAndDataRelationshipTracker) {
        this.stepAndDataRelationshipTracker = stepAndDataRelationshipTracker;
    }

    /**
     * Call this only when the spawnedModel instance was just instantiated (-> do not call this from places where the data model was readily propagated ...)
     *
     * @param steps hold generated models to be populated by subsequent step execution with parsed data ...
     */
    public synchronized void enqueueStepsToAwaitDataPublishing(SpawnedSteps steps) {
        publishingOrderQueue.addAll(steps.getSteps());
    }

    public synchronized void publishDataAfterStepFinished(StepOrder finishedStep) {

        log.debug("Received finished step notification: {}", finishedStep);

        List<FinalizedModels> newFinalized = resolveNewFinalized(finishedStep);

        addToWaitingToPublishSet(newFinalized);
        untrackStepAndDataRelationship(newFinalized);

        handlePublishing(finishedStep, newFinalized);
    }

    private List<FinalizedModels> resolveNewFinalized(StepOrder stepOrder) {
        // as we are waiting this repeatedly returns same stuff ... we need to make sure that we do not use it twice ... as it is already waiting ...
        List<FinalizedModels> finalizedData = stepAndDataRelationshipTracker.getModelsWithNoActiveSteps(stepOrder);

        return finalizedData.stream()
                .filter(fm -> fm.getModels().getSteps().stream().noneMatch(waitingToPublishSet::contains))
                .collect(Collectors.toList());
    }

    private void addToWaitingToPublishSet(List<FinalizedModels> newFinalized) {
        for (FinalizedModels fin : newFinalized) {
            waitingToPublishSet.addAll(fin.getModels().getSteps());
        }
    }

    private void untrackStepAndDataRelationship(List<FinalizedModels> newFinalized) {
        // no longer needed there ... we take care of tracking this ...
        newFinalized.forEach(fin -> stepAndDataRelationshipTracker.untrack(fin.getModels()));
    }

    private void handlePublishing(StepOrder stepOrder, List<FinalizedModels> newFinalized) {
        if (!newFinalized.isEmpty()) {
            waitingToPublishQueue.addAll(newFinalized);

            while (true) {

                FinalizedModels nextWaiting = waitingToPublishQueue.peek();

                if (nextWaiting != null) {
                    List<StepOrder> waitingSteps = nextWaiting.getModels().getSteps().stream().sorted(StepOrder.NATURAL_COMPARATOR).collect(Collectors.toList());

                    if (!publishingOrderQueue.isEmpty()) {
                        boolean publish = pollPublishingQueueIfPublishingPossible(stepOrder, waitingSteps);
                        if (publish) {
                            removeFromWaitingAndPublish(stepOrder, nextWaiting);
                        } else {
                            log.debug("delaying sending finalized data: {}", waitingSteps);
                            break;
                        }
                    } else {
                        removeFromWaitingAndPublish(stepOrder, nextWaiting);
                    }
                } else {
                    break;
                }

            }
        }
    }

    private boolean pollPublishingQueueIfPublishingPossible(StepOrder stepOrder, List<StepOrder> waitingSteps) {
        boolean publish = false;
        for (StepOrder waitingStep : waitingSteps) {
            StepOrder publishingHead = publishingOrderQueue.peek();
            log.debug("publishingHead: {}", publishingHead);
            log.debug("waitingHead: {}", waitingSteps);

            if (publishingHead != null && publishingHead.equals(waitingStep)) { // if there is no bug then this should natively for all the sorted steps ...
                publish = true;
                publishingOrderQueue.poll();
            } else {
                if (publish) {
                    log.error("{} Unexpected state - not all steps from the FinalizedModels matched the head of the publishingOrder queue", stepOrder);
                }
            }
        }
        return publish;
    }

    private void removeFromWaitingAndPublish(StepOrder stepOrder, FinalizedModels nextWaiting) {
        waitingToPublishQueue.poll();
        nextWaiting.getModels().getSteps().forEach(waitingToPublishSet::remove);
        publish(stepOrder, nextWaiting);
    }


    private void publish(StepOrder stepOrder, FinalizedModels data) {
        for (ModelToPublish mtp : data.getModels().getModelToPublishList()) {
            log.debug("{} has finalized data of type '{}'", stepOrder, mtp.getModel().getClass().getSimpleName());
            if (mtp.getScrapedDataListener() != null) {
                log.debug("{} About to publish data to listener for type '{}' after step finished", stepOrder, mtp.getModelClass().getSimpleName());
                mtp.getScrapedDataListener().onScrapedData(mtp.getModel());
            }
        }
    }


}
