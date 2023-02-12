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
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitNavigateToParsedLink;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitStepBlock;
import com.github.scrape.flow.utils.JsonUtils;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnit.*;

@Log4j2
public class BbcComDemo {

    public static final String HTTPS_WWW_BBC_COM = "https://www.bbc.com";

    //    @Ignore
    @Test
    public void demo() throws InterruptedException {

        final Scraping scraping = new Scraping(10, TimeUnit.SECONDS);
        scraping.getDebugOptions().setLogFoundElementsCount(true);

        scraping.setSequence(Do.navigateTo("https://www.bbc.com/news/world")
                .next(Get.descendants().byAttr("aria-label", "World").first())
                .next(Get.descendants().byTag("ul").first())
                .next(Get.descendants().byTag("li"))
                .addCollector(Section::new, Section.class, new SectionListener())
                .next(Get.descendants().byTag("a")
                        .nextBranch(Parse.textContent()
                                .collectValue(Section::setName, Section.class)
                        )
                        .nextBranch(Parse.hRef(href -> HTTPS_WWW_BBC_COM + href)
                                .next(Do.navigateToParsedLink())
                                .next(Get.descendants().byAttr("id", "featured-contents"))
                                .next(Get.siblings().next())
                                .next(toListedArticles()
                                )
                        )
                )
        );
        start(scraping);
    }

    private HtmlUnitGetDescendantsByCssSelector toListedArticles() {
        return Get.descendantsBySelector("div.gs-c-promo").stepName("listed-article")
                .addCollector(Promo::new, Promo.class)
                .addCollector(Article::new, Article.class, new ArticleListener())
                .collectValue(Article::setRegion, Article.class, Section.class)
                .collectValue(Article::setPromo, Article.class, Promo.class)
                .nextBranch(Get.descendantsBySelector("p.gs-c-promo-summary")
                        .next(Parse.textContent())
                        .collectValue(Promo::setSummary, Promo.class)
                )
                .nextBranch(Get.descendantsBySelector("a.gs-c-promo-heading")
                        .nextBranch(Get.descendants().byTag("h3")
                                .next(Parse.textContent())
                                .collectValue(Promo::setHeading, Promo.class)
                        )
                        .nextBranch(Parse.hRef(href1 -> href1.contains("https") ? href1 : HTTPS_WWW_BBC_COM + href1)
                                .collectValue(Article::setUrl, Article.class)
                                .nextBranch(toArticles())
                        )
                );
    }


    private HtmlUnitNavigateToParsedLink toArticles() {
        return Do.navigateToParsedLink()
                .nextBranch(Get.descendants().byTag("article")
                        .nextBranchExclusively(Get.descendants().byTextContent("Sport Africa") // category must be parsed before following steps can proceed -> exclusive call
                                .next(Parse.textContent())
                                .setValueMapper(s -> "Sport")
                                .collectValue(Article::setCategory, Article.class)
                        )
                        .nextBranchIf(this::isSportArticle, Article.class,
                                parseSportArticle()
                        )
                        .nextBranchIf(this::isNotSportArticle, Article.class,
                                parseNonSportArticle()
                        )
                );
    }

    private HtmlUnitStepBlock parseSportArticle() {
        return Flow.asBlock()
                .nextBranch(Get.descendants().byAttr("id", "page")
                        .next(Parse.textContent())
                        .collectValue(Article::setTitle, Article.class)
                )
                .nextBranch(Get.descendants().byTag("p")
                        .next(Parse.textContent())
                        .collectValues((Article a, String p) -> a.getParagraphs().add(p), Article.class)
                );
    }

    private HtmlUnitStepBlock parseNonSportArticle() {
        return Flow.asBlock()
                .nextBranch(Get.descendants().byAttr("id", "main-heading")
                        .next(Parse.textContent())
                        .collectValue(Article::setTitle, Article.class)
                )
                .nextBranch(Get.descendants().byAttr("data-component", "text-block")
                        .next(Parse.textContent())
                        .collectValues((Article a, String p) -> a.getParagraphs().add(p), Article.class)
                )
                .nextBranch(Get.descendants().byAttr("data-component", "unordered-list-block")
                        .next(Get.descendants().byTag("a"))
                        .next(Parse.hRef())
                        .collectValues((Article a, String p) -> a.getLinks().add(p), Article.class)
                );
    }

    private boolean isSportArticle(Article a) {
        return a.getCategory() != null && a.getCategory().equalsIgnoreCase("Sport");
    }

    private boolean isNotSportArticle(Article a) {
        return !isSportArticle(a);
    }


    private void start(Scraping articlesScraping) throws InterruptedException {
        articlesScraping.start(Duration.ofMinutes(2));
        Thread.sleep(1000); // let logging finish
    }


    @Data
    public static class Section {
        private String name;
    }

    @Data
    public static class Promo {
        private String heading;
        private String summary;
    }

    @Data
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
        public void onScrapedData(Article data) {
            log.info("\n" + JsonUtils.write(data).orElse("JSON ERROR"));
        }
    }

    public static class SectionListener implements ScrapedDataListener<Section> {
        @Override
        public void onScrapedData(Section data) {
            log.info("\n" + JsonUtils.write(data).orElse("JSON ERROR"));
        }
    }


}
