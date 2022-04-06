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

package com.github.scrape.flow.demos.by.sites;

import com.github.scrape.flow.scraping.Scraping;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitFlow;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitGetDescendants;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnitFlow.Get;
import static com.github.scrape.flow.scraping.htmlunit.HtmlUnitFlow.Parse;

public class AktualneCzDemo {

    @Ignore
    @Test
    public void start() throws InterruptedException {

        final HtmlUnitGetDescendants getArticleElements = Get.descendants().byAttr("data-ga4-type", "article");
        final HtmlUnitGetDescendants getArticleHeadlineElem = Get.descendants().byAttr("data-vr-headline");
        final HtmlUnitGetDescendants getArticleDescElem1 = Get.descendants().byClass("section-opener__desc");
        final HtmlUnitGetDescendants getArticleDescElem2 = Get.descendants().byClass("small-box__desc");

        final Scraping articlesScraping = new Scraping(5, TimeUnit.SECONDS)
                .setSequence(
                        HtmlUnitFlow.Do.navigateToUrl("https://zpravy.aktualne.cz/zahranici/")
                                .next(getArticleElements
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
                                        ))

                );


        articlesScraping.start(Duration.ofMinutes(2));
        Thread.sleep(1000); // let logging finish
    }

}
