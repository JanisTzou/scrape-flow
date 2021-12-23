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
    public void start() {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());
        final HtmlUnitSiteParser siteParser = new HtmlUnitSiteParser(driverManager);

        final Scraping productsScraping = new Scraping(siteParser, 10)
                .setParsingSequence(
                        GetElements.ByTag.body()
                                .next(GetElements.ByTextContent.searchByString("Mozaika skleněná", true)
                                        .next(Actions.mapElements(domNode -> Optional.ofNullable(domNode.getParentNode()))
                                                .next(GetElements.ByTag.anchor()
                                                        .next(Parse.textContent().setCollector(Product::setCategory)) // ... TODO hmm interesting ... how to communicate the category downstream ?
                                                        .next(Parse.hRef(href -> "https://www.maxeuro.cz" + href)
                                                                .nextNavigate(Actions.navigateToParsedLink(siteParser)
                                                                        .next(Actions.paginate()
                                                                                .setStepsLoadingNextPage(
                                                                                        GetElements.ByCssClass.className("pagination")
                                                                                                .next(GetElements.ByTextContent.searchByString("»", true) // returns anchor
                                                                                                        .next(Actions.filterElements(domNode -> !HtmlUnitUtils.hasAttributeWithValue(domNode.getParentNode(), "class", "disabled", true))
                                                                                                                .next(Actions.followLink()
                                                                                                                        .next(Actions.returnNextPage())
                                                                                                                )
                                                                                                        )
                                                                                                )
                                                                                )
                                                                                .nextForEachPage(
                                                                                        GetElements.ByCssClass.className("product").stepName("product-search")
                                                                                                .setCollector(Product::new, ProductsPage::new, ProductsPage::add, new ProductListenerParsed())
                                                                                                .next(GetElements.ByCssClass.className("product-name")
                                                                                                        .next(GetElements.ByTag.anchor()
                                                                                                                .next(Parse.textContent()
                                                                                                                        .setCollector(Product::setName)
                                                                                                                )
                                                                                                        )
                                                                                                        .next(GetElements.ByTag.anchor()
                                                                                                                .next(Parse.hRef(href -> "https://www.maxeuro.cz" + href)
                                                                                                                        .setCollector(Product::setDetailUrl)
                                                                                                                        .nextNavigate(Actions.navigateToParsedLink(siteParser)
                                                                                                                                .next(GetElements.ByAttribute.id("productDescription1")
                                                                                                                                        .next(Parse.textContent()
                                                                                                                                                .setCollector(Product::setDescription)
                                                                                                                                        )
                                                                                                                                )
                                                                                                                        )
                                                                                                                )
                                                                                                        )
                                                                                                )
                                                                                                .next(GetElements.ByCssClass.className("cena")
                                                                                                        .next(Parse.textContent(txt -> txt.replace(" ", "").replace("Kč(m2)", "").replace(",", ".").replace("Kč(bm)", ""))
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


        final String url = "https://www.maxeuro.cz/obklady-dlazby-mozaika-kat_1010.html";
        final EntryPoint entryPoint = new EntryPoint(url, productsScraping);
        final Scraper scraper = new Scraper();

        scraper.scrape(entryPoint);

        scraper.awaitCompletion(Duration.ofSeconds(200));
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
        private volatile String description;
    }


    @Log4j2
    public static class ProductListenerParsed implements ParsedDataListener<Product> {

        @Override
        public void onParsingFinished(Product data) {
            log.info(JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

}
