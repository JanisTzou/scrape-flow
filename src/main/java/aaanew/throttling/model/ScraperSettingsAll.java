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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScraperSettingsAll {

    private final List<ScraperSettings> settings;

    public ScraperSettingsAll(List<ScraperSettings> settings) {
        this.settings = settings;
    }

    public ScraperSettings getScrapersSettings(WebsiteEnum website, ScrapedDataType statsType) {
        return settings.stream().filter((ScraperSettings stats) ->  stats.getWebsite().equals(website) && stats.getType().equals(statsType)).findFirst().get();
    }

    public Map<WebsiteEnum, Integer> getWebsiteToActorsToKeepAliveMap(ScrapedDataType type) {
        return settings.stream()
                .filter(scraperSettings -> scraperSettings.getType().equals(type))
                .collect(Collectors.groupingBy(ScraperSettings::getWebsite, Collectors.summingInt(ScraperSettings::getMinScrapers)));
    }
}
