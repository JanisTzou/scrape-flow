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

package ret.webscrapers.cycles;

import akka.actor.ActorRef;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.scraping.ScrapingCycleFinished;
import ret.appcore.model.scraping.ScrapingCycleStarted;
import ret.webscrapers.actors.ActorClass;
import ret.webscrapers.actors.ActorUtils;
import ret.webscrapers.actors.FollowedBy;
import ret.webscrapers.actors.PrecededBy;
import ret.webscrapers.data.send.DataSenderActor;
import ret.webscrapers.data.send.DataSending;
import ret.webscrapers.http.RestClient;
import ret.webscrapers.messages.FinishedCycleMsg;
import ret.webscrapers.messages.PipeMsg;
import ret.webscrapers.messages.StartedCycleMsg;
import ret.webscrapers.pipe.SimplePipeActorBase;
import ret.webscrapers.pipe.SimpleRequestersMapper;
import ret.webscrapers.websegments.WebSegmentsActor;
import ret.webscrapers.websegments.WebSegmentsManagerActor;

import java.util.Optional;

/**
 * - Initial cycle will take a few days as it will have to download all the images ... subsequent cycles will take less time and should finish within the night
 */
@PrecededBy(CyclesManagerActor.class)
@FollowedBy({WebSegmentsManagerActor.class, DataSenderActor.class})
public class CyclesActor extends SimplePipeActorBase<CyclesActor.FinishedAllWebSegments> implements RestClient, DataSending {

    private static Logger log = LogManager.getLogger(CyclesActor.class);
    @ActorClass(WebSegmentsActor.class)
    private final ActorRef webSegmensManagerActor;
    @ActorClass(DataSenderActor.class)
    private final ActorRef dataSenderActor;
    private final CycleStartFinishRecorder cycleStartFinishRecorder;


    public CyclesActor(ActorRef webSegmentsActor,
                       ActorRef dataSenderActor,
                       SimpleRequestersMapper requestersMapper,
                       CycleStartFinishRecorder cycleStartFinishRecorder) {

        super(requestersMapper);
        this.webSegmensManagerActor = webSegmentsActor;
        this.dataSenderActor = dataSenderActor;
        this.cycleStartFinishRecorder = cycleStartFinishRecorder;
    }


    public static Props props(ActorRef webSegmensManagerActor,
                              ActorRef dataSenderActor,
                              SimpleRequestersMapper requestersMapper,
                              CycleStartFinishRecorder cycleStartFinishRecorder) {

        return Props.create(CyclesActor.class, () -> new CyclesActor(webSegmensManagerActor, dataSenderActor, requestersMapper, cycleStartFinishRecorder));
    }


    // message
    public static class StartNextCycleMsg {}

    // message
    public static class FinishedAllWebSegments extends PipeMsg {}


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartNextCycleMsg.class,         this::onStartNextCycleMsg)
                .match(FinishedAllWebSegments.class,    this::onFinishedByNextPipeActor)
                .matchAny(                              this::unhandled)
                .build();
    }


    private void onStartNextCycleMsg(StartNextCycleMsg msg) {
        logReceivedMsg(log, msg, ">>> STARTING NEW CYCLE <<<");
        setSenderAsRequestor(Optional.empty());
        setupNewCycle();
        sendToNextPipeActor(webSegmensManagerActor, new WebSegmentsManagerActor.StartSegmentsScraping(), Optional.empty());
    }


    private void setupNewCycle() {
        ScrapingCycleStarted scrapingCycleStarted = cycleStartFinishRecorder.setScrapingCycleStarted();
        StartedCycleMsg startedCycleMsg = new StartedCycleMsg(scrapingCycleStarted);
        sendToDataSenderActor(startedCycleMsg);
    }


    @Override
    protected void onFinishedByNextPipeActor(FinishedAllWebSegments msg) {
        logReceivedMsg(log, msg);
        ScrapingCycleFinished scrapingCycleFinished = cycleStartFinishRecorder.setScrapingCycleFinished();
        FinishedCycleMsg finishedCycleMsg = new FinishedCycleMsg(scrapingCycleFinished);
        sendToPrevPipeActor(finishedCycleMsg, Optional.empty());
        sendToDataSenderActor(finishedCycleMsg);
    }


    @Override
    public void sendToDataSenderActor(Object dataMsg) {
        log.info("{}: Sending data to {}", selfName, ActorUtils.getName(dataSenderActor));
        dataSenderActor.tell(dataMsg, getSelf());
    }

}
