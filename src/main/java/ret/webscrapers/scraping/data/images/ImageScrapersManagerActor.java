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

package ret.webscrapers.scraping.data.images;

import akka.actor.ActorRef;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.WebSegment;
import ret.appcore.model.enums.WebsiteEnum;
import ret.webscrapers.actors.ActorUtils;
import ret.webscrapers.actors.FollowedBy;
import ret.webscrapers.actors.PrecededBy;
import ret.webscrapers.messages.FinishedInzeratMsg;
import ret.webscrapers.messages.InzeratDataMsg;
import ret.webscrapers.messages.ThrottleScrapersMsg;
import ret.webscrapers.pipe.MultipleRequestersMapper;
import ret.webscrapers.pipe.PipeNarrowingActorBase;
import ret.webscrapers.scraping.data.ActorPoolManager;
import ret.webscrapers.scraping.data.ScrapersThrottler;
import ret.webscrapers.scraping.data.images.query.ImageQueryActor;

import java.util.Optional;

@PrecededBy(ImageQueryActor.class)
@FollowedBy({ImageScraperActor.class})
public class ImageScrapersManagerActor extends PipeNarrowingActorBase<FinishedInzeratMsg> implements ScrapersThrottler {

    private static final Logger log = LogManager.getLogger(ImageScrapersManagerActor.class);
    private final ActorPoolManager<InzeratDataMsg> imageScrapersManager;


    public ImageScrapersManagerActor(ActorPoolManager<InzeratDataMsg> imageScrapersManager,
                                     MultipleRequestersMapper requestersMapper) {

        super(requestersMapper);
        this.imageScrapersManager = imageScrapersManager;
    }

    public static Props props(ActorPoolManager<InzeratDataMsg> imageScrapersManager,
                              MultipleRequestersMapper requestersMapper) {

        return Props.create(ImageScrapersManagerActor.class, () ->
                new ImageScrapersManagerActor(imageScrapersManager, requestersMapper));
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InzeratDataMsg.class,        this::onInzeratDataMsg)
                .match(FinishedInzeratMsg.class,    this::onFinishedByNextPipeActor)
                .match(ThrottleScrapersMsg.class,   this::onThrottleScrapersMsg)
                .matchAny(this::unhandled)
                .build();
    }


    private void onInzeratDataMsg(InzeratDataMsg msg) {
        String inzeratIdentifier = msg.getInzeratIdentifier();
        logReceivedMsgInz(log, msg, inzeratIdentifier);
        setSenderAsRequestor(inzeratIdentifier);
        WebSegment webSegment = msg.getWebSegment();
        imageScrapersManager.enqueueItemsToProcess(msg);
        tellActorsToScrape(webSegment);
    }


    private void tellActorsToScrape(WebSegment webSegment) {
        WebsiteEnum website = webSegment.getWebsite();
        while (imageScrapersManager.hasItemsToProcess() && imageScrapersManager.hasAvailableActors(website)) {
            ActorRef actor = imageScrapersManager.dequeueActor(website).get();
            InzeratDataMsg dataMsg = imageScrapersManager.dequeueItemToProcess().get();
            ImageScraperActor.ScrapeImagesMsg scrapeImagesMsg = new ImageScraperActor.ScrapeImagesMsg(dataMsg.getScrapedInzerat(), webSegment, dataMsg.getInzeratUrl(), dataMsg.getInzeratIdentifier());
            String inzeratIdentifier = scrapeImagesMsg.getInzeratIdentifier();
            sendToNextPipeActor(actor, scrapeImagesMsg, Optional.of(inzeratIdentifier));
            imageScrapersManager.incrementRemainingAnswers();
        }
    }


    @Override
    protected void onFinishedByNextPipeActor(FinishedInzeratMsg msg) {
        int remainingAnswers = imageScrapersManager.decrementRemainingAnswers();
        String inzeratIdentifier = msg.getInzeratIdentifier();
        logReceivedMsgInz(log, msg, inzeratIdentifier);
        log.info("{}: Remaining responses: {}", selfName, remainingAnswers);

        WebSegment webSegment = msg.getWebSegment();
        log.info("{}: Enqueuing actor: {} after scraping: {}", selfName, ActorUtils.getName(getSender()), inzeratIdentifier);
        imageScrapersManager.enqueueActor(webSegment.getWebsite(), getSender());  // when enqueued they might get throttled

        log.info("{}: unprocessedItemsCount = {}", selfName, imageScrapersManager.unprocessedItemsCount());
        if (imageScrapersManager.hasItemsToProcess()) {
            tellActorsToScrape(webSegment);
        }

        sendToPrevPipeActor(msg, inzeratIdentifier);
    }

    @Override
    public void onThrottleScrapersMsg(ThrottleScrapersMsg msg) {
        log.info("Got ThrottleScrapersMsg: {}", msg);
        imageScrapersManager.setActorsToKeepActive(msg.getThrottleCommand().getScrapersCountToKeepActive(), msg.getWebsite());
    }

}
