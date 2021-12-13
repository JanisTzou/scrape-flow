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

import com.github.web.scraping.lib.throttling.model.SingleScrapingResponsivenessData;
import akka.actor.AbstractActor;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResponsivenessDataCollectingActor extends AbstractActor {

    private static final Logger log = LogManager.getLogger(ResponsivenessDataCollectingActor.class);
    private final ResponsivenessStatisticsAggregator statistics;

    public ResponsivenessDataCollectingActor(ResponsivenessStatisticsAggregator statistics) {
        this.statistics = statistics;
    }

    public static Props props(ResponsivenessStatisticsAggregator statistics) {
        return Props.create(ResponsivenessDataCollectingActor.class, () -> new ResponsivenessDataCollectingActor(statistics));
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SingleScrapingResponsivenessData.class, stats -> {
                    log.info("Got statistics: {}", stats);
                    statistics.add(stats);
                })
                .matchAny(this::unhandled)
                .build();
    }

    @Override
    public void unhandled(Object msg) {
        log.error("ResponsivenessStatsCollectorActor received UNKNOWN MESSAGE: {}", msg);
    }

}
