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

import static com.github.web.scraping.lib.Main.Identifiers.*;
import static com.github.web.scraping.lib.dom.data.parsing.HtmlUnitParsingStrategy.*;

public class Main {

    public static void main(String[] args) {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

//        final HtmlUnitParsingStrategy strategy1 = new HtmlUnitParsingStrategyIterableByFullXPath(ParsedFieldId.EVENT_LINK,
////                "/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]",
//                "/html/body/div[3]/div[3]/div[5]/div[1]/div[4]/ul/li[1]",
//                List.of(new HtmlUnitParsingStrategyIteratedChildByFullXPath(ParsedFieldId.FIELD_VALUE,
////                        "/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/div/div[1]/span[1]"
//                                "/html/body/div[3]/div[3]/div[5]/div[1]/div[4]/ul/li[1]/a/span[2]",
//                                List.of(new HtmlUnitParsingStrategyIterableByFullXPath(ParsedFieldId.FIELD_VALUE,
////                        "/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/div/div[1]/span[1]"
//                                        "/html/body/div[3]/div[3]/div[5]/div[1]/div[4]/ul/li[2]/ul/li[1]",
//                                        List.of(new HtmlUnitParsingStrategyIteratedChildByFullXPath(ParsedFieldId.FIELD_VALUE,
//                                                "/html/body/div[3]/div[3]/div[5]/div[1]/div[4]/ul/li[2]/ul/li[1]/a/span[2]",
//                                                List.of())
//                                        )
//                                ))
//                        )
//                )
//        );


//        final HtmlUnitParsingStrategy strategy1 = new HtmlUnitParsingStrategyIterableByFullXPath(Identifiers.EVENT_LINK,
////                "/html/body/div[3]/div[2]/div/div/main/section[1]/div/div[2]/div/div/div/div[1]/div[2]/div[1]/div/div/article/div/a"
////                "/html/body/div[3]/div[2]/div/div/main/section[1]/div/div[2]/div/div/div/div[1]/div[2]/div[1]/div/div/article/div/a/div/h2"
//                "/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]",
//                List.of(new HtmlUnitParsingStrategyIteratedChildByFullXPath(Identifiers.FIELD_VALUE, "/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/div/div[1]/span[1]", List.of()))
//        );


        final HtmlUnitParsingStrategyIterableByFullXPath.Builder eventsListStrategy = iterableByXPath("/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]");

        final HtmlUnitParsingStrategyIteratedChildByFullXPath eventTitleStrategy = iteratedChildByXPath("/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/div/div[1]/span[1]")
                .setIdentifier(EVENT_TITLE)
                .build();

        final HtmlUnitParsingStrategyIteratedChildByFullXPath eventDetailLinkStrategy = iteratedChildByXPath("/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/a")
                .setIdentifier(EVENT_LINK)
                .build();

        final HtmlUnitParsingStrategyIteratedChildByFullXPath eventDateLinkStrategy = iteratedChildByXPath("/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[9]/span")
                .setIdentifier(EVENT_DATE)
                .build();

        eventsListStrategy.addNextStrategy(eventTitleStrategy);

        final ScrapingStage.Builder eventsListStage = ScrapingStage.builder()
                .setParser(HtmlUnitSiteParser.builder(driverManager)
                        .addStrategy(eventsListStrategy
                                .addNextStrategy(eventDetailLinkStrategy)
                                .addNextStrategy(eventDateLinkStrategy)
                                .build()
                        )
                        .build());

        final HtmlUnitParsingStrategyByFullXPath eventOddsStrategy = byXPath("/html/body/div[1]/div/div[2]/div[2]/div/section/div/div[2]/table/tbody/tr/td[2]/a/span")
                .setIdentifier(HOME_ODDS)
                .build();

        final ScrapingStage eventOddsStage = ScrapingStage.builder()
                .setParser(HtmlUnitSiteParser.builder(driverManager)
                        .addStrategy(eventOddsStrategy)
                        .build())
                .setHrefKey(EVENT_LINK)
                .setHrefToURLMapper(href -> "https://www.ifortuna.cz/" + href)
                .build();

        final ScrapingStage stage = eventsListStage
                .addNextStage(eventOddsStage)
                .build();

        // TODO maybe the entry url should be part of the first scraping stage? And we can have something like "FirstScrapingStage) ... or maybe entry point abstraction is good enough ?
        final EntryPoint entryPoint = new EntryPoint(
                "https://www.ifortuna.cz/",
//                "https://en.wikipedia.org/wiki/Gross_domestic_product",
                stage
        );


        final Scraper scraper = new Scraper();

        scraper.scrape(entryPoint);

    }

    public enum Identifiers {
        EVENT_LINK,
        EVENT_DATE,
        EVENT_TITLE,
        HOME_ODDS
    }

}
