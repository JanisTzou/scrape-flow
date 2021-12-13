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

import org.junit.Test;
import ret.webscrapers.AppConfig;

import java.util.List;

public class ScraperSettingsAllTest {

    @Test
    public void getWebsiteToActorsToKeepAliveMap() {
        List<ScraperSettings> scrapersSettings = AppConfig.getScrapersSettings();
        ScraperSettingsAll scraperSettingsAll = new ScraperSettingsAll(scrapersSettings);
        System.out.println(scraperSettingsAll.getWebsiteToActorsToKeepAliveMap(ScrapedDataType.IMAGES));
    }
}
