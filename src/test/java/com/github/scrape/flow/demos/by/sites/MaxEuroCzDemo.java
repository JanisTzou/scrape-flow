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

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.scrape.flow.data.publishing.ScrapedDataListener;
import com.github.scrape.flow.scraping.Scraping;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitGetDescendants;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitNavigateToParsedLink;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitUtils;
import com.github.scrape.flow.scraping.selenium.Selenium;
import com.github.scrape.flow.scraping.selenium.SeleniumNavigateToParsedLink;
import com.github.scrape.flow.utils.JsonUtils;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnit.*;

public class MaxEuroCzDemo {

    //    @Ignore
    @Test
    public void start() throws InterruptedException {

        final Scraping scraping = new Scraping(10, TimeUnit.SECONDS);


        /*
            <li>
                <a href="/obklady-dlazby-mozaika-kat_1010.html" class="in-path">
                    Obklady, dlažby, mozaika
                </a>
                <ul class="level-2">
                    <!--...-->
                    <li>
                        <a href="/mozaika-sklenena-kat_109.html" class="selected">
                            Mozaika skleněná
                        </a>
                    </li>
                    <!--...-->
                </ul>
            </li>
         */

        scraping.getDebugOptions().setOnlyScrapeFirstElements(false)
                .getDebugOptions().setLogFoundElementsSource(false)
                .getDebugOptions().setLogFoundElementsCount(false)
                .getOptions().setMaxRequestRetries(2);

        // TODO maybe the static and dynamic parses should be specified as part of the options ?

        // TODO here we are passing a static site sequence ... but the parser is defined elsewhere ... how to deal with that?
        scraping.setSequence(Do.navigateTo("https://www.maxeuro.cz/obklady-dlazby-mozaika-kat_1010.html")
                .next(Get.descendants().byTextContent("Mozaika skleněná")
                        .first()
                        .debugOptions().logFoundElementsSource(false)
                )
                .next(Get.natively(domNode -> Optional.ofNullable(domNode.getParentNode())))
                .next(Get.descendants().byTag("a"))
                .addCollector(Category::new, Category.class)
                .nextBranch(Parse.textContent().collectValue(Category::setName, Category.class))
                .nextBranch(Parse.hRef(href -> "https://www.maxeuro.cz" + href)
                        .next(Do.navigateToParsedLink())
                        .next(Flow.withPagination().setStepsLoadingNextPage(
                                        Get.descendants().byClass("pagination").stepName("product-pagination")
                                                .next(Get.descendants().byTextContent("»"))  // returns anchor
                                                .next(Filter.natively(domNode -> !isDisabled(domNode)))
                                                .next(Do.followLink())
                                                .next(Flow.returnNextPage())
                                )
                        )
                        .next(Get.descendants().byClass("product").stepName("product-search")
                                .addCollector(Product::new, Product.class, new ProductListenerScraped())
                                .collectValue(Product::setCategory, Product.class, Category.class)
                        )
                        .nextBranch(Get.descendants().byClass("product-name")
                                .next(Get.descendants().byTag("a")
                                        .nextBranch(Parse.textContent().collectValue(Product::setName, Product.class)
                                        )
                                        .nextBranch(Parse.hRef(href -> "https://www.maxeuro.cz" + href).stepName("get-product-detail-url")
                                                .collectValue(Product::setDetailUrl, Product.class)
                                                .next(Do.navigateToParsedLink())
                                                .next(Get.descendants().byAttr("id", "productDescription1"))
                                                .next(Parse.textContent().collectValue(Product::setDescription, Product.class))
                                        )
                                )
                        )
                        .nextBranch(Get.descendants().byClass("cena")
                                .next(Parse.textContent(this::parseNumber).collectValue(Product::setPrice, Product.class))
                        )
                        .next(Do.peek(d -> System.out.println(d)))
                )
        );

        start(scraping);
    }

    @Nonnull
    private String parseNumber(String txt) {
        return txt.replace(" ", "").replace("Kč(m2)", "").replace(",", ".").replace("Kč(bm)", "");
    }

    private boolean isDisabled(DomNode domNode) {
        return HtmlUnitUtils.hasAttributeWithExactValue(domNode.getParentNode(), "class", "disabled");
    }

    private void start(Scraping scraping) throws InterruptedException {
        scraping.start();
        scraping.awaitCompletion(Duration.ofMinutes(2));
        Thread.sleep(2000); // let logging finish ...
    }


    @Data
    public static class Category {
        private volatile String name;
    }

    @Data
    public static class SubCategory {
        private volatile String name;
    }

    @Data
    public static class Product {
        private volatile Category category;
        private volatile SubCategory subCategory;
        private volatile String name;
        private volatile String price;
        private volatile String detailUrl;
        private volatile String description;
    }


    @Log4j2
    public static class ProductListenerScraped implements ScrapedDataListener<Product> {

        @Override
        public void onScrapedData(Product data) {
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

}
