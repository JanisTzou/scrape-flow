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

import com.github.web.scraping.lib.Scraper;
import com.github.web.scraping.lib.Scraping;
import com.github.web.scraping.lib.EntryPoint;
import com.github.web.scraping.lib.dom.data.parsing.steps.*;
import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;
import org.junit.Test;

public class IFortunaCzDemo {

    @Test
    public void start() {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        final GetListedElementsByFirstElementXPath getEventsListElements = GetListedElementsByFirstElementXPath.instance("/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]");
        final GetListedElementByFirstElementXPath getEventDetailLinkElem = GetListedElementByFirstElementXPath.instance("/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/a");
        final GetListedElementByFirstElementXPath getEventTitleElem = GetListedElementByFirstElementXPath.instance("/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/div/div[1]/span[1]");
        final GetListedElementByFirstElementXPath getEventDateElem = GetListedElementByFirstElementXPath.instance("/html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[9]/span");

        // get text -> search step 1, 2, 3 ...
        // search step 1, 2, 3 ... -> get text ...
        // search step 1, 2, 3 by dynamic text value ... -> get text ...
        // TODO somehow pagination must be involved as well ...

        // TODO step examples:
        //  search x execute x paginate x click x wait ...

        final Scraping matchesScraping = new Scraping(new HtmlUnitSiteParser(driverManager), 5)
                .setScrapingSequence(getEventsListElements
                        .next(getEventDetailLinkElem  // TODO perhaps we can express it better that the next step is going for the children ?
                                .next(Parse.hRef())
                        )
                        .next(getEventTitleElem
                                .next(Parse.textContent())
                        )
                        .next(getEventDateElem
                                .next(Parse.textContent())
                        )
                );

        final GetElementsByXPath getEventHomeOddsElem = GetElements.ByXPath.xPath("/html/body/div[1]/div/div[2]/div[2]/div/section/div/div[2]/table/tbody/tr/td[2]/a/span");

//        final Crawling eventDetailOddsStage = new Crawling()
//                .setParser(new HtmlUnitSiteParser(driverManager)
//                        .setScrapingSequence(getEventHomeOddsElem
//                                .then(new ParseElementText())
//                        )
//                        )
//               ;


        // TODO maybe the entry url should be part of the first scraping stage? And we can have something like "FirstScrapingStage) ... or maybe entry point abstraction is good enough ?
        final EntryPoint entryPoint = new EntryPoint("https://www.ifortuna.cz/", matchesScraping);

        final Scraper scraper = new Scraper();

        scraper.scrape(entryPoint);

    }

    public enum Identifiers {
        EVENT_LINK,
    }
}
