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

package com.github.web.scraping.lib.throttling;

import akka.actor.*;
import com.github.web.scraping.lib.throttling.model.ScrapedDataType;
import com.github.web.scraping.lib.throttling.model.ThrottleCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.enums.WebsiteEnum;

import java.time.Duration;
import java.util.Optional;

@Deprecated // TODO throttling should be implemented differently ...
public class ThrottleManagerActor extends AbstractActor {

    private static final Logger log = LogManager.getLogger(ThrottleManagerActor.class);
    private final ThrottlingCalculator throttler;
    private final ActorRef inzeratScrapersManagerActor;
    private final ActorRef imageScrapersManagerActor;
    private Cancellable nextAnalysisCancellable;
    private final Duration responsivenessCheckInterval;


    public ThrottleManagerActor(ThrottlingCalculator throttler,
                                ActorRef inzeratScrapersManagerActor,
                                ActorRef imageScrapersManagerActor,
                                Duration responsivenessCheckInterval) {

        this.throttler = throttler;
        this.inzeratScrapersManagerActor = inzeratScrapersManagerActor;
        this.imageScrapersManagerActor = imageScrapersManagerActor;
        this.responsivenessCheckInterval = responsivenessCheckInterval;
    }


    public static Props props(ThrottlingCalculator throttleManager,
                              ActorRef inzeratQueryManagerActor,
                              ActorRef imageScrapersManagerActor,
                              Duration responsivenessCheckInterval) {

        return Props.create(ThrottleManagerActor.class, () ->
                new ThrottleManagerActor(throttleManager, inzeratQueryManagerActor, imageScrapersManagerActor, responsivenessCheckInterval));
    }


    // message
    private static class RunStatisticsAnalysis {}


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RunStatisticsAnalysis.class, this::onRunStatisticsAnalysis)
                .matchAny(this::unhandled)
                .build();
    }


    @SuppressWarnings("OptionalIsPresent")
    private void onRunStatisticsAnalysis(RunStatisticsAnalysis msg) {
        log.info("Got RunStatisticsAnalysis");

        for (WebsiteEnum website : WebsiteEnum.values()) {
            Optional<ThrottleCommand> inzThrottleCommandOp = throttler.evaluateResponsiveness(website, ScrapedDataType.INZERAT);
            if (inzThrottleCommandOp.isPresent()) {
                inzeratScrapersManagerActor.tell(new ThrottleScrapersMsg(website, inzThrottleCommandOp.get()), getSelf());
            }

            Optional<ThrottleCommand> imgThrottleCommandOp = throttler.evaluateResponsiveness(website, ScrapedDataType.IMAGES);
            if (imgThrottleCommandOp.isPresent()) {
                imageScrapersManagerActor.tell(new ThrottleScrapersMsg(website, imgThrottleCommandOp.get()), getSelf());
            }
        }

        throttler.resetStatsIfPossible();
        scheduleNextAnalysisRun();
    }


    @Override
    public void unhandled(Object msg) {
        log.error("ThrottleManagerActor received UNKNOWN MESSAGE: {}", msg);
    }

    public void sendToDataSenderActor(Object dataMsg) {
    }


    public void scheduleNextAnalysisRun() {
        ActorSystem system = getContext().getSystem();
        Runnable task = () -> getSelf().tell(new RunStatisticsAnalysis(), getSelf());
        nextAnalysisCancellable = system.scheduler().scheduleOnce(responsivenessCheckInterval, task, system.dispatcher());
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        scheduleNextAnalysisRun();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        if (nextAnalysisCancellable != null && !nextAnalysisCancellable.isCancelled()) {
            nextAnalysisCancellable.cancel();
        }
    }
}
