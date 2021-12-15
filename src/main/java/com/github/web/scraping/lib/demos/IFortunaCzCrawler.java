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

package com.github.web.scraping.lib.demos;

import com.github.web.scraping.lib.Crawler;
import com.github.web.scraping.lib.CrawlingStage;
import com.github.web.scraping.lib.EntryPoint;
import com.github.web.scraping.lib.dom.data.parsing.HtmlUnitSiteParser;
import com.github.web.scraping.lib.dom.data.parsing.steps.*;
import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;

import static com.github.web.scraping.lib.demos.IFortunaCzCrawler.Identifiers.*;

public class IFortunaCzCrawler {

    public void start() {

        // TODO any way for these to be accessible globally? So they do not need to be specified explicitly in every stage definition?
        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        // TODO the parsing/scraping steps should be better named so it is clear what action they perform ... it might not be parsing exacly but also actions like button clicks etc ...
        //  maybe it is ok to have a "parsing ste" that is not exacly parsing enything but performing an action ... it's just something that needs to be performed to do the actual parsing ...

        final GetListedElementsByFirstElementXPath.Builder getEventsListElements = GetListedElementsByFirstElementXPath.builder("/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]");
        final GetListedElementByFirstElementXPath.Builder getEventDetailLinkElem = GetListedElementByFirstElementXPath.builder("/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/a");
        final GetListedElementByFirstElementXPath.Builder getEventTitleElem = GetListedElementByFirstElementXPath.builder("/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/div/div[1]/span[1]");
        final GetListedElementByFirstElementXPath.Builder getEventDateElem = GetListedElementByFirstElementXPath.builder("/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[9]/span");

        // get text -> search step 1, 2, 3 ...
        // search step 1, 2, 3 ... -> get text ...
        // search step 1, 2, 3 by dynamic text value ... -> get text ...
        // TODO somehow pagination must be involved as well ...

        // TODO step examples:
        //  search x execute x paginate x click x wait ...

        final CrawlingStage.Builder eventsListStage = CrawlingStage.builder()
                .setParser(HtmlUnitSiteParser.builder(driverManager)
                        .addParsingSequence(getEventsListElements
                                .then(getEventDetailLinkElem  // TODO perhaps we can express it better that the next step is going for the children ?
                                        .then(ParseElementHRef.builder(EVENT_LINK).build())
                                        .build()
                                )
                                .then(getEventTitleElem
                                        .then(ParseElementText.builder(EVENT_TITLE).build())
                                        .build()
                                )
                                .then(getEventDateElem
                                        .then(ParseElementText.builder(EVENT_DATE).build())
                                        .build()
                                )
                                .build()
                        )
                        .build());

        final GetElementsByXPath.Builder getEventHomeOddsElem = GetElementsByXPath.builder("/html/body/div[1]/div/div[2]/div[2]/div/section/div/div[2]/table/tbody/tr/td[2]/a/span");

        final CrawlingStage eventDetailOddsStage = CrawlingStage.builder()
                .setParser(HtmlUnitSiteParser.builder(driverManager)
                        .addParsingSequence(getEventHomeOddsElem
                                .then(ParseElementText.builder(HOME_ODDS).build())
                                .build()
                        )
                        .build())
                .setReferenceForParsedHrefToCrawl(EVENT_LINK, href -> "https://www.ifortuna.cz/" + href)
                .build();

        final CrawlingStage allCrawling = eventsListStage
                .addNextStage(eventDetailOddsStage)
                .build();

        // TODO maybe the entry url should be part of the first scraping stage? And we can have something like "FirstScrapingStage) ... or maybe entry point abstraction is good enough ?
        final EntryPoint entryPoint = new EntryPoint("https://www.ifortuna.cz/", allCrawling);

        final Crawler crawler = new Crawler();

        crawler.scrape(entryPoint);

    }

    public enum Identifiers {
        EVENT_LINK,
        EVENT_DATE,
        EVENT_TITLE,
        HOME_ODDS
    }
}
