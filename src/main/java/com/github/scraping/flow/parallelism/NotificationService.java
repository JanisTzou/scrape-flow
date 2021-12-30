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

package com.github.scraping.flow.parallelism;

import com.github.scraping.flow.scraping.htmlunit.ModelToPublish;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.github.scraping.flow.parallelism.StepAndDataRelationshipTracker.FinalizedModels;

@Log4j2
public class NotificationService {

    private final StepAndDataRelationshipTracker stepAndDataRelationshipTracker;

    private final Queue<StepExecOrder> publishingOrderQueue = new PriorityQueue<>(100, StepExecOrder.NATURAL_COMPARATOR);
    private final Queue<FinalizedModels> waitingToSendQueue = new PriorityQueue<>(100, FinalizedModels.NATURAL_COMPARATOR);
    private final Set<StepExecOrder> waitingToSendSet = Collections.newSetFromMap(new ConcurrentHashMap<>());


    public NotificationService(StepAndDataRelationshipTracker stepAndDataRelationshipTracker) {
        this.stepAndDataRelationshipTracker = stepAndDataRelationshipTracker;
    }


    // TODO fix this better ... potential deadlocks ...

    /**
     * Call this only when the spawnedModel instance was just instantiated (-> do not call this from places where the data model was readily propagated ...)
     *
     * @param spawnedSteps steps that hold generated models to be populated by step execution with parsed data ...
     */
    public synchronized void track(List<StepExecOrder> spawnedSteps) {
        publishingOrderQueue.addAll(spawnedSteps);
    }


    // TODO refactor this ...

    public synchronized void notifyAfterStepFinished(StepExecOrder stepExecOrder) {

        log.debug("Received finished step notification: {}", stepExecOrder);

        // as we are waiting this repeated returns same stuff ... we need to make sure that we do not use it twice ... as it is already waiting ...
        List<FinalizedModels> finalizedData = stepAndDataRelationshipTracker.getModelsWithNoActiveSteps(stepExecOrder);

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
                    List<StepExecOrder> waitingSteps = nextWaiting.getSpawned().getSteps().stream().sorted(StepExecOrder.NATURAL_COMPARATOR).collect(Collectors.toList());

                    if (!publishingOrderQueue.isEmpty()) {

                        for (StepExecOrder waitingStep : waitingSteps) {
                            StepExecOrder publishingHead = publishingOrderQueue.peek();
                            log.debug("publishingHead: {}", publishingHead);
                            log.debug("waitingHead: {}", waitingSteps);

                            if (publishingHead != null && publishingHead.equals(waitingStep)) { // if there is no bug then this should apply for all the sorted steps ...
                                send = true;
                                publishingOrderQueue.poll();
                            } else {
                                if (send) {
                                    log.error("{} Unexpected state - not all steps from the FinalizedModels matched the head of the publishingOrder queue", stepExecOrder);
                                }
                            }
                        }

                        if (send) {
                            removeFromWaitingAndPublish(stepExecOrder, nextWaiting, waitingSteps);
                        } else {
                            log.debug("delaying sending finalized data: {}", waitingSteps);
                            break;
                        }

                    } else {
                        removeFromWaitingAndPublish(stepExecOrder, nextWaiting, waitingSteps);
                    }
                } else {
                    break;
                }

            }
        }
    }


    private void removeFromWaitingAndPublish(StepExecOrder stepExecOrder, FinalizedModels nextWaiting, List<StepExecOrder> waitingSteps) {
        waitingToSendQueue.poll();
        nextWaiting.getSpawned().getSteps().forEach(waitingToSendSet::remove);
        publish(stepExecOrder, nextWaiting);
    }


    private void publish(StepExecOrder stepExecOrder, FinalizedModels data) {
        for (ModelToPublish mtp : data.getSpawned().getModelToPublishList()) {
            log.debug("{} has finalized data of type '{}'", stepExecOrder, mtp.getModel().getClass().getSimpleName());
            if (mtp.getScrapedDataListener() != null) {
                log.debug("{} About to publish data to listener for type '{}' after step finished", stepExecOrder, mtp.getModelClass().getSimpleName());
                mtp.getScrapedDataListener().onParsedData(mtp.getModel());
            }
        }
    }


}
