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

package ret.webscrapers.scraping.data.urls.parsers;


import aaanew.drivers.DriverManager;
import aaanew.drivers.DriverManagerFactory;
import aaanew.drivers.HtmlUnitDriversFactory;
import aaanew.throttling.model.ScrapedDataType;
import org.junit.Test;
import ret.appcore.model.enums.WebsiteEnum;

public class IDnesRealityUrlsParserTest {

    @Test
    public void test() {
        HtmlUnitDriversFactory driversFactory = new HtmlUnitDriversFactory();
        DriverManagerFactory driverManagerFactory = new DriverManagerFactory(null, driversFactory);
        DriverManager<?> driverManager = driverManagerFactory.newDriverManager(WebsiteEnum.IDNES_REALITY, ScrapedDataType.URLS);
        UrlsParserFactory urlsParserFactory = new UrlsParserFactory();
        UrlsParser urlsParser = urlsParserFactory.newUrlsParser(WebsiteEnum.IDNES_REALITY, driverManager);
        urlsParser.goToPage("https://reality.idnes.cz/s/byty/olomoucky-kraj/");
        urlsParser.scrapePropertyUrls();
    }

}
