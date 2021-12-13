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


import com.github.web.scraping.lib.throttling.model.AvgScrapingStatistics;
import com.github.web.scraping.lib.throttling.model.ResponsivenessStatistics;
import com.github.web.scraping.lib.throttling.model.ScrapedDataType;
import com.github.web.scraping.lib.throttling.model.SingleScrapingResponsivenessData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.enums.WebsiteEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResponsivenessStatisticsAggregator {

    private static final Logger log = LogManager.getLogger(ResponsivenessStatisticsAggregator.class);
    private final List<ResponsivenessStatistics> cumulativeStatisticsList = new ArrayList<>();


    public ResponsivenessStatisticsAggregator(int minTotalScrapedItemsCountToConsider,
                                              long maxStatsAgeInMillis) {

        init(minTotalScrapedItemsCountToConsider, maxStatsAgeInMillis);
    }


    private void init(int minTotalScrapedItemsCountToConsider, long maxStatsAgeInMillis) {
        for (WebsiteEnum website : WebsiteEnum.values()) {
            for (ScrapedDataType statsType : ScrapedDataType.values()) {
                cumulativeStatisticsList.add(new ResponsivenessStatistics(website, statsType, minTotalScrapedItemsCountToConsider, maxStatsAgeInMillis));
            }
        }
    }


    public synchronized void add(SingleScrapingResponsivenessData statistics) {
        long millis = statistics.getFinishedScrapingTs() - statistics.getStartedScrapingTs();
        int totalItems = statistics.getItemsScraped();
        Optional<ResponsivenessStatistics> cumulativeStatisticsOp = getResponsivenessStatisticsFor(statistics.getWebsite(), statistics.getType());
        if (cumulativeStatisticsOp.isPresent()) {
            cumulativeStatisticsOp.get().incrementTotalItems(totalItems);
            cumulativeStatisticsOp.get().incrementTotalItemScrapingDurationMillis(millis);
            cumulativeStatisticsOp.get().setLastUpdateTs(System.currentTimeMillis());
        } else {
            log.error("STATISTICS FOR TYPE: '{}' AND WEBSITE '{}'  NOT INITIATED! SingleScraperResponsivenessStatistics not added!", statistics.getWebsite(), statistics.getType());
        }
    }


    public synchronized Optional<AvgScrapingStatistics> avgScrapingStatisticsFor(WebsiteEnum website, ScrapedDataType type) {
        Optional<ResponsivenessStatistics> cumulativeStatsOp = getResponsivenessStatisticsFor(website, type);
        if (cumulativeStatsOp.isPresent()) {
            return cumulativeStatsOp.get().calcAvgScrapingStats();
        } else {
            log.error("STATISTICS FOR TYPE: '{}' AND WEBSITE '{}'  NOT INITIATED! Can not calculate AvgScrapingStatistics.", website, type);
            return Optional.empty();
        }
    }

    private Optional<ResponsivenessStatistics> getResponsivenessStatisticsFor(WebsiteEnum website, ScrapedDataType type) {
        return cumulativeStatisticsList.stream().filter((ResponsivenessStatistics stats) ->  stats.getWebsite().equals(website) && stats.getType().equals(type)).findFirst();
    }

    public synchronized void resetCumulativeStatisticsIfPossible() {
        for (ResponsivenessStatistics statistics : cumulativeStatisticsList) {
            statistics.resetStatsIfPossible();
        }
    }

}
