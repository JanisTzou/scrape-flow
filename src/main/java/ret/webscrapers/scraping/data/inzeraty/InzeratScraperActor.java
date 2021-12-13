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
import ret.appcore.model.enums.ScrapedInzeratStatus;
import ret.appcore.model.enums.ScrapedInzeratStatusEnum;
import ret.webscrapers.actors.ActorClass;
import ret.webscrapers.actors.ActorUtils;
import ret.webscrapers.actors.FollowedBy;
import ret.webscrapers.actors.PrecededBy;
import ret.webscrapers.data.send.DataSenderActor;
import ret.webscrapers.data.send.DataSending;
import ret.webscrapers.messages.*;
import ret.webscrapers.pipe.SimpleRequestersMapper;
import ret.webscrapers.scraping.data.ScraperActorBase;
import ret.webscrapers.scraping.data.images.query.ImageQueryActor;
import ret.webscrapers.scraping.data.inzeraty.query.InzeratQueryActor;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;


@PrecededBy(InzeratQueryActor.class)
@FollowedBy({ImageQueryActor.class, DataSenderActor.class})
public class InzeratScraperActor extends ScraperActorBase<FinishedInzeratMsg> implements DataSending, StatisticsSending {

	private static final Logger log = LogManager.getLogger(InzeratScraperActor.class);
	@ActorClass(ImageQueryActor.class)
	private final ActorRef imagesQueryActor;
	private final InzeratScraper inzeratScraper;
	@ActorClass(DataSenderActor.class)
	private final ActorRef dataSenderActor;
	@ActorClass(ResponsivenessDataCollectingActor.class)
	private final ActorRef responsivenessStatsCollectorActor;


	// constructor
	public InzeratScraperActor(ActorRef imagesQueryActor,
							   InzeratScraper inzeratScraper,
							   ActorRef dataSenderActor,
							   SimpleRequestersMapper requestersMapper,
							   ActorRef responsivenessStatsCollectorActor,
							   Duration checkForIdleDriverInterval){

		super(requestersMapper, checkForIdleDriverInterval);
		this.imagesQueryActor = imagesQueryActor;
		this.inzeratScraper = inzeratScraper;
		this.dataSenderActor = dataSenderActor;
		this.responsivenessStatsCollectorActor = responsivenessStatsCollectorActor;
	}

	public static Props props(ActorRef scrapeImagesQueryActor,
							  InzeratScraper imageScraper,
							  ActorRef dataSenderActor,
							  SimpleRequestersMapper requestersMapper,
							  ActorRef responsivenessStatsCollectorActor,
							  Duration checkForIdleDriverInterval){

		return Props.create(InzeratScraperActor.class, () ->
				new InzeratScraperActor(scrapeImagesQueryActor, imageScraper, dataSenderActor, requestersMapper, responsivenessStatsCollectorActor, checkForIdleDriverInterval));
	}


	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(ScrapeInzeratMsg.class, this::onScrapeInzeratMsg)
				.match(FinishedInzeratMsg.class, this::onFinishedByNextPipeActor)
				.match(QuitWebDriverIfIdleMsg.class, this::onQuitWebDriverIfIdleMsg)
				.matchAny(this::unhandled)
				.build();
	}


	private void onScrapeInzeratMsg(ScrapeInzeratMsg msg) {
		String inzeratIdentifier = msg.getInzeratIdentifier();
		setSenderAsRequestor(Optional.ofNullable(inzeratIdentifier));
		String inzeratUrl = msg.getInzeratUrl();
		logReceivedMsgInz(log, msg, inzeratIdentifier);
		WebSegment wSegment = msg.getWebSegment();

		SingleScrapingResponsivenessData responsivenessStats = SingleScrapingResponsivenessData.startRecordingStats(msg.getWebSegment().getWebsite(), ScrapedDataType.INZERAT);
		Optional<ScrapedInzerat> inzeratOp = inzeratScraper.scrapeInzerat(wSegment, inzeratUrl);
		responsivenessStats.finishRecordingStats(1, System.currentTimeMillis());
		sendStatisticsToStatisticsCollector(responsivenessStats);

		if (inzeratOp.isPresent()) {
			ScrapedInzerat scrapedInzerat = inzeratOp.get();
			log.info("{}: Scraped inzerat: {}", selfName, inzeratIdentifier);
			InzeratDataMsg message = new InzeratDataMsg(scrapedInzerat, wSegment, inzeratUrl, inzeratIdentifier);
			sendToNextPipeActor(imagesQueryActor, message, Optional.ofNullable(inzeratIdentifier));
		} else {
			log.warn("{}: Failed to scrape data of inzerat: {}", selfName, inzeratIdentifier);
			ScrapedInzerat scrapedInzerat = ScrapedInzerat.emptyScrapedInzerat(inzeratUrl, wSegment.getWebsite(), wSegment.getTypNabidky(), wSegment);
			scrapedInzerat.setScrapedInzeratStatus(new ScrapedInzeratStatus(ScrapedInzeratStatusEnum.CAN_BE_PROCESSED));
			InzeratWithImagesDataMsg dataMsg = new InzeratWithImagesDataMsg(scrapedInzerat, wSegment, inzeratUrl, Collections.emptyList(), inzeratIdentifier);
			sendToDataSenderActor(dataMsg);

			FinishedInzeratMsg finMsg = msg.toFinishedInzeratMsg(FinishedStatus.FAILED);
			onFinishedByNextPipeActor(finMsg);
		}
	}


	@Override
	protected void onFinishedByNextPipeActor(FinishedInzeratMsg msg) {
		String inzeratIdentifier = msg.getInzeratIdentifier();
		logReceivedMsgInz(log, msg, inzeratIdentifier);
		sendToPrevPipeActor(msg, Optional.ofNullable(inzeratIdentifier));
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
		return inzeratScraper;
	}

}
