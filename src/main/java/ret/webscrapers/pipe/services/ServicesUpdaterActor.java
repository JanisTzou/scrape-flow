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

package ret.webscrapers.pipe.services;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.webscrapers.init.PipeInitialiserActor;

import java.time.Duration;

public class ServicesUpdaterActor extends AbstractActor {

    private static Logger log = LogManager.getLogger(ServicesUpdaterActor.class);
    private final Duration servicesUpdateRetryPeriod;
    private final Duration servicesUpdatePeriod;
    private final static UpdateServicesMsg UPDATE_SERVICES_MSG = new UpdateServicesMsg();
    private final Services services;


    public ServicesUpdaterActor(Services services,
                                Duration servicesUpdateRetryPeriod,
                                Duration servicesUpdatePeriod) {

        this.services = services;
        this.servicesUpdateRetryPeriod = servicesUpdateRetryPeriod;
        this.servicesUpdatePeriod = servicesUpdatePeriod;
    }

    public static Props props(Services services,
                              Duration servicesInitRetryPeriod,
                              Duration servicesUpdatePeriod) {

        return Props.create(ServicesUpdaterActor.class, () ->
                new ServicesUpdaterActor(services, servicesInitRetryPeriod, servicesUpdatePeriod));
    }


    // message
    public static class InitiateServicesMsg {}

    // message
    private static class UpdateServicesMsg {}


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InitiateServicesMsg.class,       this::onInitiateServicesMsg)
                .match(UpdateServicesMsg.class,         this::onUpdateServicesMsg)
                .build();

    }

    private void onInitiateServicesMsg(InitiateServicesMsg msg) {
        log.info("Got InitiateServicesMsg");
        boolean initSuccess = services.initOrUpdateServices(Services.Operation.INITIALISATION);
        if (initSuccess) {
            getSender().tell(new PipeInitialiserActor.ServicesInitialisationResultMsg(initSuccess), getSelf());
            scheduleNextUpdate(UPDATE_SERVICES_MSG, servicesUpdatePeriod);
            log.info(">>> INITIALISATION of SERVICES SUCCESSFUL. GOING TO UPDATE IN {} <<<", servicesUpdatePeriod);
        } else {
            scheduleRetry(msg);
            log.info(">>> INITIALISATION of some SERVICES FAILED. GOING TO RETRY IN {} <<<", servicesUpdateRetryPeriod);
        }
    }

    private void scheduleRetry(InitiateServicesMsg msg) {
        ActorSystem system = getContext().getSystem();
        system.scheduler().scheduleOnce(servicesUpdateRetryPeriod, getSelf(), msg, system.dispatcher(), getSender());
    }


    private void onUpdateServicesMsg(UpdateServicesMsg msg) {
        log.info("UpdateServicesMsg received.");
        Duration delay = servicesUpdatePeriod;
        boolean overallSuccess = services.initOrUpdateServices(Services.Operation.UPDATE);
        if (overallSuccess) {
            log.info(">>> UPDATE of SERVICES SUCCESSFUL. GOING TO UPDATE IN {} <<<", delay);
        } else {
            delay = servicesUpdateRetryPeriod;
            log.info(">>> UPDATE of some SERVICES FAILED. GOING TO RETRY IN {} <<<", delay);
        }
        scheduleNextUpdate(UPDATE_SERVICES_MSG, delay);
    }


    private void scheduleNextUpdate(Object msg, Duration delay) {
        ActorSystem system = getContext().getSystem();
        system.scheduler().scheduleOnce(delay, getSelf(), msg, system.dispatcher(), getSelf());
    }




    @Override
    public void preStart() throws Exception {
        super.preStart();
    }


    @Override
    public void postStop() throws Exception {
    }


}
