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

package ret.webscrapers.scraping.data;

import aaanew.throttling.model.ScrapedDataType;
import akka.actor.ActorRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.enums.WebsiteEnum;
import ret.webscrapers.actors.ActorClass;
import ret.webscrapers.actors.ActorUtils;
import ret.webscrapers.scraping.data.images.ImageScraperActor;
import ret.webscrapers.scraping.data.inzeraty.query.InzeratQueryActor;

import java.util.*;

public class ActorPoolManager<T> {

    private static final Logger log = LogManager.getLogger(ActorPoolManager.class);

    private final ScrapedDataType dataType;
    /**
     * Always just one type of the listed ActorClasses
     */
    @ActorClass({ImageScraperActor.class, InzeratQueryActor.class})
    private final Map<WebsiteEnum, Queue<ActorRef>> websiteToActorsQueueMap;
    private volatile int remainingAnswers;
    private final Queue<T> itemsQueue = new LinkedList<>();
    private final ScraperActivityHandler scraperActivityHandler;


    public ActorPoolManager(ScrapedDataType dataType,
                            Map<WebsiteEnum, Queue<ActorRef>> websiteToActorsQueueMap,
                            Map<WebsiteEnum, Integer> websiteToMinActorsKeepAliveMap) {

        this.dataType = dataType;
        this.websiteToActorsQueueMap = websiteToActorsQueueMap;
        this.scraperActivityHandler = new ScraperActivityHandler(websiteToActorsQueueMap, websiteToMinActorsKeepAliveMap);
    }


    public void enqueueItemsToProcess(List<T> items) {
        itemsQueue.addAll(items);
    }

    public void enqueueItemsToProcess(T item) {
        enqueueItemsToProcess(Collections.singletonList(item));
    }

    public void setActorsToKeepActive(int actorsToKeepActive, WebsiteEnum website) {
        scraperActivityHandler.setActorsToKeepActive(actorsToKeepActive, website);
    }

    public void enqueueActor(WebsiteEnum website, ActorRef actor) {
        if (scraperActivityHandler.shouldDeactivateActor(website)) {
            log.info("DataType: {}, Enqueuing actor for deactivation: {}", dataType, ActorUtils.getName(actor));
            scraperActivityHandler.deactivateActor(website, actor);
        } else {
            log.info("DataType: {}, Enqueuing actor to active actor queue: {}", dataType, ActorUtils.getName(actor));
            websiteToActorsQueueMap.get(website).add(actor);
        }
        log.info("DataType: {}, Active actor queue size = {}", dataType, websiteToActorsQueueMap.get(website).size());
    }

    public boolean hasItemsToProcess() {
        return !itemsQueue.isEmpty();
    }

    public int unprocessedItemsCount() {
        return itemsQueue.size();
    }

    public Optional<T> dequeueItemToProcess() {
        if (!hasItemsToProcess()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(itemsQueue.poll());
        }
    }

    public boolean hasAvailableActors(WebsiteEnum website) {
        return websiteToActorsQueueMap.containsKey(website) && ! websiteToActorsQueueMap.get(website).isEmpty();
    }


    public Optional<ActorRef> dequeueActor(WebsiteEnum website) {
        if (!hasAvailableActors(website)) {
            log.info("No available actors to dequeue ...");
            return Optional.empty();
        } else {
            ActorRef actor = websiteToActorsQueueMap.get(website).poll();
            return Optional.ofNullable(actor);
        }
    }

    public int incrementRemainingAnswers() {
        return remainingAnswers += 1;
    }

    public int decrementRemainingAnswers() {
        return remainingAnswers -= 1;
    }

    public int getRemainingAnswers() {
        return remainingAnswers;
    }

    public boolean anyRemainingAnswers() {
        return remainingAnswers > 0;
    }



    /**
     * Helper class encapsulating data about active/deactivated actors.
     */
    public class ScraperActivityHandler {

        private final Map<WebsiteEnum, Integer> minActorsKeepActiveMap;
        private final Map<WebsiteEnum, Integer> websiteToTotalActorsMap = new HashMap<>();
        private final Map<WebsiteEnum, Queue<ActorRef>> inactiveActorsQueueMap = new HashMap<>();
        private final Map<WebsiteEnum, Integer> actorsToKeepActiveMap = new HashMap<>();


        public ScraperActivityHandler(Map<WebsiteEnum, Queue<ActorRef>> websiteToActorsQueueMap,
                                      Map<WebsiteEnum, Integer> minActorsKeepActiveMap) {

            this.minActorsKeepActiveMap = minActorsKeepActiveMap;
            for (Map.Entry<WebsiteEnum, Queue<ActorRef>> entry : websiteToActorsQueueMap.entrySet()) {
                websiteToTotalActorsMap.put(entry.getKey(), entry.getValue().size());
                inactiveActorsQueueMap.put(entry.getKey(), new LinkedList<>());
                actorsToKeepActiveMap.put(entry.getKey(), entry.getValue().size());
            }
        }


        public void setActorsToKeepActive(int actorsToKeepActive, WebsiteEnum website) {
            Integer minActiveActors = minActorsKeepActiveMap.get(website);
            if (actorsToKeepActive <= minActiveActors) {
                actorsToKeepActive = minActiveActors;
            }
            actorsToKeepActiveMap.put(website, actorsToKeepActive);
            while (shouldActivateActor(website)) {
                activateActor(website);
            }
            printCurrentStatus(website);
        }

        private void deactivateActor(WebsiteEnum website, ActorRef actor) {
            log.info("DataType: {}, Deactivating actor: {}", dataType, ActorUtils.getName(actor));
            inactiveActorsQueueMap.get(website).add(actor);
            printCurrentStatus(website);
        }

        private void activateActor(WebsiteEnum website) {
            ActorRef actor = inactiveActorsQueueMap.get(website).poll();
            log.info("DataType: {}, Activated actor: {}", dataType, ActorUtils.getName(actor));
            if (actor != null) {
                websiteToActorsQueueMap.get(website).add(actor);
            } else {
                log.error("Some error happened. Did not have any inactive actors to dequeue ... ");
            }
        }

        private boolean shouldDeactivateActor(WebsiteEnum website) {
            return countOfActorsToDeactivate(website) > 0;
        }

        private boolean shouldActivateActor(WebsiteEnum website) {
            return countOfActorsToDeactivate(website) < 0;
        }

        private int countOfActorsToDeactivate(WebsiteEnum website) {
            // this is called multiple times before the required number of actors actually gets reactivated as we are waitig for them to return from their scraping tasks ...
            int totalActors = websiteToTotalActorsMap.get(website);
            int newInactiveCountToBe = totalActors - actorsToKeepActiveMap.get(website);
            int currInactive = inactiveActorsQueueMap.get(website).size();
            int toDeactivate = newInactiveCountToBe - currInactive;
            return toDeactivate;
        }

        private void printCurrentStatus(WebsiteEnum website) {
            int totalActors = websiteToTotalActorsMap.get(website);
            int currInactive = inactiveActorsQueueMap.get(website).size();
            int toDeactivate = countOfActorsToDeactivate(website);
            log.info("SCRAPERS STATUS: DataType = "+ dataType + ", totalActors = " + totalActors + ", currInactive = " + currInactive + ", toDeactivate = " + toDeactivate);
        }

    }


}
