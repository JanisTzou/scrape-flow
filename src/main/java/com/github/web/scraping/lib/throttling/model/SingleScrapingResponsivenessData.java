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

public class SingleScrapingResponsivenessData {

    private final ScrapedDataType type;
    private final WebsiteEnum website;
    private volatile long finishedScrapingTs;
    private final long startedScrapingTs;
    private volatile int itemsScraped;
    private volatile long duration;


    private SingleScrapingResponsivenessData(ScrapedDataType type, WebsiteEnum website) {
        this.type = type;
        this.website = website;
        startedScrapingTs = System.currentTimeMillis();
    }

    public ScrapedDataType getType() {
        return type;
    }

    public WebsiteEnum getWebsite() {
        return website;
    }

    public long getStartedScrapingTs() {
        return startedScrapingTs;
    }

    public long getFinishedScrapingTs() {
        return finishedScrapingTs;
    }

    public int getItemsScraped() {
        return itemsScraped;
    }

    protected void setFinishedScrapingTs(long finishedScrapingTs) {
        this.finishedScrapingTs = finishedScrapingTs;
        this.duration = finishedScrapingTs - startedScrapingTs;
    }

    public static SingleScrapingResponsivenessData startRecordingStats(WebsiteEnum website, ScrapedDataType type) {
        return new SingleScrapingResponsivenessData(type, website);
    }

    public void finishRecordingStats(int itemsScraped, long finishedScrapingTs) {
        this.itemsScraped = itemsScraped;
        setFinishedScrapingTs(finishedScrapingTs);
    }


    @Override
    public String toString() {
        return "SingleScrapingResponsivenessData{" +
                "type=" + type +
                ", website=" + website +
                ", finishedScrapingTs=" + finishedScrapingTs +
                ", startedScrapingTs=" + startedScrapingTs +
                ", itemsScraped=" + itemsScraped +
                ", duration=" + duration +
                '}';
    }
}
