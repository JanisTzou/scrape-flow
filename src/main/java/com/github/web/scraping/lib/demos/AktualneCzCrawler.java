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

import static com.github.web.scraping.lib.demos.AktualneCzCrawler.Identifiers.*;

public class AktualneCzCrawler {

    public void start() {

        // TODO any way for these to be accessible globally? So they do not need to be specified explicitly in every stage definition?
        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        // TODO the parsing/scraping steps should be better named so it is clear what action they perform ... it might not be parsing exacly but also actions like button clicks etc ...
        //  maybe it is ok to have a "parsing ste" that is not exacly parsing enything but performing an action ... it's just something that needs to be performed to do the actual parsing ...

        final GetElementByAttribute.Builder getArticleElements = GetElementByAttribute.builder("data-ga4-type", "article");
        final GetElementByAttribute.Builder getArticleHeadlineElem = GetElementByAttribute.builder("data-vr-headline");
        final GetElementByCssClass.Builder getArticleDescElem1 = GetElementByCssClass.builder("section-opener__desc");
        final GetElementByCssClass.Builder getArticleDescElem2 = GetElementByCssClass.builder("small-box__desc");

        final CrawlingStage.Builder articleListStage = CrawlingStage.builder()
                .setParser(HtmlUnitSiteParser.builder(driverManager)
                        .addParsingSequence(getArticleElements
                                .then(getArticleHeadlineElem
                                        .then(ParseElementTextContent.builder().setId(ARTICLE_HEADLINE).build())
                                        .build()
                                )
                                .then(getArticleDescElem1
                                        .then(ParseElementTextContent.builder().setId(ARTICLE_DESC).build())
                                        .build()
                                )
                                .then(getArticleDescElem2
                                        .then(ParseElementTextContent.builder().setId(ARTICLE_DESC).build())
                                        .build()
                                )
                                .build()
                        )
                        .build()
                );

        final CrawlingStage allCrawling = articleListStage.build();

        // TODO maybe the entry url should be part of the first scraping stage? And we can have something like "FirstScrapingStage) ... or maybe entry point abstraction is good enough ?
        final EntryPoint entryPoint = new EntryPoint("https://zpravy.aktualne.cz/zahranici/", allCrawling);

        final Crawler crawler = new Crawler();

        crawler.scrape(entryPoint);

    }

    public enum Identifiers {
        ARTICLE_HEADLINE,
        ARTICLE_DESC
    }

}
