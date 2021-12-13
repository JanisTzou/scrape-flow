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



import ret.appcore.model.scraping.ScrapingCycleFinished;
import ret.appcore.model.scraping.ScrapingCycleStarted;
import ret.webscrapers.AppConfig;

import java.time.LocalDateTime;

@Deprecated
public class CycleStartFinishRecorder {

    private volatile LocalDateTime startedScrapingDt;

    public ScrapingCycleStarted setScrapingCycleStarted() {
        startedScrapingDt = LocalDateTime.now().withNano(0);
        return new ScrapingCycleStarted(AppConfig.scraperClientId, startedScrapingDt);
    }

    public ScrapingCycleFinished setScrapingCycleFinished() {
        checkInvariants();
        ScrapingCycleFinished scrapingCycleFinished = new ScrapingCycleFinished(AppConfig.scraperClientId, startedScrapingDt, LocalDateTime.now().withNano(0));
        resetStartedTime();
        return scrapingCycleFinished;
    }

    private void resetStartedTime() {
        startedScrapingDt = null;
    }

    private void checkInvariants() {
        if (startedScrapingDt == null) {
            throw new IllegalStateException("startedScrapingDt has not been set!");
        }
    }

}
