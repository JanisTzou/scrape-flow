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

import com.github.web.scraping.lib.scraping.EntryPoint;
import com.github.web.scraping.lib.scraping.Scraper;
import com.github.web.scraping.lib.scraping.Scraping;
import com.github.web.scraping.lib.utils.JsonUtils;
import com.github.web.scraping.lib.scraping.htmlunit.Actions;
import com.github.web.scraping.lib.scraping.htmlunit.GetElements;
import com.github.web.scraping.lib.scraping.htmlunit.HtmlUnitSiteParser;
import com.github.web.scraping.lib.scraping.htmlunit.Parse;
import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;
import com.github.web.scraping.lib.parallelism.ParsedDataListener;
import com.github.web.scraping.lib.scraping.htmlunit.HtmlUnitUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.time.Duration;
import java.util.Optional;

public class MaxEuroCzDemo {

    @Test
    public void start() throws InterruptedException {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());
        final HtmlUnitSiteParser siteParser = new HtmlUnitSiteParser(driverManager);

        final Scraping productsScraping = new Scraping(siteParser, 10);

        // TODO capture the declaration order of the steps somehow ... so we can forget

        productsScraping.setScrapingSequence(
                GetElements.Descendants.ByTextContent.search("Mozaika skleněná", true)
                        .next(Actions.mapElements(domNode -> Optional.ofNullable(domNode.getParentNode()))
                                .next(GetElements.Descendants.ByTag.anchor()
                                        .setCollector(Category::new, Category.class)
                                        .next(Parse.textContent()
                                                .collect(Category::setName, Category.class)
                                        )
                                        .next(Parse.hRef(href -> "https://www.maxeuro.cz" + href)
                                                .nextNavigate(Actions.navigateToParsedLink(siteParser)
                                                        .next(Actions.paginate()
                                                                .setStepsLoadingNextPage(
                                                                        GetElements.Descendants.ByCss.byClassName("pagination")
                                                                                .next(GetElements.Descendants.ByTextContent.search("»", true) // returns anchor
                                                                                        .next(Actions.filterElements(domNode -> !HtmlUnitUtils.hasAttributeWithValue(domNode.getParentNode(), "class", "disabled", true))
                                                                                                .next(Actions.followLink()
                                                                                                        .next(Actions.returnNextPage())
                                                                                                )
                                                                                        )
                                                                                )
                                                                )
                                                                .nextForEachPage(
                                                                        GetElements.Descendants.ByCss.byClassName("product").stepName("product-search")
                                                                                .setCollector(Product::new, Product.class, new ProductListenerParsed())
                                                                                .collect(Product::setCategory, Product.class, Category.class)
                                                                                .next(GetElements.Descendants.ByCss.byClassName("product-name")
                                                                                        .next(GetElements.Descendants.ByTag.anchor()
                                                                                                .next(Parse.textContent()
                                                                                                        .collect(Product::setName, Product.class)
                                                                                                )
                                                                                        )
                                                                                        .next(GetElements.Descendants.ByTag.anchor()
                                                                                                .next(Parse.hRef(href -> "https://www.maxeuro.cz" + href).stepName("get-product-detail-url")
                                                                                                        .collect(Product::setDetailUrl, Product.class)
                                                                                                        .nextNavigate(Actions.navigateToParsedLink(siteParser)
                                                                                                                .next(GetElements.Descendants.ByAttribute.id("productDescription1")
                                                                                                                        .next(Parse.textContent()
                                                                                                                                .collect(Product::setDescription, Product.class)
                                                                                                                        )
                                                                                                                )
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                )
                                                                                .next(GetElements.Descendants.ByCss.byClassName("cena")
                                                                                        .next(Parse.textContent(txt -> txt.replace(" ", "").replace("Kč(m2)", "").replace(",", ".").replace("Kč(bm)", ""))
                                                                                                .collect(Product::setPrice, Product.class)
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
    public static class SubCategory {
        private volatile String name;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Product {
        private volatile Category category;
        private volatile SubCategory subCategory;
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
