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

package ret.webscrapers.data.send;

import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.wrappers.CycleFinishedWrapper;
import ret.appcore.model.wrappers.CycleStartedWrapper;
import ret.appcore.model.wrappers.InzeratAndImageDataWrapper;
import ret.appcore.model.wrappers.WebSegmentScrapingStatsWrapper;
import ret.appcore.utils.ImageUtils;
import ret.webscrapers.actors.PrecededBy;
import ret.webscrapers.data.DataService;
import ret.webscrapers.messages.*;
import ret.webscrapers.pipe.MultipleRequestersMapper;
import ret.webscrapers.pipe.PipeNarrowingActorBase;
import ret.webscrapers.scraping.data.images.ImageScraperActor;
import ret.webscrapers.scraping.data.images.query.ImageQueryActor;
import ret.webscrapers.scraping.data.inzeraty.InzeratScraperActor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@PrecededBy({InzeratScraperActor.class, ImageScraperActor.class, ImageQueryActor.class})
public class DataSenderActor extends PipeNarrowingActorBase<FinishedInzeratMsg> {

    private static Logger log = LogManager.getLogger(DataSenderActor.class);

    private final DataService dataService;

    private DataSenderActor(DataService dataService, MultipleRequestersMapper requestersMapper){
        super(requestersMapper);
        this.dataService = dataService;
    }

    public static Props props(DataService dataSenderService, MultipleRequestersMapper requestersMapper) {
        return Props.create(DataSenderActor.class, () -> new DataSenderActor(dataSenderService, requestersMapper));
    }

    public static class ReconnectedMsg {}

    public static class DisconnectedMsg {}


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InzeratWithImagesDataMsg.class,  this::onInzeratWithImagesDataMsg)
                .match(WebSegmentStatsMsg.class,        this::onWebSegmentStatsMsg)
                .match(StartedCycleMsg.class,           this::onStartedCycleMsg)
                .match(FinishedCycleMsg.class,          this::onFinishedCycleMsg)
                .match(DisconnectedMsg.class,           this::onDisconnectedMsg)
                .match(ReconnectedMsg.class,            this::onReconnectedMsg)
                .matchAny(this::unhandled)
                .build();
    }

    private void onInzeratWithImagesDataMsg(InzeratWithImagesDataMsg msg) {
        logReceivedMsgInz(log, msg, msg.getInzeratIdentifier());
        List<BufferedImage> images = resizeImages(msg.getBufferedImages(), msg.getInzeratIdentifier());
        dataService.sendData(new InzeratAndImageDataWrapper(msg.getScrapedInzerat(), images), true);
    }

    private void onWebSegmentStatsMsg(WebSegmentStatsMsg msg) {
        logReceivedMsg(log, msg);
        dataService.sendData(new WebSegmentScrapingStatsWrapper(msg.getSegmentStats()), true);
    }

    private void onStartedCycleMsg(StartedCycleMsg msg) {
        logReceivedMsg(log, msg);
        dataService.sendData(new CycleStartedWrapper(msg.getScrapingCycleStarted()), true);
    }

    private void onFinishedCycleMsg(FinishedCycleMsg msg) {
        logReceivedMsg(log, msg);
        dataService.sendData(new CycleFinishedWrapper(msg.getCycleFinished()), true);
    }

    private void onDisconnectedMsg(DisconnectedMsg msg) {
        logReceivedMsg(log, msg);
        // Probably not needed ...
    }

    private void onReconnectedMsg(ReconnectedMsg msg) {
        logReceivedMsg(log, msg);
        dataService.tryResendStoredData();
    }

    @Override
    protected void onFinishedByNextPipeActor(FinishedInzeratMsg msg) {
        logReceivedMsg(log, msg);
        // not supported
    }

    private List<BufferedImage> resizeImages(List<BufferedImage> images, String inzeratIdentifier) {
        List<BufferedImage> result = new ArrayList<>();
        for (BufferedImage bufferedImage : images) {
            if (bufferedImage != null) {
                Optional<BufferedImage> resizedImageOpt = ImageUtils.resizeImage(bufferedImage, 300);
                if (resizedImageOpt.isPresent()) {
                    result.add(resizedImageOpt.get());
                } else {
                    log.warn("Failed to resize image for: {}", inzeratIdentifier);
                    result.add(bufferedImage);
                }
            } else {
                log.warn("Null image for inzerat: {}", inzeratIdentifier);
            }

        }
        return result;
    }

}

