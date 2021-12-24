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

import com.github.web.scraping.lib.scraping.Scraper;
import com.github.web.scraping.lib.scraping.Scraping;
import com.github.web.scraping.lib.scraping.EntryPoint;
import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;
import com.github.web.scraping.lib.scraping.htmlunit.*;
import org.junit.Test;

public class AktualneCzDemo {

    @Test
    public void start() {

        // TODO any way for these to be accessible globally? So they do not need to be specified explicitly in every stage definition?
        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        final GetElementsByAttribute getArticleElements = GetElements.Descendants.ByAttribute.nameAndValue("data-ga4-type", "article");
        final GetElementsByAttribute getArticleHeadlineElem = GetElements.Descendants.ByAttribute.name("data-vr-headline");
        final GetElementsByCssClass getArticleDescElem1 = GetElements.Descendants.ByCss.byClassName("section-opener__desc");
        final GetElementsByCssClass getArticleDescElem2 = GetElements.Descendants.ByCss.byClassName("small-box__desc");

        final Scraping articlesScraping = new Scraping(new HtmlUnitSiteParser(driverManager), 5)
                .setScrapingSequence(getArticleElements
                        .next(getArticleHeadlineElem
                                .next(Parse.textContent())
                        )
                        .next(getArticleDescElem1
                                .next(Parse.textContent())
                        )
                        .next(getArticleDescElem2
                                .next(Parse.textContent())
                        )
                );


        final EntryPoint entryPoint = new EntryPoint("https://zpravy.aktualne.cz/zahranici/", articlesScraping);

        final Scraper scraper = new Scraper();

        scraper.scrape(entryPoint);

    }

    public enum Identifiers {
    }

}
