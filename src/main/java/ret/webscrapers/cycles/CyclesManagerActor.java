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
import ret.webscrapers.AppConfig;
import ret.webscrapers.actors.ActorClass;
import ret.webscrapers.actors.FollowedBy;
import ret.webscrapers.actors.PrecededBy;
import ret.webscrapers.init.PipeInitialiserActor;
import ret.webscrapers.messages.FinishedCycleMsg;
import ret.webscrapers.pipe.SimplePipeActorBase;
import ret.webscrapers.pipe.SimpleRequestersMapper;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

@PrecededBy(PipeInitialiserActor.class)
@FollowedBy(CyclesActor.class)
public class CyclesManagerActor extends SimplePipeActorBase<FinishedCycleMsg> {

    private static Logger log = LogManager.getLogger(CyclesManagerActor.class);
    private final CyclesService cyclesService;
    @ActorClass(CyclesActor.class)
    private final ActorRef cyclesActor;


    public CyclesManagerActor(SimpleRequestersMapper requestersMapper,
                              CyclesService cyclesService,
                              ActorRef cyclesActor) {

        super(requestersMapper);
        this.cyclesService = cyclesService;
        this.cyclesActor = cyclesActor;
    }

    public static Props props(SimpleRequestersMapper requestersMapper, CyclesService cyclesService, ActorRef cyclesActor) {
        return Props.create(CyclesManagerActor.class, () -> new CyclesManagerActor(requestersMapper, cyclesService, cyclesActor));
    }


    // message
    public static class InitiateCycles {}


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InitiateCycles.class, this::onInitiateCycles)
                .match(FinishedCycleMsg.class, this::onFinishedByNextPipeActor)
                .matchAny(this::unhandled)
                .build();
    }


    private void onInitiateCycles(InitiateCycles msg) {
        logReceivedMsg(log, msg);
        setSenderAsRequestor(Optional.empty());
        sendToNextPipeActor(cyclesActor, new CyclesActor.StartNextCycleMsg(), Optional.empty());
    }

    @Override
    protected void onFinishedByNextPipeActor(FinishedCycleMsg msg) {
        logReceivedMsg(log, msg);
        scheduleNextCycle();
        sendToPrevPipeActor(msg, Optional.empty());
    }

    private void scheduleNextCycle() {
        Duration nextStartDelay = AppConfig.startNewCycleDelay;
        if (AppConfig.calculateNextCycleStart) {
            nextStartDelay = cyclesService.calcTimeTillNextStart(AppConfig.scraperClientId, LocalTime.now());
        }
        sendToNextPipeActor(cyclesActor, new CyclesActor.StartNextCycleMsg(), nextStartDelay, Optional.empty());
    }

}
