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

package aaanew;

import aaanew.dom.data.parsing.HtmlUnitParsingStrategy;
import aaanew.dom.data.parsing.HtmlUnitParsingStrategyByFullXPath;
import aaanew.dom.data.parsing.HtmlUnitSiteParser;
import aaanew.drivers.HtmlUnitDriverManager;
import aaanew.drivers.HtmlUnitDriversFactory;
import aaanew.scraping.Scraper;
import aaanew.throttling.model.ScrapedDataType;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(ScrapedDataType.INZERAT, new HtmlUnitDriversFactory());
        List<HtmlUnitParsingStrategy> parsingStrategies = List.of(new HtmlUnitParsingStrategyByFullXPath(ParsedFieldId.EVENT_LINK, "/html/body/div[1]/div/div[2]/div[2]/div/div[6]/section[1]/div[2]/div/div/table/tbody/tr[1]/td[1]/a"));
        HtmlUnitSiteParser siteParser = new HtmlUnitSiteParser(driverManager, parsingStrategies);
        EntryPoint entryPoint = new EntryPoint("https://www.ifortuna.cz/sazeni/fotbal", new SiteScrapingSettings(siteParser));


        Scraper scraper = new Scraper();

        scraper.start(entryPoint);

    }

    public enum ParsedFieldId {
        EVENT_LINK
    }

}
