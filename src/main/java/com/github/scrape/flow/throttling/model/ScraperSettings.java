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

package com.github.scrape.flow.throttling.model;


import ret.appcore.model.enums.WebsiteEnum;

public class ScraperSettings {

    private final WebsiteEnum website;
    private final ScrapedDataType type;
    private final int minScrapers;
    private final int maxScrapers;
    private final int lowerItemScrapingDurationMillis;
    private final int upperItemScrapingDurationMillis;

    public ScraperSettings(WebsiteEnum website, ScrapedDataType type, int minScrapers, int maxScrapers, int lowerItemScrapingDurationMillis, int upperItemScrapingDurationMillis) {
        this.website = website;
        this.type = type;
        this.minScrapers = minScrapers;
        this.maxScrapers = maxScrapers;
        this.lowerItemScrapingDurationMillis = lowerItemScrapingDurationMillis;
        this.upperItemScrapingDurationMillis = upperItemScrapingDurationMillis;
    }

    public WebsiteEnum getWebsite() {
        return website;
    }

    public ScrapedDataType getType() {
        return type;
    }

    public int getMinScrapers() {
        return minScrapers;
    }

    public int getMaxScrapers() {
        return maxScrapers;
    }

    public int getLowerItemScrapingDurationMillis() {
        return lowerItemScrapingDurationMillis;
    }

    public int getUpperItemScrapingDurationMillis() {
        return upperItemScrapingDurationMillis;
    }

    @Override
    public String toString() {
        return "ScrapersSettings{" +
                "website=" + website +
                ", type=" + type +
                ", minScrapers=" + minScrapers +
                ", maxScrapers=" + maxScrapers +
                ", lowerItemScrapingDurationMillis=" + lowerItemScrapingDurationMillis +
                ", upperItemScrapingDurationMillis=" + upperItemScrapingDurationMillis +
                '}';
    }
}
