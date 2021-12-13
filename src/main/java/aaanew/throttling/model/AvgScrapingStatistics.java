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

package aaanew.throttling.model;


import ret.appcore.model.enums.WebsiteEnum;

public class AvgScrapingStatistics {

    private final ScrapedDataType type;
    private final WebsiteEnum website;
    private final long avgPerItemScrapingDurationInMillis;

    public AvgScrapingStatistics(ScrapedDataType type, WebsiteEnum website, long avgPerItemScrapingDurationInMillis) {
        this.type = type;
        this.website = website;
        this.avgPerItemScrapingDurationInMillis = avgPerItemScrapingDurationInMillis;
    }

    public ScrapedDataType getType() {
        return type;
    }

    public WebsiteEnum getWebsite() {
        return website;
    }

    public long getAvgPerItemScrapingDurationInMillis() {
        return avgPerItemScrapingDurationInMillis;
    }


    @Override
    public String toString() {
        return "AvgScrapingDuration{" +
                "type=" + type +
                ", website=" + website +
                ", avgPerItemScrapingDurationInMillis=" + avgPerItemScrapingDurationInMillis +
                '}';
    }
}
