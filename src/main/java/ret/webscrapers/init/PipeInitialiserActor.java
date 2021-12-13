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

package ret.webscrapers.init;

import akka.actor.ActorRef;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.webscrapers.actors.ActorClass;
import ret.webscrapers.actors.FollowedBy;
import ret.webscrapers.cycles.CyclesManagerActor;
import ret.webscrapers.messages.FinishedCycleMsg;
import ret.webscrapers.pipe.SimplePipeActorBase;
import ret.webscrapers.pipe.SimpleRequestersMapper;
import ret.webscrapers.pipe.services.ServicesUpdaterActor;

import java.util.Optional;

@FollowedBy(CyclesManagerActor.class)
public class PipeInitialiserActor extends SimplePipeActorBase<FinishedCycleMsg> {

    private static Logger log = LogManager.getLogger(PipeInitialiserActor.class);
    @ActorClass(CyclesManagerActor.class)
    private final ActorRef cyclesManagerActor;
    @ActorClass(ServicesUpdaterActor.class)
    private final ActorRef servicesUpdaterActor;



    public PipeInitialiserActor(ActorRef cyclesManagerActor,
                                ActorRef servicesUpdaterActor,
                                SimpleRequestersMapper requestersMapper) {

        super(requestersMapper);
        this.cyclesManagerActor = cyclesManagerActor;
        this.servicesUpdaterActor = servicesUpdaterActor;
    }

    public static Props props(ActorRef cyclesManagerActor,
                              ActorRef servicesUpdaterActor,
                              SimpleRequestersMapper requestersMapper) {

        return Props.create(PipeInitialiserActor.class, () -> new PipeInitialiserActor(cyclesManagerActor, servicesUpdaterActor, requestersMapper));
    }


    // message
    public static class InitialisePipeMsg {}

    // message
    public static class ServicesInitialisationResultMsg {
        private final boolean isSuccess;
        public ServicesInitialisationResultMsg(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InitialisePipeMsg.class,                 this::onInitialiseMsg)
                .match(ServicesInitialisationResultMsg.class,   this::onServicesInitialisationResultMsg)
                .match(FinishedCycleMsg.class,                  this::onFinishedByNextPipeActor)
                .matchAny(                                      this::unhandled)
                .build();
    }


    private void onInitialiseMsg(InitialisePipeMsg msg) {
        logReceivedMsg(log, msg);
        servicesUpdaterActor.tell(new ServicesUpdaterActor.InitiateServicesMsg(), getSelf());
    }


    private void onServicesInitialisationResultMsg(ServicesInitialisationResultMsg msg) {
        logReceivedMsg(log, msg);
        if (msg.isSuccess) {
            sendToNextPipeActor(cyclesManagerActor, new CyclesManagerActor.InitiateCycles(), Optional.empty());
        } else {
            // wait ...
        }
    }

    @Override
    protected void onFinishedByNextPipeActor(FinishedCycleMsg finishedMsg) {
        logReceivedMsg(log, finishedMsg);
        // do nothing ... this means that the operation has been finished ...
    }

}
