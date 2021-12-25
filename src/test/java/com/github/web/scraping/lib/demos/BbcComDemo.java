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
import com.github.web.scraping.lib.scraping.htmlunit.*;
import com.github.web.scraping.lib.utils.JsonUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class BbcComDemo {

    public static final String HTTPS_WWW_BBC_COM = "https://www.bbc.com";


    @Test
    public void demo() throws InterruptedException {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());
        final HtmlUnitSiteParser parser = new HtmlUnitSiteParser(driverManager);

        final Scraping articlesScraping = new Scraping(parser, 10)
                .setScrapingSequence(
                        GetElements.Descendants.ByAttribute.nameAndValue("aria-label", "World").getFirst()
                                .next(GetElements.Descendants.ByTag.ul().getFirst()
                                        .next(GetElements.Descendants.ByTag.li()
                                                .next(Actions.filterElements(domNode -> domNode.getTextContent().contains("Africa"))
                                                        .setCollector(Section::new, Section.class, new SectionListener())
                                                        .next(GetElements.Descendants.ByTag.anchor()
                                                                .next(Parse.textContent()
                                                                        .collect(Section::setName, Section.class)
                                                                )
                                                                .next(Parse.hRef(href -> HTTPS_WWW_BBC_COM + href)
                                                                        .nextNavigate(toSections(parser))
                                                                )
                                                        )
                                                )
                                        )
                                )
                );

        start(articlesScraping);
    }

    private NavigateToParsedLink toSections(HtmlUnitSiteParser parser) {
        return Actions.navigateToParsedLink(parser)
                .next(GetElements.Descendants.ByAttribute.id("featured-contents")
                        .next(GetElements.ByDomTraversal.nextSiblingElem()
                                .next(GetElements.Descendants.ByCss.bySelector("div.gs-c-promo").stepName("listed-article")
                                        .setCollector(Promo::new, Promo.class)
                                        .setCollector(Article::new, Article.class, new ArticleListener())
                                        .collect(Article::setRegion, Article.class, Section.class)
                                        .collect(Article::setPromo, Article.class, Promo.class)
                                        .next(GetElements.Descendants.ByCss.bySelector("p.gs-c-promo-summary")
                                                .next(Parse.textContent()
                                                        .collect(Promo::setSummary, Promo.class)
                                                )
                                        )
                                        .next(GetElements.Descendants.ByCss.bySelector("a.gs-c-promo-heading")
                                                .next(GetElements.Descendants.ByTag.h3().next(
                                                                Parse.textContent()
                                                                        .collect(Promo::setHeading, Promo.class)
                                                        )
                                                )
                                                .next(Parse.hRef(href -> href.contains("https") ? href : HTTPS_WWW_BBC_COM + href)
                                                        .collect(Article::setUrl, Article.class)
                                                        .nextNavigate(toArticles(parser))
                                                )

                                        )
                                )
                        )
                )
                ;
    }


    private NavigateToParsedLink toArticles(HtmlUnitSiteParser parser) {
        return Actions.navigateToParsedLink(parser)
                .next(GetElements.Descendants.ByTag.article()
                        .nextExclusively(GetElements.Descendants.ByTextContent.search("Sport Africa", true) // category must be parsed before following steps can proceed -> exclusive call
                                .next(Parse.textContent()
                                        .setTransformation(s -> "Sport")
                                        .collect(Article::setCategory, Article.class)
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

    private StepGroup parseSportArticle() {
        return StepFlow.asStepGroup()
                .next(GetElements.Descendants.ByAttribute.id("page")
                        .next(Parse.textContent()
                                .collect(Article::setTitle, Article.class)
                        )
                )
                .next(GetElements.Descendants.ByTag.p()
                        .next(Parse.textContent()
                                .collect((Article a, String p) -> a.getParagraphs().add(p), Article.class)
                        )
                );
    }

    private StepGroup parseNonSportArticle() {
        return StepFlow.asStepGroup()
                .next(GetElements.Descendants.ByAttribute.id("main-heading")
                        .next(Parse.textContent()
                                .collect(Article::setTitle, Article.class)
                        )
                )
                .next(GetElements.Descendants.ByAttribute.nameAndValue("data-component", "text-block")
                        .next(Parse.textContent()
                                .collect((Article a, String p) -> a.getParagraphs().add(p), Article.class)
                        )
                )
                .next(GetElements.Descendants.ByAttribute.nameAndValue("data-component", "unordered-list-block")
                        .next(GetElements.Descendants.ByTag.anchor()
                                .next(Parse.hRef()
                                        .collect((Article a, String p) -> a.getLinks().add(p), Article.class)
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
        scraper.scrape(entryPoint);
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


    public static class ArticleListener implements ParsedDataListener<Article> {
        @Override
        public void onParsingFinished(Article data) {
            log.info(JsonUtils.write(data).orElse("JSON ERROR"));
        }
    }

    public static class SectionListener implements ParsedDataListener<Section> {
        @Override
        public void onParsingFinished(Section data) {
            log.info(JsonUtils.write(data).orElse("JSON ERROR"));
        }
    }


}
