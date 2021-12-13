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

package ret.webscrapers.scraping.data.urls;





import ret.appcore.model.scraping.PartialWebSegmentStats;
import ret.appcore.model.scraping.WebSegmentScrapingStats;

import java.time.LocalDateTime;

class SegmentStatsManager {

    private WebSegmentScrapingStats currSegmentStats;

    void setupCurrSegmentStats(UrlsScraperActor.ScrapeSegmentUrlsMsg msg) {
        currSegmentStats = new WebSegmentScrapingStats();
        currSegmentStats.setStartedScrapingDt(LocalDateTime.now());
        currSegmentStats.setWebSegment(msg.getWebSegment());
    }

    void updateCurrentSegmentStats(PartialWebSegmentStats partialStats) {
        currSegmentStats.incrementBy(partialStats);
    }

    void finishCurrentSegmentStats() {
        currSegmentStats.setFinishedScrapingDt(LocalDateTime.now());
    }

    WebSegmentScrapingStats copy() {
        return new WebSegmentScrapingStats(currSegmentStats);
    }
}
