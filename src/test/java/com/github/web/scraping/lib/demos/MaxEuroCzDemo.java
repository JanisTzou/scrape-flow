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

import com.github.web.scraping.lib.Crawler;
import com.github.web.scraping.lib.Crawling;
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

        final Crawling productsCrawling = new Crawling()
                .setSiteParser(new HtmlUnitSiteParser(driverManager)
                                .setParsingSequence(
                                        new GetHtmlBody()
//                                        .then(GetListedElementByFirstElementXPath.instance("/html/body/div[2]/div[4]/div/ul/li[12]")
//                                                .then(GetElementsByAttribute.id("menuleft")
//                                                        .then(GetElementsByTag.li()
                                                .then(new GetElementsByTextContent("Mozaika skleněná")
                                                        .setMatchWholeTextContent(false)
                                                        .then(new MapElements(anchor -> Optional.ofNullable(anchor.getParentNode()))
                                                                .then(GetElementsByTag.anchor() // ... confusing that this is separate from the ParseHRef ...
                                                                        .then(ParseElementText.instance().setCollector(Product::setCategory)) // ... TODO hmm interesting ... how to communicate the category down ?
                                                                        .then(new ParseElementHRef()
                                                                                .setTransformation(href -> "https://www.maxeuro.cz" + href)
                                                                                .thenNavigate(new NavigateToParsedHRef()
                                                                                        .setSiteParser(new HtmlUnitSiteParser(driverManager))
                                                                                        .then(new Paginate()
                                                                                                .setPaginationTrigger(
                                                                                                        GetElementsByCssClass.instance("pagination")
                                                                                                                .then(new GetElementsByTextContent("»")
                                                                                                                        .then(new FilterElements(domNode -> {
                                                                                                                                    return !HtmlUnitUtils.hasAttributeWithValue(domNode.getParentNode(), "class", "disabled", true);
                                                                                                                                })
                                                                                                                                        .then(new ClickElement()
                                                                                                                                                .then(new ReturnNextPage())
                                                                                                                                        )
                                                                                                                        )
                                                                                                                )
                                                                                                )
                                                                                                .thenForEachPage(GetElementsByCssClass.instance("product").setName("product-search")
                                                                                                        .setCollector(Product::new, ProductsPage::new, ProductsPage::add, new ProductListenerParsed())
                                                                                                        .then(GetElementsByCssClass.instance("product-name")
                                                                                                                .then(GetElementsByTag.anchor()
                                                                                                                        .then(ParseElementText.instance()
                                                                                                                                .setCollector(Product::setName)
                                                                                                                        )
                                                                                                                )
                                                                                                                .then(GetElementsByTag.anchor()
                                                                                                                        .then(ParseElementHRef.instance()
                                                                                                                                .setTransformation(href -> "https://www.maxeuro.cz" + href)
                                                                                                                                .setCollector(Product::setDetailUrl)
                                                                                                                        )
                                                                                                                )
                                                                                                        )
                                                                                                        .then(GetElementsByCssClass.instance("cena")
                                                                                                                .then(ParseElementText.instance()
                                                                                                                        .setTransformation(txt -> txt.replace(" ", "").replace("Kč(m2)", "").replace(",", ".").replace("Kč(bm)", ""))
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
                                )
                );


        // TODO maybe the entry url should be part of the first scraping stage? And we can have something like "FirstScrapingStage) ... or maybe entry point abstraction is good enough ?

        String url = "https://www.maxeuro.cz/obklady-dlazby-mozaika-kat_1010.html";
        final EntryPoint entryPoint = new EntryPoint(url, productsCrawling);
        final Crawler crawler = new Crawler();

        crawler.scrape(entryPoint);

        crawler.awaitCompletion(Duration.ofSeconds(200)); // TODO await completion ...

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
