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

package ret.webscrapers.scraping.data.urls;

import aaanew.drivers.DriverOperator;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.WebSegment;
import ret.appcore.model.scraping.PartialWebSegmentStats;
import ret.appcore.model.scraping.WebSegmentScrapingStats;
import ret.webscrapers.actors.ActorClass;
import ret.webscrapers.actors.FollowedBy;
import ret.webscrapers.actors.PrecededBy;
import ret.webscrapers.data.send.DataSenderActor;
import ret.webscrapers.messages.FinishedWebSegmentMsg;
import ret.webscrapers.messages.QuitWebDriverIfIdleMsg;
import ret.webscrapers.messages.WebSegmentPipeMsg;
import ret.webscrapers.pipe.SimpleRequestersMapper;
import ret.webscrapers.scraping.data.ScraperActorBase;
import ret.webscrapers.scraping.data.inzeraty.InzeratQueryManagerActor;
import ret.webscrapers.websegments.WebSegmentsActor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PrecededBy(WebSegmentsActor.class)
@FollowedBy({InzeratQueryManagerActor.class, DataSenderActor.class})
public class UrlsScraperActor extends ScraperActorBase<UrlsScraperActor.FinishedScrapingInzeratyGroupMsg> {

    private static final Logger log = LogManager.getLogger(UrlsScraperActor.class);
    private final UrlsScraperBase urlsScraper;
    @ActorClass(InzeratQueryManagerActor.class)
    private final ActorRef inzeratQueryManagerActor;
    private final SegmentStatsManager statsManager = new SegmentStatsManager();
    private UrlsCounter urlsCounter = new UrlsCounter();


    // constructor
    public UrlsScraperActor(UrlsScraperBase urlsScraper,
                            ActorRef inzeratQueryManagerActor,
                            SimpleRequestersMapper requestersMapper,
                            Duration checkForIdleDriverInterval) {

        super(requestersMapper, checkForIdleDriverInterval);
        this.urlsScraper = urlsScraper;
        this.inzeratQueryManagerActor = inzeratQueryManagerActor;
    }

    public static Props props(UrlsScraperBase urlsScraper,
                              ActorRef scraperManagerActor,
                              SimpleRequestersMapper requestersMapper,
                              Duration checkForIdleDriverInterval) {

        return Props.create(UrlsScraperActor.class, () -> new UrlsScraperActor(urlsScraper, scraperManagerActor, requestersMapper, checkForIdleDriverInterval));
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(ScrapeSegmentUrlsMsg.class, this::onScrapeSegmentUrlsMsg)
                .match(FinishedScrapingInzeratyGroupMsg.class, this::onFinishedByNextPipeActor)
                .match(QuitWebDriverIfIdleMsg.class, this::onQuitWebDriverIfIdleMsg)
                .matchAny(this::unhandled)
                .build();
    }

    private void onScrapeSegmentUrlsMsg(ScrapeSegmentUrlsMsg msg) {
        logReceivedMsg(log, msg);
        setSenderAsRequestor(Optional.empty());
        statsManager.setupCurrSegmentStats(msg);
        urlsCounter = new UrlsCounter();
        urlsScraper.initiate();
        String segmentUrl = msg.getWebSegment().getUrl();

        while (true) {
            urlsScraper.gotoNextPage(segmentUrl);
            if (urlsScraper.hasSegmentMoreUrls()) {
                List<String> urls = urlsScraper.scrapeInzeratUrls(segmentUrl);
                log.info("{}: Scraped {} urls.", selfName, urls.size());
                if (urls.size() > 0) {
                    urlsCounter.incrementSent(urls.size());
                    InzeratQueryManagerActor.ScrapeInzeratyGroupMsg scrapeInzeratyGroupMsg = new InzeratQueryManagerActor.ScrapeInzeratyGroupMsg(new ArrayList<>(urls), msg.getWebSegment());
                    sendToNextPipeActor(inzeratQueryManagerActor, scrapeInzeratyGroupMsg, Optional.empty());
                } else if (urls.isEmpty() && urlsScraper.hasSegmentMoreUrls()) {
                    log.warn("{}: Scrape no urls even though we had identified that there were more => break searching for more urls.", selfName);
                    break;
                }
            } else {
                break;
            }
        }

        log.info("{}: No more urls to scrape.", selfName);
        urlsScraper.cleanUp();
    }

    @Override
    protected void onFinishedByNextPipeActor(FinishedScrapingInzeratyGroupMsg msg) {
        logReceivedMsg(log, msg);
        statsManager.updateCurrentSegmentStats(msg.partialStats);
        urlsCounter.incrementFinished(msg.partialStats.getUrlsTotal());

        if (!urlsScraper.isScrapingInProgress() && !urlsCounter.expectingMoreToFinish()) {
            statsManager.finishCurrentSegmentStats();
            WebSegmentScrapingStats statsCopy = statsManager.copy();
            sendToPrevPipeActor(new FinishedWebSegmentMsg(msg.getWebSegment(), statsCopy), Optional.empty());
        } else {
            // do nothing ... and wait till more urls are scraped ...
        }
    }

    @Override
    public DriverOperator getDriverOperator() {
        return urlsScraper;
    }

    //message
    public static class FinishedScrapingInzeratyGroupMsg extends WebSegmentPipeMsg {
        private final PartialWebSegmentStats partialStats;

        public FinishedScrapingInzeratyGroupMsg(WebSegment webSegment, PartialWebSegmentStats partialStats) {
            super(webSegment);
            this.partialStats = partialStats;
        }

        @Override
        public String toString() {
            return "FinishedScrapingInzeratyGroupMsg{" +
                    "webSegment=" + webSegment +
                    ", partialStats=" + partialStats +
                    '}';
        }
    }

    //message
    public static class ScrapeSegmentUrlsMsg extends WebSegmentPipeMsg {
        public ScrapeSegmentUrlsMsg(WebSegment webSegment) {
            super(webSegment);
        }

        @Override
        public String toString() {
            return "ScrapeSegmentUrlsMsg{" +
                    "webSegment=" + webSegment +
                    '}';
        }
    }
}
