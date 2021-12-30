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

package com.github.scraping.flow.throttling.model;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.enums.WebsiteEnum;

import java.util.Optional;

public class ResponsivenessStatistics {

    private static final Logger log = LogManager.getLogger(ResponsivenessStatistics.class);

    // settings
    private final WebsiteEnum website;
    private final ScrapedDataType type;
    private final int minTotalScrapedItemsCountToConsider;
    private final long maxStatsAgeInMillis;

    // stats
    private volatile int totalItems;
    private volatile long totalItemsScrapingDurationMillis;
    private volatile long lastUpdateTs;


    public ResponsivenessStatistics(WebsiteEnum website, ScrapedDataType type, int minTotalScrapedItemsCountToConsider, long maxStatsAgeInMillis) {
        this.website = website;
        this.type = type;
        this.minTotalScrapedItemsCountToConsider = minTotalScrapedItemsCountToConsider;
        this.maxStatsAgeInMillis = maxStatsAgeInMillis;
        this.lastUpdateTs = System.currentTimeMillis(); // To avoid being reset from the very beginning
    }

    public WebsiteEnum getWebsite() {
        return website;
    }

    public ScrapedDataType getType() {
        return type;
    }

    public void incrementTotalItems(int increment) {
        totalItems += increment;
    }

    public void incrementTotalItemScrapingDurationMillis(long incrementMillis) {
        totalItemsScrapingDurationMillis += incrementMillis;
    }

    public void setLastUpdateTs(long lastUpdateTs) {
        this.lastUpdateTs = lastUpdateTs;
    }


    public Optional<AvgScrapingStatistics> calcAvgScrapingStats() {
        long avgDuration = avgScrapingDurationForAnItemInMillis();
        if (avgDuration > -1) {
            log.debug("AvgScrapingDurationForAnItemInMillis for '{}', '{}' is = {} millis based on {} items", website, type, avgDuration, totalItems);
            return Optional.of(new AvgScrapingStatistics(type, website, avgDuration));
        }
        return Optional.empty();
    }

    /**
     * @return -1 if ang cannot be provided with existing data, otherwise returns calculated average.
     */
    public long avgScrapingDurationForAnItemInMillis() {
        if (totalItemsScrapingDurationMillis > 0 && statsRecentEnough(lastUpdateTs) && enoughItemsScraped(totalItems)) {
            return totalItemsScrapingDurationMillis / totalItems;
        }
        return -1;
    }

    public boolean resetStatsIfPossible() {
        if (shouldClearStats()) {
            forceResetStats();
            return true;
        }
        return false;
    }


    private boolean shouldClearStats() {
        return !statsRecentEnough(lastUpdateTs) ||  enoughItemsScraped(totalItems);
    }


    private boolean statsRecentEnough(long lastStatsUpdateTs) {
        long now = System.currentTimeMillis();
        if (now < (lastStatsUpdateTs + maxStatsAgeInMillis)) {
            return true;
        }
        log.info("Statistics  for '{}', '{}' not recent enough", website, type);
        return false;
    }


    private boolean enoughItemsScraped(int totalItems) {
        boolean enoughItems = totalItems >= minTotalScrapedItemsCountToConsider;
        if (enoughItems) log.debug("Statistics  for '{}', '{}' have enough items scraped. TotalItems = {}", website, type, totalItems);
        return enoughItems;
    }


    public void forceResetStats() {
        log.info("Clearing statistics for '{}', '{}'", website, type);
        totalItems = 0;
        totalItemsScrapingDurationMillis = 0;
        lastUpdateTs = System.currentTimeMillis();
    }
}
