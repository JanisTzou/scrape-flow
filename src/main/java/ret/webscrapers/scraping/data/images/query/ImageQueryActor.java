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

package ret.webscrapers.scraping.data.images.query;

import akka.actor.ActorRef;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.webscrapers.actors.ActorClass;
import ret.webscrapers.actors.ActorUtils;
import ret.webscrapers.actors.FollowedBy;
import ret.webscrapers.actors.PrecededBy;
import ret.webscrapers.data.send.DataSenderActor;
import ret.webscrapers.data.send.DataSending;
import ret.webscrapers.http.RestClient;
import ret.webscrapers.messages.FinishedInzeratMsg;
import ret.webscrapers.messages.FinishedStatus;
import ret.webscrapers.messages.InzeratDataMsg;
import ret.webscrapers.messages.InzeratWithImagesDataMsg;
import ret.webscrapers.pipe.SimplePipeActorBase;
import ret.webscrapers.pipe.SimpleRequestersMapper;
import ret.webscrapers.scraping.data.images.ImageScrapersManagerActor;
import ret.webscrapers.scraping.data.inzeraty.InzeratScraperActor;

import java.util.Collections;
import java.util.Optional;

@PrecededBy(InzeratScraperActor.class)
@FollowedBy({ImageScrapersManagerActor.class, DataSenderActor.class})
public class ImageQueryActor extends SimplePipeActorBase<FinishedInzeratMsg> implements RestClient, DataSending {

    private static final Logger log = LogManager.getLogger(ImageQueryActor.class);
    private final ImageQueryService imageQueryService;
    @ActorClass(ImageScrapersManagerActor.class)
    private final ActorRef imageScrapersManager;
    @ActorClass(DataSenderActor.class)
    private final ActorRef dataSenderActor;
    private final boolean imageScrapingEnabled;


    public ImageQueryActor(ImageQueryService imageQueryService,
                           ActorRef imageScrapersManager,
                           ActorRef dataSenderActor,
                           SimpleRequestersMapper requestersMapper,
                           boolean imageScrapingEnabled) {

        super(requestersMapper);
        this.imageQueryService = imageQueryService;
        this.imageScrapersManager = imageScrapersManager;
        this.dataSenderActor = dataSenderActor;
        this.imageScrapingEnabled = imageScrapingEnabled;
    }

    public static Props props(ImageQueryService imageQueryService,
                              ActorRef imageScrapersManagers,
                              ActorRef dataSenderActor,
                              SimpleRequestersMapper requestersMapper,
                              boolean imageScrapingEnabled) {

        return Props.create(ImageQueryActor.class, () ->
                new ImageQueryActor(imageQueryService, imageScrapersManagers, dataSenderActor, requestersMapper, imageScrapingEnabled));
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InzeratDataMsg.class, this::onScrapedInzeratDataMsg)
                .match(FinishedInzeratMsg.class, this::onFinishedByNextPipeActor)
                .matchAny(this::unhandled)
                .build();
    }


    private void onScrapedInzeratDataMsg(InzeratDataMsg msg) {

        String inzeratIdentifier = msg.getInzeratIdentifier();
        logReceivedMsgInz(log, msg, inzeratIdentifier);
        setSenderAsRequestor(Optional.of(inzeratIdentifier));

        boolean doScrapeImages = false;

        if (imageScrapingEnabled) {
            doScrapeImages = imageQueryService.shouldScrapeImageFor(msg.getInzeratUrl());
        }

        if (doScrapeImages) {
            log.info("{}: Will scrape images for: {}", selfName, inzeratIdentifier);
            sendToNextPipeActor(imageScrapersManager, msg, Optional.of(inzeratIdentifier));
        } else {
            log.info("{}: Do not scrape images for: {}", selfName, inzeratIdentifier);
            FinishedInzeratMsg finishedMsg = msg.toFinishedInzeratMsg(FinishedStatus.SUCCESS_INZERAT);
            sendToPrevPipeActor(finishedMsg, Optional.of(inzeratIdentifier));
            InzeratWithImagesDataMsg dataMsg = msg.toInzeratWithImagesDataMsg(Collections.EMPTY_LIST);
            sendToDataSenderActor(dataMsg);
        }
    }


    @Override
    protected void onFinishedByNextPipeActor(FinishedInzeratMsg msg) {
        String inzeratIdentifier = msg.getInzeratIdentifier();
        logReceivedMsgInz(log, msg, inzeratIdentifier);
        sendToPrevPipeActor(msg, Optional.of(inzeratIdentifier));
    }

    @Override
    public void sendToDataSenderActor(Object dataMsg) {
        log.info("{}: Sending data to {}", selfName, ActorUtils.getName(dataSenderActor));
        dataSenderActor.tell(dataMsg, getSelf());
    }

}
