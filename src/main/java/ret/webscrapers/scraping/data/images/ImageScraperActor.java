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

import aaanew.drivers.DriverOperator;
import aaanew.throttling.ResponsivenessDataCollectingActor;
import aaanew.throttling.StatisticsSending;
import aaanew.throttling.model.ScrapedDataType;
import aaanew.throttling.model.SingleScrapingResponsivenessData;
import akka.actor.ActorRef;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.ScrapedInzerat;
import ret.appcore.model.WebSegment;
import ret.webscrapers.actors.ActorClass;
import ret.webscrapers.actors.ActorUtils;
import ret.webscrapers.actors.FollowedBy;
import ret.webscrapers.actors.PrecededBy;
import ret.webscrapers.data.send.DataSenderActor;
import ret.webscrapers.data.send.DataSending;
import ret.webscrapers.messages.FinishedInzeratMsg;
import ret.webscrapers.messages.FinishedStatus;
import ret.webscrapers.messages.InzeratDataMsg;
import ret.webscrapers.messages.QuitWebDriverIfIdleMsg;
import ret.webscrapers.pipe.SimpleRequestersMapper;
import ret.webscrapers.scraping.data.ScraperActorBase;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@PrecededBy(ImageScrapersManagerActor.class)
@FollowedBy(DataSenderActor.class)
public class ImageScraperActor extends ScraperActorBase<FinishedInzeratMsg> implements DataSending, StatisticsSending {

    private static final Logger log = LogManager.getLogger(ImageScraperActor.class);
    @ActorClass(DataSenderActor.class)
    private final ActorRef dataSenderActor;
    private final ImageScraper imageScraper;
    @ActorClass(ResponsivenessDataCollectingActor.class)
    private final ActorRef responsivenessStatsCollectorActor;


    public ImageScraperActor(ActorRef dataSenderActor,
                             ImageScraper imageScraper,
                             SimpleRequestersMapper requestersMapper,
                             Duration checkForIdleDriverInterval,
                             ActorRef responsivenessStatsCollectorActor) {

        super(requestersMapper, checkForIdleDriverInterval);
        this.dataSenderActor = dataSenderActor;
        this.imageScraper = imageScraper;
        this.responsivenessStatsCollectorActor = responsivenessStatsCollectorActor;
    }

    public static Props props(ActorRef dataSenderActor,
                              ImageScraper imageScraper,
                              SimpleRequestersMapper requestersMapper,
                              Duration checkForIdleDriverInterval,
                              ActorRef responsivenessStatsCollectorActor) {

        return Props.create(ImageScraperActor.class, () -> new ImageScraperActor(dataSenderActor, imageScraper, requestersMapper, checkForIdleDriverInterval, responsivenessStatsCollectorActor));
    }


    // message
    public static class ScrapeImagesMsg extends InzeratDataMsg {

        public ScrapeImagesMsg(ScrapedInzerat scrapedInzerat,
                               WebSegment webSegment,
                               String inzeratUrl,
                               String inzeratIdentifier) {
            super(scrapedInzerat, webSegment, inzeratUrl, inzeratIdentifier);
        }

        @Override
        public String toString() {
            return "ScrapeImagesMsg{" +
                    "webSegment=" + webSegment +
                    ", inzeratIdentifier='" + inzeratIdentifier + '\'' +
                    ", inzeratUrl='" + inzeratUrl + '\'' +
                    ", scrapedInzerat=" + scrapedInzerat +
                    '}';
        }
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ScrapeImagesMsg.class,       this::onScrapeImagesMsg)
                .match(FinishedInzeratMsg.class,    this::onFinishedByNextPipeActor)
                .match(QuitWebDriverIfIdleMsg.class,this::onQuitWebDriverIfIdleMsg)
                .matchAny(this::unhandled)
                .build();
    }


    private void onScrapeImagesMsg(ScrapeImagesMsg msg) {
        String inzeratIdentifier = msg.getInzeratIdentifier();
        logReceivedMsgInz(log, msg, inzeratIdentifier);
        setSenderAsRequestor(java.util.Optional.ofNullable(inzeratIdentifier));

        ScrapedInzerat scrapedInzerat = msg.getScrapedInzerat();
        SingleScrapingResponsivenessData responsivenessStats = SingleScrapingResponsivenessData.startRecordingStats(msg.getWebSegment().getWebsite(), ScrapedDataType.IMAGES);
        List<BufferedImage> images = imageScraper.scrapeImages(msg.getInzeratUrl(), scrapedInzerat);

        responsivenessStats.finishRecordingStats(images.size(), System.currentTimeMillis());
        sendStatisticsToStatisticsCollector(responsivenessStats);
        FinishedStatus status = FinishedStatus.SUCCESS_INZERAT;
        if (!images.isEmpty()) {
            status = FinishedStatus.SUCCESS_INZERAT_AND_IMAGES;
        }

        sendToPrevPipeActor(msg.toFinishedInzeratMsg(status), java.util.Optional.ofNullable(inzeratIdentifier));
        sendToDataSenderActor(msg.toInzeratWithImagesDataMsg(images));
    }

    private List<BufferedImage> resizeImages(List<BufferedImage> imagesTmp) {
        List<BufferedImage> images = new ArrayList<>();
        images.addAll(imagesTmp);
        return images;
    }

    @Override
    protected void onFinishedByNextPipeActor(FinishedInzeratMsg msg) {
        String inzeratIdentifier = msg.getInzeratIdentifier();
        logReceivedMsgInz(log, msg, inzeratIdentifier);
        sendToPrevPipeActor(msg, java.util.Optional.ofNullable(inzeratIdentifier));
    }

    @Override
    public void sendToDataSenderActor(Object dataMsg) {
        log.info("{}: Sending data to {}", selfName, ActorUtils.getName(dataSenderActor));
        dataSenderActor.tell(dataMsg, getSelf());
    }

    @Override
    public void sendStatisticsToStatisticsCollector(SingleScrapingResponsivenessData stats) {
        responsivenessStatsCollectorActor.tell(stats, getSelf());
    }

    @Override
    public DriverOperator getDriverOperator() {
        return imageScraper;
    }
}
