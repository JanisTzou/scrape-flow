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
import java.util.Random;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.junit.Assert.*;

public class ScrapingRateLimiterTest {

    private final LocalDateTime time = LocalDateTime.of(2020, 1, 1, 12, 0, 1);
    private final LocalDateTime prevRequestTime = time.minusSeconds(1);


    @Test
    public void isRequestWithinLimitBasicTest() {

        int requestsPerSecondLimit = 10;
        ScrapingRateLimiter scrapingRateLimiter = new ScrapingRateLimiterImpl(requestsPerSecondLimit, prevRequestTime);

        for (int count = 1; count <= requestsPerSecondLimit + 1; count++) {
            if (count <= requestsPerSecondLimit) {
                assertTrue("isRequestWithinLimit for count " + count + " must be true!", scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(time));
            } else {
                // last request should not be within limit
                assertFalse(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(time));
            }
        }

    }

    @Test
    public void isRequestWithinLimitWhenNewRequestTimeAfterOneWholeSecondHasPassed() {

        int requestsPerSecondLimit = 10;
        ScrapingRateLimiter scrapingRateLimiter = new ScrapingRateLimiterImpl(requestsPerSecondLimit, prevRequestTime);

        for (int second = 0; second < 2; second++) {
            LocalDateTime rqTime = time.plusSeconds(second);
            for (int count = 1; count <= requestsPerSecondLimit + 1; count++) {
                if (count <= requestsPerSecondLimit) {
                    assertTrue("isRequestWithinLimit for count " + count + " must be true!", scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(rqTime));
                } else {
                    // last request should not be within limit
                    assertFalse(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(rqTime));
                }
            }
        }

    }

    @Test
    public void isRequestWithinLimitWhenNewRequestTimeAfterNotWholeSecondHasPassed() {

        int requestsPerSecondLimit = 2;
        ScrapingRateLimiter scrapingRateLimiter = new ScrapingRateLimiterImpl(requestsPerSecondLimit, prevRequestTime);

        assertTrue(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(time));
        assertTrue(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(time.plus(100, MILLIS)));
        assertFalse(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(time.plus(200, MILLIS)));

        LocalDateTime timeAfterOneSecond = time.plusSeconds(1);
        assertTrue(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterOneSecond));
        assertFalse(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterOneSecond));
        assertTrue(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterOneSecond.plus(100, MILLIS)));
        assertFalse(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterOneSecond.plus(200, MILLIS)));

        LocalDateTime timeAfterTwoSeconds = time.plusSeconds(2);
        assertTrue(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterTwoSeconds.plus(90, MILLIS)));
        assertFalse(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterTwoSeconds.plus(95, MILLIS)));
        assertTrue(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterTwoSeconds.plus(100, MILLIS)));
        assertFalse(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterTwoSeconds.plus(100, MILLIS)));
        assertFalse(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterTwoSeconds.plus(300, MILLIS)));
        assertFalse(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterTwoSeconds.plus(900, MILLIS)));

        LocalDateTime timeAfterOneMinuteAndSecond = time.plusMinutes(1).plusSeconds(1);
        assertTrue(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterOneMinuteAndSecond));
        assertTrue(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterOneMinuteAndSecond.plus(90, MILLIS)));
        assertFalse(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterOneMinuteAndSecond.plus(100, MILLIS)));
        assertFalse(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(timeAfterOneMinuteAndSecond.plus(200, MILLIS)));
    }

    @Test
    public void testForAllSeconds() {

        int requestsPerSecondLimit = 10;
        ScrapingRateLimiter scrapingRateLimiter = new ScrapingRateLimiterImpl(requestsPerSecondLimit, prevRequestTime);

        for (int second = 0; second < 60; second++) {
            LocalDateTime rqTime = time.plusSeconds(second);
            assertTrue(scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(rqTime));
        }

    }

    // just for exploring the log outputs
    @Ignore
    @Test
    public void testPassageOfTime() throws InterruptedException {

        ScrapingRateLimiter scrapingRateLimiter = new ScrapingRateLimiterImpl(1);


        for (int second = 0; second < 120; second++) {

            for (int attemptNo = 0; attemptNo < 10; attemptNo++) {
                LocalDateTime now = LocalDateTime.now();
                boolean withinLimit = scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(now);
                System.out.println( "now = " + now + "; second = " + second + "; withinLimit = " + withinLimit);
                Thread.sleep(100);
            }

        }

    }

    // just for exploring the log outputs
    @Ignore
    @Test
    public void testPassageOfTime2() {

        ScrapingRateLimiter scrapingRateLimiter = new ScrapingRateLimiterImpl(10, prevRequestTime);

        LocalDateTime prevTime = time;
        Random random = new Random(1000);


        for (int second = 0; second < 1000; second++) {

            for (int attemptNo = 0; attemptNo < 100; attemptNo++) {
                int nextRqMillis = random.nextInt(100);
                LocalDateTime currTime = prevTime.plus(nextRqMillis, MILLIS);
                prevTime = currTime;
                boolean withinLimit = scrapingRateLimiter.incrementIfRequestWithinLimitAndGet(currTime);
                System.out.println(currTime + " = " + withinLimit);
            }

        }

    }


}
