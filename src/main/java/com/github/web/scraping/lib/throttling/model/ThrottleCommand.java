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

package com.github.web.scraping.lib.throttling.model;


import ret.appcore.model.enums.WebsiteEnum;

public class ThrottleCommand {

    private final WebsiteEnum website;
    private final ScrapedDataType type;
    private final int scraperActorsCountToKeepActive;

    public ThrottleCommand(WebsiteEnum website, ScrapedDataType type, int scraperActorsCountToKeepActive) {
        this.website = website;
        this.type = type;
        this.scraperActorsCountToKeepActive = scraperActorsCountToKeepActive;
    }

    public ThrottleCommand(AvgScrapingStatistics avgScrapingStats, int scraperActorsCountToKeepActive) {
        this.website = avgScrapingStats.getWebsite();
        this.type = avgScrapingStats.getType();
        this.scraperActorsCountToKeepActive = scraperActorsCountToKeepActive;
    }


    public WebsiteEnum getWebsite() {
        return website;
    }

    public ScrapedDataType getType() {
        return type;
    }

    public int getScrapersCountToKeepActive() {
        return scraperActorsCountToKeepActive;
    }


    @Override
    public String toString() {
        return "ThrottleCommand{" +
                "website=" + website +
                ", type=" + type +
                ", scraperActorsCountToKeepActive=" + scraperActorsCountToKeepActive +
                '}';
    }
}
