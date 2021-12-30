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

import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@ToString
public class ScrapingRateLimiterImpl implements ScrapingRateLimiter {

    private final int limit;
    private final Duration duration;
    private final Duration requestFreq;
    private LocalDateTime nextRqAllowedTime;

    public ScrapingRateLimiterImpl(int limitPerTimeUnit, TimeUnit timeUnit, LocalDateTime nextRqAllowedTime) {
        this.limit = limitPerTimeUnit;
        this.duration = Duration.of(1, timeUnit.toChronoUnit());
        this.requestFreq = duration.dividedBy(limitPerTimeUnit);
        this.nextRqAllowedTime = nextRqAllowedTime;
    }

    public ScrapingRateLimiterImpl(int limitPerDuration, TimeUnit timeUnit) {
        this(limitPerDuration, timeUnit, LocalDateTime.now());
    }

    // requests evenly spread during the specified period ...
    @Override
    public synchronized boolean incrementIfRequestWithinLimitAndGet(LocalDateTime now) {
        if (now.isEqual(nextRqAllowedTime) || now.isAfter(nextRqAllowedTime)) {
            Duration between = Duration.between(nextRqAllowedTime, now);
            long multiplicand = between.dividedBy(requestFreq) + 1;
            nextRqAllowedTime = nextRqAllowedTime.plus(requestFreq.multipliedBy(multiplicand));
            return true;
        }
        return false;
    }

    @Override
    public Duration getRequestFreq() {
        return requestFreq;
    }
}
