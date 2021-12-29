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

import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;
import com.github.web.scraping.lib.parallelism.ParsedDataListener;
import com.github.web.scraping.lib.scraping.EntryPoint;
import com.github.web.scraping.lib.scraping.Scraper;
import com.github.web.scraping.lib.scraping.Scraping;
import com.github.web.scraping.lib.scraping.htmlunit.GetDescendantsByCssSelector;
import com.github.web.scraping.lib.scraping.htmlunit.HtmlUnitSiteParser;
import com.github.web.scraping.lib.utils.JsonUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.time.Duration;

import static com.github.web.scraping.lib.scraping.htmlunit.HtmlUnit.*;

public class CsfdCzDemo {

    @Test
    public void start() throws InterruptedException {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        final Scraping productsScraping = new Scraping(new HtmlUnitSiteParser(driverManager), 3)
                .setSequence(
                        Get.descendantsBySelector("header.box-header")
                                .first() // the first article list out of two
                                .addCollector(Category::new, Category.class, new CategoryListener())
                                .next(Parse.textContent()
                                        .collectOne(Category::setName, Category.class)
                                )
                                .next(Get.parent()
                                        .next(getArticles()
                                        )
                                )

                );

        start(productsScraping);
    }

    private GetDescendantsByCssSelector getArticles() {
        return Get.descendantsBySelector("div.box-content")
                .next(Get.descendants()
                        .byTag("article")
                        .addCollector(Article::new, Article.class, new ArticleListener())
                        .collectOne(Article::setCategory, Article.class, Category.class)
                        .next(Get.descendants().byTag("figure")
                                .next(Get.children().first()
                                        .next(Parse.hRef(href -> "https:" + href)
                                                .next(Do.downloadImage()
                                                        .collectOne(Article::setImage, Article.class)
                                                )
                                        )
                                )
                        )
                        .next(Get.descendantsBySelector("header.article-header")
                                .next(Get.children().first()
                                        .next(Parse.textContent()
                                                .setValueConversion(str -> str.replace("\t", "").replace("\n", " "))
                                                .collectOne(Article::setTitle, Article.class)
                                        )
                                )

                        ));
    }

    private void start(Scraping productsScraping) throws InterruptedException {
        final EntryPoint entryPoint = new EntryPoint("https://www.csfd.cz/novinky/", productsScraping);
        final Scraper scraper = new Scraper();
        scraper.scrape(entryPoint);

        scraper.awaitCompletion(Duration.ofMinutes(5));
        Thread.sleep(2000); // let logging finish ...
    }


    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Category {
        private volatile String name;
    }


    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Article {
        private volatile String title;
        private volatile Category category;
        private volatile BufferedImage image;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Movie {
        private volatile String title;
    }


    @Log4j2
    public static class CategoryListener implements ParsedDataListener<Category> {
        @Override
        public void onParsingFinished(Category data) {
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

    @Log4j2
    public static class ArticleListener implements ParsedDataListener<Article> {
        @Override
        public void onParsingFinished(Article data) {
            data.setImage(null); //  throws exceptions ...
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

    @Log4j2
    public static class MovieListener implements ParsedDataListener<Movie> {
        @Override
        public void onParsingFinished(Movie data) {
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

}
