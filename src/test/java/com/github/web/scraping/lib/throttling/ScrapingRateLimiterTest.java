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

import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScrapingRateLimiterTest {

    private final LocalDateTime nowBase = LocalDateTime.of(2020, 1, 1, 12, 0, 1);


    @Test
    public void basicTest() {

        ScrapingRateLimiter limiter = new ScrapingRateLimiterImpl(2, TimeUnit.SECONDS, nowBase);

        assertTrue(limiter.incrementIfRequestWithinLimitAndGet(nowBase));
        assertFalse(limiter.incrementIfRequestWithinLimitAndGet(nowBase.plus(499, TimeUnit.MILLISECONDS.toChronoUnit())));
        assertTrue(limiter.incrementIfRequestWithinLimitAndGet(nowBase.plus(501, TimeUnit.MILLISECONDS.toChronoUnit())));
        assertFalse(limiter.incrementIfRequestWithinLimitAndGet(nowBase.plus(502, TimeUnit.MILLISECONDS.toChronoUnit())));
        assertFalse(limiter.incrementIfRequestWithinLimitAndGet(nowBase.plus(999, TimeUnit.MILLISECONDS.toChronoUnit())));
        assertTrue(limiter.incrementIfRequestWithinLimitAndGet(nowBase.plus(1000, TimeUnit.MILLISECONDS.toChronoUnit())));
        assertFalse(limiter.incrementIfRequestWithinLimitAndGet(nowBase.plus(1001, TimeUnit.MILLISECONDS.toChronoUnit())));
        assertFalse(limiter.incrementIfRequestWithinLimitAndGet(nowBase.plus(1499, TimeUnit.MILLISECONDS.toChronoUnit())));
        assertTrue(limiter.incrementIfRequestWithinLimitAndGet(nowBase.plus(1500, TimeUnit.MILLISECONDS.toChronoUnit())));
    }

    // just for exploring the log outputs
    @Ignore
    @Test
    public void testPassageOfTime() throws InterruptedException {

        ScrapingRateLimiter scrapingRateLimiter = new ScrapingRateLimiterImpl(10, TimeUnit.MINUTES);

        for (int second = 0; second < 120; second++) {

            for (int attemptNo = 0; attemptNo < 10; attemptNo++) {
                LocalDateTime now = LocalDateTime.now();
                boolean withinLimit = scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(now);
                System.out.println( "now = " + now + "; second = " + second + "; withinLimit = " + withinLimit);
                Thread.sleep(100);
            }

        }

    }



}
