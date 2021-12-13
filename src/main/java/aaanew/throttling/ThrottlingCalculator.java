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

package aaanew.throttling;


import aaanew.throttling.model.*;
import ret.appcore.model.enums.WebsiteEnum;

import java.util.Optional;

public class ThrottlingCalculator {

    private final ResponsivenessStatisticsAggregator statisticsAggregator;
    private final ScraperSettingsAll scrapersSettingsAll;

    public ThrottlingCalculator(ResponsivenessStatisticsAggregator statisticsAggregator, ScraperSettingsAll scrapersSettingsAll) {
        this.statisticsAggregator = statisticsAggregator;
        this.scrapersSettingsAll = scrapersSettingsAll;
    }


    public Optional<ThrottleCommand> evaluateResponsiveness(WebsiteEnum website, ScrapedDataType type) {
        Optional<AvgScrapingStatistics> avgScrapingStatsOp = statisticsAggregator.avgScrapingStatisticsFor(website, type);
        if (avgScrapingStatsOp.isPresent()) {
            ThrottleCommand throttleCommand = makeThrottleCommand(avgScrapingStatsOp.get());
            return Optional.ofNullable(throttleCommand);
        } else {
            return Optional.empty();
        }
    }


    public ThrottleCommand makeThrottleCommand(AvgScrapingStatistics avgScrapingStats) {

        long avgDuration = avgScrapingStats.getAvgPerItemScrapingDurationInMillis();
        ScraperSettings scraperSettings = scrapersSettingsAll.getScrapersSettings(avgScrapingStats.getWebsite(), avgScrapingStats.getType());
        int activeScrapersCount = calcActiveScrapersCount(avgDuration, scraperSettings);

        return new ThrottleCommand(avgScrapingStats, activeScrapersCount);
    }


    int calcActiveScrapersCount(long avgDuration, ScraperSettings scraperSettings) {

        int intervalsCount = calcIntervalsCount(scraperSettings.getMinScrapers(), scraperSettings.getMaxScrapers());
        int oneIntervalMillis = calcDurationInterval(intervalsCount, scraperSettings);
        boolean avgDurationWithinLowerBound = (avgDuration - scraperSettings.getLowerItemScrapingDurationMillis()) <= 0;

        if (avgDurationWithinLowerBound) {
            return scraperSettings.getMaxScrapers();
        } else {
            int diffFromLowerBoundMillis = (int) (avgDuration - scraperSettings.getLowerItemScrapingDurationMillis());
            int wholeIntervalsCountFromLowerBound = diffFromLowerBoundMillis / oneIntervalMillis + 1;

            int activeScrapers = scraperSettings.getMaxScrapers() - wholeIntervalsCountFromLowerBound;
            if (activeScrapers < scraperSettings.getMinScrapers()) {
                return scraperSettings.getMinScrapers();
            } else {
                return activeScrapers;
            }
        }

    }


    private int calcDurationInterval(int intervalCount, ScraperSettings scraperSettings) {
        int upperDurationLimit = scraperSettings.getUpperItemScrapingDurationMillis();
        int lowerDurationLimit = scraperSettings.getLowerItemScrapingDurationMillis();
        return (upperDurationLimit - lowerDurationLimit) / intervalCount;
    }

    private int calcIntervalsCount(int minScrapersCount, int maxScrapersCount) {
        if (maxScrapersCount > minScrapersCount) {
            return maxScrapersCount - minScrapersCount;
        } else {
            return 0;
        }
    }

    public void resetStatsIfPossible() {
        statisticsAggregator.resetCumulativeStatisticsIfPossible();
    }

}
