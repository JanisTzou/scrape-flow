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

package ret.webscrapers.scraping.data.images.parsers;

import aaanew.drivers.SeleniumDriverManager;
import aaanew.drivers.SeleniumDriversFactory;
import aaanew.drivers.lifecycle.DriverQuitStrategy;
import aaanew.drivers.lifecycle.DriverRestartStrategy;
import aaanew.drivers.lifecycle.QuitAfterIdleInterval;
import aaanew.drivers.lifecycle.RestartDriverAfterInterval;
import aaanew.throttling.model.ScrapedDataType;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ret.appcore.model.ScrapedInzerat;
import ret.appcore.model.WebSegment;
import ret.appcore.model.enums.TypNabidkyEnum;
import ret.appcore.model.enums.TypNemovitostiEnum;
import ret.appcore.model.enums.WebsiteEnum;
import ret.webscrapers.AppConfig;
import ret.webscrapers.scraping.data.inzeraty.parsers.BezRealitkyInzeratParser;

import java.util.Optional;

@Ignore
public class BezRealitkyinzeratParserTest {

    SeleniumDriverManager seleniumDriverManager;

    @After
    public void tearDown() {
        if (seleniumDriverManager != null) {
            seleniumDriverManager.terminateDriver();
        }
    }

    @Test
    public void scrapeImages() {
        String inzeratUrl = "https://www.bezrealitky.cz/nemovitosti-byty-domy/575938-nabidka-prodej-bytu";

        DriverRestartStrategy driverRestartStrategy = new RestartDriverAfterInterval(10000);
        DriverQuitStrategy driverQuitStrategy = new QuitAfterIdleInterval(5);
        SeleniumDriversFactory seleniumDriversFactory = new SeleniumDriversFactory(AppConfig.getChromeDriverDir(), false);
        seleniumDriverManager = new SeleniumDriverManager(ScrapedDataType.INZERAT, driverRestartStrategy, driverQuitStrategy, seleniumDriversFactory);
        BezRealitkyInzeratParser inzeratParserBR = new BezRealitkyInzeratParser(seleniumDriverManager);

        Optional<ScrapedInzerat> scrapedInzeratOpt = inzeratParserBR.scrapeData(inzeratUrl, WebsiteEnum.BEZ_REALITKY, TypNabidkyEnum.PRODEJ, TypNemovitostiEnum.BYT, new WebSegment());

        System.out.println(scrapedInzeratOpt.get());
    }
}
