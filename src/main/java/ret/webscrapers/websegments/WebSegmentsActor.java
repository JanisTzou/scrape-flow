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

package ret.webscrapers.websegments;

import akka.actor.ActorRef;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.WebSegment;
import ret.appcore.model.enums.WebsiteEnum;
import ret.webscrapers.actors.ActorClass;
import ret.webscrapers.actors.ActorUtils;
import ret.webscrapers.actors.FollowedBy;
import ret.webscrapers.actors.PrecededBy;
import ret.webscrapers.data.send.DataSenderActor;
import ret.webscrapers.data.send.DataSending;
import ret.webscrapers.http.RestClient;
import ret.webscrapers.messages.FinishedWebSegmentMsg;
import ret.webscrapers.messages.WebSegmentStatsMsg;
import ret.webscrapers.pipe.SimplePipeActorBase;
import ret.webscrapers.pipe.SimpleRequestersMapper;
import ret.webscrapers.scraping.data.urls.UrlsScraperActor;

import java.util.Map;
import java.util.Optional;

@PrecededBy(WebSegmentsManagerActor.class)
@FollowedBy({UrlsScraperActor.class})
public class WebSegmentsActor extends SimplePipeActorBase<FinishedWebSegmentMsg> implements RestClient, DataSending {

    private static Logger log = LogManager.getLogger(WebSegmentsActor.class);
    @ActorClass(UrlsScraperActor.class)
    private final Map<WebsiteEnum, ActorRef> websiteToUrlScrapingActor;
    @ActorClass(DataSenderActor.class)
    private final ActorRef dataSenderActor;


    // constructor
    public WebSegmentsActor(Map<WebsiteEnum, ActorRef> websiteToUrlScrapingActor,
                            ActorRef dataSenderActor,
                            SimpleRequestersMapper requestersMapper) {

        super(requestersMapper);
        this.websiteToUrlScrapingActor = websiteToUrlScrapingActor;
        this.dataSenderActor = dataSenderActor;
    }


    public static Props props(Map<WebsiteEnum, ActorRef> websiteToUrlScrapingActor,
                              ActorRef dataSenderActor,
                              SimpleRequestersMapper requestersMapper) {

        return Props.create(WebSegmentsActor.class, () -> new WebSegmentsActor(websiteToUrlScrapingActor, dataSenderActor, requestersMapper));
    }


    // message
    public static class ScrapeNextSegmentMsg {
        private final WebSegment webSegment;

        public ScrapeNextSegmentMsg(WebSegment webSegment) {
            this.webSegment = webSegment;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ScrapeNextSegmentMsg.class,      this::onScrapeNextSegmentMsg)
                .match(FinishedWebSegmentMsg.class,     this::onFinishedByNextPipeActor)
                .matchAny(this::unhandled)
                .build();
    }


    private void onScrapeNextSegmentMsg(ScrapeNextSegmentMsg msg) {
        logReceivedMsg(log, msg);
        setSenderAsRequestor(Optional.empty());
        ActorRef urlScrapingActor = getUrlScrapingActor(msg.webSegment);
        sendToNextPipeActor(urlScrapingActor, new UrlsScraperActor.ScrapeSegmentUrlsMsg(msg.webSegment), Optional.empty());
    }


    private ActorRef getUrlScrapingActor(WebSegment webSegment) {
        WebsiteEnum website = webSegment.getWebsite();
        return websiteToUrlScrapingActor.get(website);
    }


    @Override
    protected void onFinishedByNextPipeActor(FinishedWebSegmentMsg msg) {
        logReceivedMsg(log, msg);
        WebSegmentStatsMsg statsDataMsg = new WebSegmentStatsMsg(msg.getWebSegmentScrapingStats());
        sendToDataSenderActor(statsDataMsg);
        sendToPrevPipeActor(msg, Optional.empty());
    }

    @Override
    public void sendToDataSenderActor(Object dataMsg) {
        log.info("{}: Sending data to {}", selfName, ActorUtils.getName(dataSenderActor));
        dataSenderActor.tell(dataMsg, getSelf());
    }

}
