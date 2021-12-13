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

package ret.webscrapers.scraping.data.inzeraty;

import akka.actor.ActorRef;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.Inzerat;
import ret.appcore.model.WebSegment;
import ret.appcore.model.enums.WebsiteEnum;
import ret.appcore.model.scraping.PartialWebSegmentStats;
import ret.webscrapers.actors.ActorClass;
import ret.webscrapers.actors.FollowedBy;
import ret.webscrapers.actors.PrecededBy;
import ret.webscrapers.messages.*;
import ret.webscrapers.pipe.SimplePipeActorBase;
import ret.webscrapers.pipe.SimpleRequestersMapper;
import ret.webscrapers.scraping.data.ActorPoolManager;
import ret.webscrapers.scraping.data.ScrapersThrottler;
import ret.webscrapers.scraping.data.inzeraty.query.InzeratQueryActor;
import ret.webscrapers.scraping.data.urls.UrlsScraperActor;

import java.util.List;
import java.util.Optional;

@PrecededBy({UrlsScraperActor.class})
@FollowedBy(InzeratQueryActor.class)
public class InzeratQueryManagerActor extends SimplePipeActorBase<FinishedInzeratMsg> implements ScrapersThrottler {

    private static final Logger log = LogManager.getLogger(InzeratQueryManagerActor.class);
    @ActorClass(InzeratQueryActor.class)
    private final ActorPoolManager<String> inzeratQueryActorsManager;
    private final PartialStatsManager statsManager = new PartialStatsManager();


    // constructor
    public InzeratQueryManagerActor(ActorPoolManager<String> inzeratQueryActorsManager, SimpleRequestersMapper requestersMapper) {
        super(requestersMapper);
        this.inzeratQueryActorsManager = inzeratQueryActorsManager;
        requestersMapper.addActorWithAllowedRequestorsReplacement(getSelf());
    }


    public static Props props(ActorPoolManager<String> inzeratQueryActorsManager, SimpleRequestersMapper requestersMapper) {
        return Props.create(InzeratQueryManagerActor.class, () ->
                new InzeratQueryManagerActor(inzeratQueryActorsManager, requestersMapper));
    }


    // message
    public static class ScrapeInzeratyGroupMsg extends WebSegmentPipeMsg {
        private final List<String> urls;

        public ScrapeInzeratyGroupMsg(List<String> urls, WebSegment webSegment) {
            super(webSegment);
            this.urls = urls;
        }

        public List<String> getUrls() {
            return urls;
        }

        @Override
        public String toString() {
            return "ScrapeInzeratyMsg{" +
                    "webSegment=" + webSegment +
                    ", urls=" + urls +
                    '}';
        }
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ScrapeInzeratyGroupMsg.class, this::onScrapeInzeratyGroupMsg)
                .match(FinishedInzeratMsg.class,     this::onFinishedByNextPipeActor)
                .match(ThrottleScrapersMsg.class,    this::onThrottleScrapersMsg)
                .matchAny(                           this::unhandled)
                .build();
    }


    private void onScrapeInzeratyGroupMsg(ScrapeInzeratyGroupMsg msg) {
        logReceivedMsg(log, msg);
        setSenderAsRequestor(Optional.empty());
        WebSegment webSegment = msg.getWebSegment();
        inzeratQueryActorsManager.enqueueItemsToProcess(msg.getUrls());
        statsManager.incrementUrlsTotal(msg.getUrls().size());
        tellActorsToScrape(webSegment);
    }


    private void tellActorsToScrape(WebSegment webSegment) {
        WebsiteEnum website = webSegment.getWebsite();
        while (inzeratQueryActorsManager.hasItemsToProcess() && inzeratQueryActorsManager.hasAvailableActors(website)) {
            ActorRef actor = inzeratQueryActorsManager.dequeueActor(website).get();
            String url = inzeratQueryActorsManager.dequeueItemToProcess().get();
            String inzeratIdentifier = Inzerat.makeIdentifier(url);
            sendToNextPipeActor(actor, new ScrapeInzeratMsg(webSegment, url, inzeratIdentifier), Optional.of(inzeratIdentifier));
            inzeratQueryActorsManager.incrementRemainingAnswers();
        }
    }


    @Override
    protected void onFinishedByNextPipeActor(FinishedInzeratMsg msg) {
        int remainingAnswers = inzeratQueryActorsManager.decrementRemainingAnswers();
        String inzeratIdentifier = msg.getInzeratIdentifier();
        logReceivedMsgInz(log, msg, inzeratIdentifier);
        log.info("{}: Remaining responses: {}", selfName, remainingAnswers);

        WebSegment webSegment = msg.getWebSegment();
        WebsiteEnum website = webSegment.getWebsite();
        inzeratQueryActorsManager.enqueueActor(website, getSender()); // here when enqueued they migt get throttled ...
        statsManager.updatePartialStats(msg.getStatus());

        if (inzeratQueryActorsManager.hasItemsToProcess()) {
            tellActorsToScrape(webSegment);
        } else {
            if (!inzeratQueryActorsManager.anyRemainingAnswers()) {
                PartialWebSegmentStats statsCopy = statsManager.getCopy();
                statsManager.resetPartialStats();
                UrlsScraperActor.FinishedScrapingInzeratyGroupMsg finishedGroupMsg = new UrlsScraperActor.FinishedScrapingInzeratyGroupMsg(webSegment, statsCopy);
                sendToPrevPipeActor(finishedGroupMsg, Optional.empty());
            } else {
                // do nothing - wait for the rest to come in
            }
        }
    }


    @Override
    public void onThrottleScrapersMsg(ThrottleScrapersMsg msg) {
        logReceivedMsg(log, msg);
        inzeratQueryActorsManager.setActorsToKeepActive(msg.getThrottleCommand().getScrapersCountToKeepActive(), msg.getWebsite());
    }


    private static class PartialStatsManager {

        private PartialWebSegmentStats partialStats = new PartialWebSegmentStats();

        private void incrementUrlsTotal(int increment) {
            partialStats.incrementUrlsTotal(increment);
        }

        private PartialWebSegmentStats getCopy(){
            return new PartialWebSegmentStats(partialStats);
        }

        private void updatePartialStats(FinishedStatus status) {
            if (FinishedStatus.consideredSuccess(status)) {
                partialStats.incrementSuccessfulTotal(1);
            } else {
                partialStats.incrementFailedTotal(1);
            }
        }

        private void resetPartialStats() {
            partialStats = new PartialWebSegmentStats();
        }
    }

}
