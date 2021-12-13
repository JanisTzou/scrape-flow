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

import ret.webscrapers.http.HttpClient;
import ret.webscrapers.http.HttpServiceBase;

import java.time.Duration;
import java.time.LocalTime;

public class CyclesServiceImpl extends HttpServiceBase implements CyclesService {

    public CyclesServiceImpl(HttpClient httpClient) {
        super(httpClient);
    }

    // With this solution it is possible that a slower scraper might lose a cycle - when it takes too long to finish ... Might not be a problem though ...
    @Override
    public Duration calcTimeTillNextStart(int scraperId, LocalTime now) {

        LocalTime time_20_00 = LocalTime.of(20, 00);
        LocalTime time_23_59 = LocalTime.of(00, 00).minusNanos(1);
        LocalTime time_00_01 = LocalTime.of(00, 00).plusNanos(1);

        if (now.isAfter(time_00_01) && now.isBefore(time_20_00)) {
            int delaySecs = time_20_00.toSecondOfDay() - now.toSecondOfDay();
            return Duration.ofSeconds(delaySecs);

        } else if (now.isAfter(time_20_00) && now.isBefore(time_23_59)) {
            return Duration.ofSeconds(0);

        } else {
            return Duration.ofSeconds(0);
//            HttpResponse<Boolean> response = httpClient.makeRequest(HttpConfig.areOtherClientsScrapingURL, HttpClient.Method.GET, null, Boolean.class);
        }
    }


    @Override
    public boolean initialiseOrUpdate() {
        // maybe init not needed ...
        return true;
    }

    @Override
    public void reset() {
        // not needed
    }
}
