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

package com.github.scrape.flow.demos;

import com.github.scrape.flow.drivers.HtmlUnitDriverManager;
import com.github.scrape.flow.drivers.HtmlUnitDriversFactory;
import com.github.scrape.flow.parallelism.ScrapedDataListener;
import com.github.scrape.flow.scraping.EntryPoint;
import com.github.scrape.flow.scraping.Scraper;
import com.github.scrape.flow.scraping.Scraping;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitSiteParser;
import com.github.scrape.flow.scraping.htmlunit.NavigateToParsedLink;
import com.github.scrape.flow.scraping.htmlunit.StepBlock;
import com.github.scrape.flow.utils.JsonUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnit.*;

@Log4j2
public class BbcComDemo {

    public static final String HTTPS_WWW_BBC_COM = "https://www.bbc.com";

    @Test
    public void demo() throws InterruptedException {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());
        final HtmlUnitSiteParser parser = new HtmlUnitSiteParser(driverManager);

        final Scraping scraping = new Scraping(parser, 1, TimeUnit.SECONDS)
                .setSequence(
                        Get.descendants().byAttr("aria-label", "World")
                                .first()
                                .next(Get.descendants().byTag("ul")
                                        .first()
                                        .next(Get.descendants().byTag("li")
                                                .addCollector(Section::new, Section.class, new SectionListener())
                                                .next(Get.descendants().byTag("a")
                                                        .next(Parse.textContent()
                                                                .collectOne(Section::setName, Section.class)
                                                        )
                                                        .next(Parse.hRef(href -> HTTPS_WWW_BBC_COM + href)
                                                                .nextNavigate(goToEachSection(parser))
                                                        )
                                                )
                                        )
                                )
                );

        start(scraping);
    }

    private NavigateToParsedLink goToEachSection(HtmlUnitSiteParser parser) {
        return Do.navigateToParsedLink(parser)
                .next(Get.descendants().byAttr("id", "featured-contents")
                        .next(Get.siblings().next()
                                .next(Get.descendantsBySelector("div.gs-c-promo").stepName("listed-article")
                                        .addCollector(Promo::new, Promo.class)
                                        .addCollector(Article::new, Article.class, new ArticleListener())
                                        .collectOne(Article::setRegion, Article.class, Section.class)
                                        .collectOne(Article::setPromo, Article.class, Promo.class)
                                        .next(Get.descendantsBySelector("p.gs-c-promo-summary")
                                                .next(Parse.textContent()
                                                        .collectOne(Promo::setSummary, Promo.class)
                                                )
                                        )
                                        .next(Get.descendantsBySelector("a.gs-c-promo-heading")
                                                .next(Get.descendants().byTag("h3")
                                                        .next(Parse.textContent()
                                                                .collectOne(Promo::setHeading, Promo.class)
                                                        )
                                                )
                                                .next(Parse.hRef(href -> href.contains("https") ? href : HTTPS_WWW_BBC_COM + href)
                                                        .collectOne(Article::setUrl, Article.class)
                                                        .nextNavigate(toArticles(parser))
                                                )

                                        )
                                )
                        )
                )
                ;
    }


    private NavigateToParsedLink toArticles(HtmlUnitSiteParser parser) {
        return Do.navigateToParsedLink(parser)
                .next(Get.descendants().byTag("article")
                        .nextExclusively(Get.descendants().byTextContent("Sport Africa") // category must be parsed before following steps can proceed -> exclusive call
                                .next(Parse.textContent()
                                        .setValueConversion(s -> "Sport")
                                        .collectOne(Article::setCategory, Article.class)
                                )
                        )
                        .nextIf(this::isSportArticle, Article.class,
                                parseSportArticle()
                        )
                        .nextIf(this::isNotSportArticle, Article.class,
                                parseNonSportArticle()
                        )

                );
    }

    private StepBlock parseSportArticle() {
        return Flow.asBlock()
                .next(Get.descendants().byAttr("id", "page")
                        .next(Parse.textContent()
                                .collectOne(Article::setTitle, Article.class)
                        )
                )
                .next(Get.descendants().byTag("p")
                        .next(Parse.textContent()
                                .collectMany((Article a, String p) -> a.getParagraphs().add(p), Article.class)
                        )
                );
    }

    private StepBlock parseNonSportArticle() {
        return Flow.asBlock()
                .next(Get.descendants().byAttr("id", "main-heading")
                        .next(Parse.textContent()
                                .collectOne(Article::setTitle, Article.class)
                        )
                )
                .next(Get.descendants().byAttr("data-component", "text-block")
                        .next(Parse.textContent()
                                .collectMany((Article a, String p) -> a.getParagraphs().add(p), Article.class)
                        )
                )
                .next(Get.descendants().byAttr("data-component", "unordered-list-block")
                        .next(Get.descendants().byTag("a")
                                .next(Parse.hRef()
                                        .collectMany((Article a, String p) -> a.getLinks().add(p), Article.class)
                                )
                        )
                );
    }

    private boolean isSportArticle(Article a) {
        return a.getCategory() != null && a.getCategory().equalsIgnoreCase("Sport");
    }

    private boolean isNotSportArticle(Article a) {
        return !isSportArticle(a);
    }


    private void start(Scraping articlesScraping) throws InterruptedException {
        final EntryPoint entryPoint = new EntryPoint("https://www.bbc.com/news/world", articlesScraping);
        final Scraper scraper = new Scraper();
        scraper.start(entryPoint);
        scraper.awaitCompletion(Duration.ofMinutes(2));
        Thread.sleep(1000); // let logging finish
    }


    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class Section {
        private String name;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class Promo {
        private String heading;
        private String summary;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Article {
        private String url;
        private Section region;
        private String category;
        private Promo promo;
        private String title;
        private List<String> paragraphs = new ArrayList<>();
        private List<String> links = new ArrayList<>();
    }


    public static class ArticleListener implements ScrapedDataListener<Article> {
        @Override
        public void onParsedData(Article data) {
            log.info("\n" + JsonUtils.write(data).orElse("JSON ERROR"));
        }
    }

    public static class SectionListener implements ScrapedDataListener<Section> {
        @Override
        public void onParsedData(Section data) {
            log.info("\n" + JsonUtils.write(data).orElse("JSON ERROR"));
        }
    }


}
