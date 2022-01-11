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

import com.github.scrape.flow.execution.StepAndDataRelationshipTracker;
import com.github.scrape.flow.execution.StepOrder;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.github.scrape.flow.execution.StepAndDataRelationshipTracker.FinalizedModels;

@Log4j2
public class ScrapedDataPublisher {

    private final StepAndDataRelationshipTracker stepAndDataRelationshipTracker;

    // order in which
    private final Queue<StepOrder> publishingOrderQueue = new PriorityQueue<>(100, StepOrder.NATURAL_COMPARATOR);
    private final Queue<FinalizedModels> waitingToSendQueue = new PriorityQueue<>(100, FinalizedModels.NATURAL_COMPARATOR);
    private final Set<StepOrder> waitingToSendSet = Collections.newSetFromMap(new ConcurrentHashMap<>());


    public ScrapedDataPublisher(StepAndDataRelationshipTracker stepAndDataRelationshipTracker) {
        this.stepAndDataRelationshipTracker = stepAndDataRelationshipTracker;
    }

    /**
     * Call this only when the spawnedModel instance was just instantiated (-> do not call this from places where the data model was readily propagated ...)
     *
     * @param steps hold generated models to be populated by subsequent step execution with parsed data ...
     */
    public synchronized void enqueueStepsToAwaitDataPublishing(List<StepOrder> steps) {
        publishingOrderQueue.addAll(steps);
    }


    // TODO refactor this ...
    // TODO bind this logic to a single thread and make only the publishing parallel ? ... so we can avoid synchronisation
    public synchronized void notifyAfterStepFinished(StepOrder stepOrder) {

        log.debug("Received finished step notification: {}", stepOrder);

        // as we are waiting this repeated returns same stuff ... we need to make sure that we do not use it twice ... as it is already waiting ...
        List<FinalizedModels> finalizedData = stepAndDataRelationshipTracker.getModelsWithNoActiveSteps(stepOrder);

        List<FinalizedModels> unknownFinalizedData = finalizedData.stream()
                .filter(fm -> fm.getSpawned().getSteps().stream().noneMatch(waitingToSendSet::contains))
                .collect(Collectors.toList());

        for (FinalizedModels finalized : unknownFinalizedData) {
            waitingToSendSet.addAll(finalized.getSpawned().getSteps());
        }

        // no longer needed there ... we take care of tracking this ...
        unknownFinalizedData.forEach(fm -> stepAndDataRelationshipTracker.untrack(fm.getSpawned()));

        if (!unknownFinalizedData.isEmpty()) {
            waitingToSendQueue.addAll(unknownFinalizedData);

            while (true) {

                boolean send = false;
                FinalizedModels nextWaiting = waitingToSendQueue.peek();

                if (nextWaiting != null) {
                    List<StepOrder> waitingSteps = nextWaiting.getSpawned().getSteps().stream().sorted(StepOrder.NATURAL_COMPARATOR).collect(Collectors.toList());

                    if (!publishingOrderQueue.isEmpty()) {

                        for (StepOrder waitingStep : waitingSteps) {
                            StepOrder publishingHead = publishingOrderQueue.peek();
                            log.debug("publishingHead: {}", publishingHead);
                            log.debug("waitingHead: {}", waitingSteps);

                            if (publishingHead != null && publishingHead.equals(waitingStep)) { // if there is no bug then this should apply for all the sorted steps ...
                                send = true;
                                publishingOrderQueue.poll();
                            } else {
                                if (send) {
                                    log.error("{} Unexpected state - not all steps from the FinalizedModels matched the head of the publishingOrder queue", stepOrder);
                                }
                            }
                        }

                        if (send) {
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


    // TODO publish on Schedulers.parallel() ?
    private void removeFromWaitingAndPublish(StepOrder stepOrder, FinalizedModels nextWaiting) {
        waitingToSendQueue.poll();
        nextWaiting.getSpawned().getSteps().forEach(waitingToSendSet::remove);
        publish(stepOrder, nextWaiting);
    }


    private void publish(StepOrder stepOrder, FinalizedModels data) {
        for (ModelToPublish mtp : data.getSpawned().getModelToPublishList()) {
            log.debug("{} has finalized data of type '{}'", stepOrder, mtp.getModel().getClass().getSimpleName());
            if (mtp.getScrapedDataListener() != null) {
                log.debug("{} About to publish data to listener for type '{}' after step finished", stepOrder, mtp.getModelClass().getSimpleName());
                mtp.getScrapedDataListener().onScrapedData(mtp.getModel());
            }
        }
    }


}
