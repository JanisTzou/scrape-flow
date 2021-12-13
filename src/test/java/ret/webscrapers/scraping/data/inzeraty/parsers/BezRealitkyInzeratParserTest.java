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

package ret.webscrapers.scraping.data.inzeraty.parsers;

import aaanew.drivers.SeleniumDriverManager;
import aaanew.drivers.SeleniumDriversFactory;
import aaanew.drivers.lifecycle.QuitAfterIdleInterval;
import aaanew.drivers.lifecycle.RestartDriverAfterInterval;
import aaanew.throttling.model.ScrapedDataType;
import org.junit.Ignore;
import org.junit.Test;
import ret.appcore.model.ScrapedInzerat;
import ret.appcore.model.WebSegment;
import ret.appcore.model.enums.TypNabidkyEnum;
import ret.appcore.model.enums.TypNemovitostiEnum;
import ret.appcore.model.enums.WebsiteEnum;
import ret.webscrapers.AppConfig;
import ret.webscrapers.scraping.data.inzeraty.InzeratScraperImpl;

import java.util.Optional;

@Ignore
public class BezRealitkyInzeratParserTest {

    @Test
    public void testParsingValue() {

        SeleniumDriversFactory driversFactory = new SeleniumDriversFactory(AppConfig.getChromeDriverDir(),true);

        SeleniumDriverManager driverManager = new SeleniumDriverManager(ScrapedDataType.INZERAT,
                new RestartDriverAfterInterval(1000),
                new QuitAfterIdleInterval(1000),
                driversFactory);

        String url = "https://www.bezrealitky.cz/nemovitosti-byty-domy/532974-nabidka-prodej-bytu";

        BezRealitkyInzeratParser parser = new BezRealitkyInzeratParser(driverManager);

        InzeratScraperImpl inzeratScraper = new InzeratScraperImpl(parser);

        WebSegment webSegment = new WebSegment(WebsiteEnum.BEZ_REALITKY, TypNabidkyEnum.PRODEJ, TypNemovitostiEnum.BYT, null, null, null, null, null, null, null, null, null);
        Optional<ScrapedInzerat> scrapedInzerat = inzeratScraper.scrapeInzerat(webSegment, url);


        System.out.println(scrapedInzerat);

    }

}
