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
import com.github.web.scraping.lib.Crawling;
import com.github.web.scraping.lib.EntryPoint;
import com.github.web.scraping.lib.dom.data.parsing.steps.HtmlUnitSiteParser;
import com.github.web.scraping.lib.dom.data.parsing.steps.GetElementsByAttribute;
import com.github.web.scraping.lib.dom.data.parsing.steps.GetElementsByCssClass;
import com.github.web.scraping.lib.dom.data.parsing.steps.ParseElementText;
import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;
import org.junit.Test;

public class AktualneCzDemo {

    @Test
    public void start() {

        // TODO any way for these to be accessible globally? So they do not need to be specified explicitly in every stage definition?
        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        // TODO the parsing/scraping steps should be better named so it is clear what action they perform ... it might not be parsing exacly but also actions like button clicks etc ...
        //  maybe it is ok to have a "parsing ste" that is not exacly parsing enything but performing an action ... it's just something that needs to be performed to do the actual parsing ...

        final GetElementsByAttribute getArticleElements = GetElementsByAttribute.instance("data-ga4-type", "article");
        final GetElementsByAttribute getArticleHeadlineElem = GetElementsByAttribute.instance("data-vr-headline");
        final GetElementsByCssClass getArticleDescElem1 = GetElementsByCssClass.instance("section-opener__desc");
        final GetElementsByCssClass getArticleDescElem2 = GetElementsByCssClass.instance("small-box__desc");

        final Crawling articlesCrawling = new Crawling()
                .setSiteParser(new HtmlUnitSiteParser(driverManager)
                        .setParsingSequence(getArticleElements
                                .then(getArticleHeadlineElem
                                        .then(new ParseElementText())
                                )
                                .then(getArticleDescElem1
                                        .then(new ParseElementText())

                                )
                                .then(getArticleDescElem2
                                        .then(new ParseElementText())
                                )
                        )
                );


        // TODO maybe the entry url should be part of the first scraping stage? And we can have something like "FirstScrapingStage) ... or maybe entry point abstraction is good enough ?
        final EntryPoint entryPoint = new EntryPoint("https://zpravy.aktualne.cz/zahranici/", articlesCrawling);

        final Crawler crawler = new Crawler();

        crawler.scrape(entryPoint);

    }

    public enum Identifiers {
    }

}
