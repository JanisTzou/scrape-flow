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
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitGetDescendantsByCssSelector;
import com.github.scrape.flow.utils.JsonUtils;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnit.*;

public class CsfdCzDemo {

    @Ignore
    @Test
    public void start() throws InterruptedException {

        final Scraping scraping = new Scraping(3, TimeUnit.SECONDS)
                .setSequence(
                        Do.navigateTo("https://www.csfd.cz/novinky/")
                                .nextBranch(Get.descendantsBySelector("div.news-page")
                                        .first() // the first article list out of two
                                        .addCollector(Category::new, Category.class, new CategoryListener())
                                        .nextBranch(Parse.textContent()
                                                .collectValue(Category::setName, Category.class)
                                        )
                                        .nextBranch(Get.parent()
                                                .nextBranch(getArticles())
                                        )
                                )
                );

        start(scraping);
    }

    private HtmlUnitGetDescendantsByCssSelector getArticles() {
        return Get.descendantsBySelector("div.box-content")
                .nextBranch(Get.descendants().byTag("article")
                        .addCollector(Article::new, Article.class, new ArticleListener())
                        .collectValue(Article::setCategory, Article.class, Category.class)
                        .nextBranch(Get.descendants().byTag("figure")
                                .nextBranch(Get.children().first()
                                        .nextBranch(Parse.hRef(href -> "https:" + href)
                                                .nextBranch(Do.downloadImage()
                                                        .collectValue(Article::setImage, Article.class)
                                                )
                                        )
                                )
                        )
                        .nextBranch(Get.descendantsBySelector("header.article-header")
                                .nextBranch(Get.children().first()
                                        .nextBranch(Parse.textContent()
                                                .setValueMapper(str -> str.replace("\t", "").replace("\n", " "))
                                                .collectValue(Article::setTitle, Article.class)
                                        )
                                )

                        ));
    }

    private void start(Scraping productsScraping) throws InterruptedException {
        productsScraping.start(Duration.ofMinutes(5));
        Thread.sleep(2000); // let logging finish ...
    }


    @Data
    public static class Category {
        private volatile String name;
    }


    @Data
    public static class Article {
        private volatile String title;
        private volatile Category category;
        private volatile BufferedImage image;
    }

    @Data
    public static class Movie {
        private volatile String title;
    }


    @Log4j2
    public static class CategoryListener implements ScrapedDataListener<Category> {
        @Override
        public void onScrapedData(Category data) {
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

    @Log4j2
    public static class ArticleListener implements ScrapedDataListener<Article> {
        @Override
        public void onScrapedData(Article data) {
            data.setImage(null); //  throws exceptions ...
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

    @Log4j2
    public static class MovieListener implements ScrapedDataListener<Movie> {
        @Override
        public void onScrapedData(Movie data) {
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

}
