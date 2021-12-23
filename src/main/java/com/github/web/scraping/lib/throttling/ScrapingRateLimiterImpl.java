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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

// TODO add support to limit rate by Minute also ...
public class ScrapingRateLimiterImpl implements ScrapingRateLimiter {

    public static final long MILLIS_BETWEEN_CORRESPOND_RQS_IN_TWO_CONSECUTIVE_SECONDS = 1000L;
    private static final int MAX_ALLOWED_REQUESTS_PER_SECOND_LIMIT = 1000;
    private final int requestsPerSecondLimit;
    private final LocalDateTime[] requestsTimesWithinSecond;
    private final AtomicInteger requestsPerSecondCounter = new AtomicInteger(0);

    /**
     * FOR TESTING PURPOSES ONLY
     * package private access is intentional
     *
     * @param initialPrevRequestTime helps testability
     */
    ScrapingRateLimiterImpl(int requestsPerSecondLimit, LocalDateTime initialPrevRequestTime) {

        this.requestsPerSecondLimit = Math.min(MAX_ALLOWED_REQUESTS_PER_SECOND_LIMIT, requestsPerSecondLimit);

        // initialise with default starting time
        this.requestsTimesWithinSecond = new LocalDateTime[this.requestsPerSecondLimit];
        for (int requestNo = 0; requestNo < this.requestsPerSecondLimit; requestNo++) {
            this.requestsTimesWithinSecond[requestNo] = initialPrevRequestTime;
        }
    }

    public ScrapingRateLimiterImpl(int requestsPerSecondLimit) {
        this(
                requestsPerSecondLimit,
                LocalDateTime.now().minus(MILLIS_BETWEEN_CORRESPOND_RQS_IN_TWO_CONSECUTIVE_SECONDS + 1L, ChronoUnit.MILLIS) // initial value - needed
        );
    }

    /**
     * atomically evaluates the limit and increases the counter and returns the result of the limit evaluation.
     * Due to possibly high  contention for mutation the data in the counter we need to ensure that operations are atomic and protected by synchronized access
     *
     * @return returns the result of the limit evaluation of rqs within limit
     */
    @Override
    public synchronized boolean incrementIfRequestWithinLimitAndGet(LocalDateTime now) {
        if (isRequestWithinLimit(now)) {
            incrementRqsPerSecCount(now);
            return true;
        } else {
            return false;
        }
    }

    private void incrementRqsPerSecCount(LocalDateTime now) {
        // order of updates matters here!
        updateRequestsPerSecondCounter();
        updateRequestTimeWithinSecond(now);
    }

    private void updateRequestsPerSecondCounter() {
        if (requestsPerSecondCounter.get() == requestsPerSecondLimit) {
            requestsPerSecondCounter.set(1);
        } else {
            requestsPerSecondCounter.incrementAndGet();
        }
    }

    private void updateRequestTimeWithinSecond(LocalDateTime now) {
        int requestTimePosIdx = requestsPerSecondCounter.get() - 1;
        requestsTimesWithinSecond[requestTimePosIdx] = now;
    }

    private boolean isRequestWithinLimit(LocalDateTime now) {
        return hasWholeSecondPassedSinceCorrespondingRequestInPrevSecond(now);
    }

    private boolean hasWholeSecondPassedSinceCorrespondingRequestInPrevSecond(LocalDateTime now) {
        int idxOfCorrespondingRequestInPrevSecond = calcNextRequestPerSecondCount() - 1;
        LocalDateTime prevCorrespondingRequestTime = requestsTimesWithinSecond[idxOfCorrespondingRequestInPrevSecond];
        return MILLIS_BETWEEN_CORRESPOND_RQS_IN_TWO_CONSECUTIVE_SECONDS <= calcMillisBetween(prevCorrespondingRequestTime, now);
    }

    private int calcNextRequestPerSecondCount() {
        int nextCount = requestsPerSecondCounter.get() + 1;
        if (nextCount <= requestsPerSecondLimit) {
            return nextCount;
        } else {
            return 1;
        }
    }

    private long calcMillisBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MILLIS.between(start, end);
    }

}
