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

import com.github.web.scraping.lib.Scraper;
import com.github.web.scraping.lib.Scraping;
import com.github.web.scraping.lib.EntryPoint;
import com.github.web.scraping.lib.dom.data.parsing.JsonUtils;
import com.github.web.scraping.lib.dom.data.parsing.steps.*;
import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;
import com.github.web.scraping.lib.parallelism.ParsedDataListener;
import com.github.web.scraping.lib.scraping.utils.HtmlUnitUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaxEuroCzDemo {

    @Test
    public void start() throws InterruptedException {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());
        final HtmlUnitSiteParser siteParser = new HtmlUnitSiteParser(driverManager);

        final Scraping productsScraping = new Scraping(siteParser)
                .setParsingSequence(
                        GetElements.ByTag.body()
                                .then(GetElements.ByTextContent.searchString("Mozaika skleněná", false)
                                        .then(Actions.mapElements(anchor -> Optional.ofNullable(anchor.getParentNode()))
                                                .then(GetElements.ByTag.anchor()
                                                        .then(ParseElement.getTextContent().setCollector(Product::setCategory)) // ... TODO hmm interesting ... how to communicate the category downstream ?
                                                        .then(ParseElement.getHRef(href -> "https://www.maxeuro.cz" + href)
                                                                .thenNavigate(Actions.navigateToParsedLink(siteParser)
                                                                        .then(Actions.paginate()
                                                                                .setPaginationTrigger(
                                                                                        GetElements.ByCssClass.className("pagination")
                                                                                                .then(GetElements.ByTextContent.searchString("»", false) // returns anchor
                                                                                                        .then(Actions.filterElements(domNode -> !HtmlUnitUtils.hasAttributeWithValue(domNode.getParentNode(), "class", "disabled", true))
                                                                                                                .then(Actions.followLink()
                                                                                                                        .then(Actions.returnNextPage())
                                                                                                                )
                                                                                                        )
                                                                                                )
                                                                                )
                                                                                .thenForEachPage(
                                                                                        GetElements.ByCssClass.className("product").stepName("product-search")
                                                                                                .setCollector(Product::new, ProductsPage::new, ProductsPage::add, new ProductListenerParsed())
                                                                                                .then(GetElements.ByCssClass.className("product-name")
                                                                                                        .then(GetElements.ByTag.anchor()
                                                                                                                .then(ParseElement.getTextContent()
                                                                                                                        .setCollector(Product::setName)
                                                                                                                )
                                                                                                        )
                                                                                                        .then(GetElements.ByTag.anchor()
                                                                                                                .then(ParseElement.getHRef(href -> "https://www.maxeuro.cz" + href)
                                                                                                                        .setCollector(Product::setDetailUrl)
                                                                                                                )
                                                                                                        )
                                                                                                )
                                                                                                .then(GetElements.ByCssClass.className("cena")
                                                                                                        .then(ParseElement.getTextContent(txt -> txt.replace(" ", "").replace("Kč(m2)", "").replace(",", ".").replace("Kč(bm)", ""))
                                                                                                                .setCollector(Product::setPrice)
                                                                                                        )
                                                                                                )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                );


        // TODO maybe the entry url should be part of the first scraping stage? And we can have something like "FirstScrapingStage) ... or maybe entry point abstraction is good enough ?

        final String url = "https://www.maxeuro.cz/obklady-dlazby-mozaika-kat_1010.html";
        final EntryPoint entryPoint = new EntryPoint(url, productsScraping);
        final Scraper scraper = new Scraper();

        scraper.scrape(entryPoint);

        Thread.sleep(15000);

        scraper.awaitCompletion(Duration.ofSeconds(200)); // TODO await completion ...

    }


    @Getter
    @NoArgsConstructor
    @ToString
    public static class ProductsPage {
        private final List<Product> products = new ArrayList<>();
        private volatile String position;

        public void add(Product product) {
            this.products.add(product);
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }


    @Getter
    @NoArgsConstructor
    @ToString
    @Deprecated
    public static class Products {

        private final List<Product> products = new ArrayList<>();

        public void add(Product product) {
            this.products.add(product);
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Product {
        private volatile String category;
        private volatile String name;
        private volatile String price;
        private volatile String detailUrl;
    }


    @Log4j2
    public static class ProductListenerParsed implements ParsedDataListener<Product> {

        @Override
        public void onParsingFinished(Product data) {
            log.info(JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

}
