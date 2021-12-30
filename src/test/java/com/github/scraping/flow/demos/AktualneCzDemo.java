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

package com.github.scraping.flow.demos;

import com.github.scraping.flow.drivers.HtmlUnitDriverManager;
import com.github.scraping.flow.drivers.HtmlUnitDriversFactory;
import com.github.scraping.flow.scraping.EntryPoint;
import com.github.scraping.flow.scraping.Scraper;
import com.github.scraping.flow.scraping.Scraping;
import com.github.scraping.flow.scraping.htmlunit.GetDescendants;
import com.github.scraping.flow.scraping.htmlunit.HtmlUnitSiteParser;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.github.scraping.flow.scraping.htmlunit.HtmlUnit.Get;
import static com.github.scraping.flow.scraping.htmlunit.HtmlUnit.Parse;

public class AktualneCzDemo {

    @Test
    public void start() throws InterruptedException {

        // TODO any way for these to be accessible globally? So they do not need to be specified explicitly in every stage definition?
        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        final GetDescendants getArticleElements = Get.descendants().byAttr("data-ga4-type", "article");
        final GetDescendants getArticleHeadlineElem = Get.descendants().byAttr("data-vr-headline");
        final GetDescendants getArticleDescElem1 = Get.descendants().byClass("section-opener__desc");
        final GetDescendants getArticleDescElem2 = Get.descendants().byClass("small-box__desc");

        final Scraping articlesScraping = new Scraping(new HtmlUnitSiteParser(driverManager), 5, TimeUnit.SECONDS)
                .setSequence(
                        getArticleElements
                                .next(getArticleHeadlineElem.stepName("step-1")
                                        .next(Parse.textContent())
                                )
                                .next(getArticleHeadlineElem.stepName("step-2")
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
        scraper.awaitCompletion(Duration.ofMinutes(2));
        Thread.sleep(1000); // let logging finish
    }

}
