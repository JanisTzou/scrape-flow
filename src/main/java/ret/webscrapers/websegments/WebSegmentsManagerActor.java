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
import ret.webscrapers.actors.ActorClass;
import ret.webscrapers.actors.FollowedBy;
import ret.webscrapers.actors.PrecededBy;
import ret.webscrapers.cycles.CyclesActor;
import ret.webscrapers.messages.FinishedWebSegmentMsg;
import ret.webscrapers.pipe.SimplePipeActorBase;
import ret.webscrapers.pipe.SimpleRequestersMapper;

import java.util.Optional;

@PrecededBy(CyclesActor.class)
@FollowedBy({WebSegmentsActor.class})
public class WebSegmentsManagerActor extends SimplePipeActorBase<FinishedWebSegmentMsg> {

    private static Logger log = LogManager.getLogger(WebSegmentsManagerActor.class);
    private final WebSegmentsService webSegmentsService;
    @ActorClass(WebSegmentsActor.class)
    private final ActorRef webSegmentsActor;


    public WebSegmentsManagerActor(SimpleRequestersMapper requestersMapper,
                                   WebSegmentsService webSegmentsService,
                                   ActorRef webSegmentsActor) {

        super(requestersMapper);
        this.webSegmentsService = webSegmentsService;
        this.webSegmentsActor = webSegmentsActor;
    }


    public static Props props(SimpleRequestersMapper requestersMapper,
                              WebSegmentsService webSegmentsService,
                              ActorRef webSegmentsActor) {

        return Props.create(WebSegmentsManagerActor.class, () -> new WebSegmentsManagerActor(requestersMapper, webSegmentsService, webSegmentsActor));
    }


    public static class StartSegmentsScraping {}


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartSegmentsScraping.class, this::onStartSegmentsScraping)
                .match(FinishedWebSegmentMsg.class, this::onFinishedByNextPipeActor)
                .matchAny(this::unhandled)
                .build();
    }

    private void onStartSegmentsScraping(StartSegmentsScraping msg) {
        logReceivedMsg(log, msg);
        setSenderAsRequestor(Optional.empty());
        processNextSegmentIfAvailable();
    }


    @Override
    protected void onFinishedByNextPipeActor(FinishedWebSegmentMsg msg) {
        logReceivedMsg(log, msg);
        webSegmentsService.processFinished(msg.getWebSegment());
        processNextSegmentIfAvailable();
    }

    private void processNextSegmentIfAvailable() {
        Optional<WebSegment> nextSegmentOp = webSegmentsService.nextWebSegment();
        if (nextSegmentOp.isPresent()) {
            WebSegment webSegment = nextSegmentOp.get();
            log.info("{}: Next WebSegment to process: {}", selfName, webSegment);
            sendToNextPipeActor(webSegmentsActor, new WebSegmentsActor.ScrapeNextSegmentMsg(webSegment), Optional.empty());
        } else {
            log.info("{}: >>> No more segments - FINISHED SCRAPING CYCLE. <<<", selfName);
            webSegmentsService.reset();
            sendToPrevPipeActor(new CyclesActor.FinishedAllWebSegments(), Optional.empty());
        }
    }

}

