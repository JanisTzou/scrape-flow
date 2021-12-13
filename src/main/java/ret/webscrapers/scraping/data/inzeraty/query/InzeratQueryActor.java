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

package ret.webscrapers.scraping.data.inzeraty.query;

import akka.actor.ActorRef;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.webscrapers.actors.ActorClass;
import ret.webscrapers.actors.FollowedBy;
import ret.webscrapers.actors.PrecededBy;
import ret.webscrapers.http.RestClient;
import ret.webscrapers.messages.FinishedInzeratMsg;
import ret.webscrapers.messages.FinishedStatus;
import ret.webscrapers.messages.ScrapeInzeratMsg;
import ret.webscrapers.pipe.SimplePipeActorBase;
import ret.webscrapers.pipe.SimpleRequestersMapper;
import ret.webscrapers.scraping.data.inzeraty.InzeratQueryManagerActor;
import ret.webscrapers.scraping.data.inzeraty.InzeratScraperActor;

import java.util.Optional;

@PrecededBy(InzeratQueryManagerActor.class)
@FollowedBy({InzeratScraperActor.class})
public class InzeratQueryActor extends SimplePipeActorBase<FinishedInzeratMsg> implements RestClient {

    private static final Logger log = LogManager.getLogger(InzeratQueryActor.class);
    @ActorClass(InzeratScraperActor.class)
    private final ActorRef scraperActor;
    private final InzeratQueryService inzeratQueryService;


    // constructor
    public InzeratQueryActor(InzeratQueryService inzeratQueryService,
                             ActorRef scraperActor,
                             SimpleRequestersMapper requestersMapper) {

        super(requestersMapper);
        this.inzeratQueryService = inzeratQueryService;
        this.scraperActor = scraperActor;
    }

    public static Props props(InzeratQueryService inzeratQueryService,
                              ActorRef scraperActor,
                              SimpleRequestersMapper requestersMapper) {

        return Props.create(InzeratQueryActor.class, () -> new InzeratQueryActor(inzeratQueryService, scraperActor, requestersMapper));
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ScrapeInzeratMsg.class, this::onScrapeUrlMsg)
                .match(FinishedInzeratMsg.class, this::onFinishedByNextPipeActor)
                .matchAny(this::unhandled)
                .build();
    }


    private void onScrapeUrlMsg(ScrapeInzeratMsg msg) {
        String inzeratUrl = msg.getInzeratUrl();
        String inzeratIdentifier = msg.getInzeratIdentifier();
        logReceivedMsgInz(log, msg, inzeratIdentifier);
        setSenderAsRequestor(Optional.of(inzeratIdentifier));
        boolean doScrape = inzeratQueryService.shouldScrapeInzeratFor(inzeratUrl);
        if (doScrape) {
            log.info("{}: Inzerat {} is to be scraped", selfName, inzeratIdentifier);
            sendToNextPipeActor(scraperActor, msg, Optional.of(inzeratIdentifier));
        } else {
            log.info("{}: Inzerat {} is NOT to be scraped", selfName, inzeratIdentifier);
            FinishedInzeratMsg finishedMsg = new FinishedInzeratMsg(msg.getWebSegment(), inzeratUrl, FinishedStatus.NOT_TO_SCRAPE, inzeratIdentifier);
            onFinishedByNextPipeActor(finishedMsg);
        }
    }


    @Override
    protected void onFinishedByNextPipeActor(FinishedInzeratMsg msg) {
        String inzeratIdentifier = msg.getInzeratIdentifier();
        logReceivedMsgInz(log, msg, inzeratIdentifier);
        sendToPrevPipeActor(msg, Optional.of(inzeratIdentifier));
    }

}
