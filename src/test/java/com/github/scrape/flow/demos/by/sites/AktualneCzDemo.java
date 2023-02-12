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

import com.github.scrape.flow.data.publishing.ScrapedDataListener;
import com.github.scrape.flow.scraping.Scraping;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnit;
import com.github.scrape.flow.utils.JsonUtils;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnit.*;
import static com.github.scrape.flow.scraping.htmlunit.HtmlUnit.Get;
import static com.github.scrape.flow.scraping.htmlunit.HtmlUnit.Parse;

@Log4j2
public class AktualneCzDemo {

//    @Ignore
    @Test
    public void start() throws InterruptedException {

        final Scraping articlesScraping = new Scraping(5, TimeUnit.SECONDS)
                .setSequence(
                        Do.navigateTo("https://zpravy.aktualne.cz/zahranici/")
                                .next(Do.peek(e -> log.info("peek ... now")))
                                .nextBranchExclusively(Get.descendants().byAttrRegex("data-ga4-type", "article|online")
                                        .addCollector(Article::new, Article.class, new ArticleListener())
                                        .nextBranch(Get.descendants().byClassRegex("small-box__title|section-opener__title").stepName("step-1")
                                                .next(Parse.textContent().collectValue(Article::setHeadline, Article.class))
                                        )
                                        .nextBranch(Get.descendants().byClassRegex("small-box__desc|section-opener__desc")
                                                .next(Parse.textContent().collectValue(Article::setDescription, Article.class))
                                        )
                                )
                                .next(Do.peek(e -> log.info("peek ... later")))

                );


        articlesScraping.start(Duration.ofMinutes(2));
        Thread.sleep(1000); // let logging finish
    }

    @Data
    private static class Article {
        private String headline;
        private String description;
    }

    @Log4j2
    private static class ArticleListener implements ScrapedDataListener<Article> {
        @Override
        public void onScrapedData(Article data) {
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }


}
