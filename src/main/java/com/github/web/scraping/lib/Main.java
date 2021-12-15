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

package com.github.web.scraping.lib;

import com.github.web.scraping.lib.dom.data.parsing.*;
import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;
import com.github.web.scraping.lib.throttling.model.ScrapedDataType;

import java.util.Collections;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(ScrapedDataType.INZERAT, new HtmlUnitDriversFactory());

        final HtmlUnitParsingStrategy strategy1 = new HtmlUnitParsingStrategyIterableByFullXPath(ParsedFieldId.EVENT_LINK,
//                "/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]",
                "/html/body/div[3]/div[3]/div[5]/div[1]/div[4]/ul/li[1]",
                List.of(new HtmlUnitParsingStrategyIteratedChildByFullXPath(ParsedFieldId.FIELD_VALUE,
//                        "/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/div/div[1]/span[1]"
                                "/html/body/div[3]/div[3]/div[5]/div[1]/div[4]/ul/li[1]/a/span[2]",
                                List.of(new HtmlUnitParsingStrategyIterableByFullXPath(ParsedFieldId.FIELD_VALUE,
//                        "/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/div/div[1]/span[1]"
                                        "/html/body/div[3]/div[3]/div[5]/div[1]/div[4]/ul/li[2]/ul/li[1]",
                                        List.of(new HtmlUnitParsingStrategyIteratedChildByFullXPath(ParsedFieldId.FIELD_VALUE,
                                                "/html/body/div[3]/div[3]/div[5]/div[1]/div[4]/ul/li[2]/ul/li[1]/a/span[2]",
                                                List.of())
                                        )
                                ))
                        )
                )
        );
        final List<HtmlUnitParsingStrategy> strategies1 = List.of(strategy1);
        final HtmlUnitSiteParser parser1 = new HtmlUnitSiteParser(driverManager, strategies1);

        final List<HtmlUnitParsingStrategy> strategies2 = List.of(strategy1);
        final HtmlUnitSiteParser parser2 = new HtmlUnitSiteParser(driverManager, strategies2);
        final ScrapingStage stage2 = new ScrapingStage(parser2, ParsedFieldId.EVENT_LINK, href -> "https://www.ifortuna.cz/" + href, Collections.emptyList());


        final ScrapingStage stage1 = new ScrapingStage(parser1, null, s -> s, List.of(stage2));

        final EntryPoint entryPoint = new EntryPoint(
//                "https://www.ifortuna.cz/",
                "https://en.wikipedia.org/wiki/Gross_domestic_product",
                stage1);


        final Scraper scraper = new Scraper();

        scraper.scrape(entryPoint);

    }

    public enum ParsedFieldId {
        EVENT_LINK,
        FIELD_VALUE
    }

}
