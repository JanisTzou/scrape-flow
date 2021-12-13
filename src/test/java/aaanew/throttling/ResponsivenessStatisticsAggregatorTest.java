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
import org.junit.Test;
import ret.appcore.model.enums.WebsiteEnum;

import java.util.Collections;
import java.util.Optional;

public class ResponsivenessStatisticsAggregatorTest {


    @Test
    public void test() {

        ResponsivenessStatisticsAggregator statisticsAggregator = new ResponsivenessStatisticsAggregator(1, 5000);

        SingleScrapingResponsivenessData responsivenessData = SingleScrapingResponsivenessData.startRecordingStats(WebsiteEnum.SREALITY, ScrapedDataType.INZERAT);
        responsivenessData.finishRecordingStats(1, System.currentTimeMillis() + 2500);
        statisticsAggregator.add(responsivenessData);

        ScraperSettings scraperSettings = new ScraperSettings(WebsiteEnum.SREALITY, ScrapedDataType.INZERAT, 2, 10, 1000, 11000);

        ScraperSettingsAll scraperSettingsAll = new ScraperSettingsAll(Collections.singletonList(scraperSettings));

        ThrottlingCalculator throttlingCalculator = new ThrottlingCalculator(statisticsAggregator, scraperSettingsAll);

        Optional<ThrottleCommand> throttleCommand = throttlingCalculator.evaluateResponsiveness(WebsiteEnum.SREALITY, ScrapedDataType.INZERAT);

        System.out.println(throttleCommand);


    }



}
