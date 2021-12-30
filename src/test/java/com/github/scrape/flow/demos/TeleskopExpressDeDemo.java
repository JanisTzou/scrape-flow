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
import com.github.scrape.flow.scraping.htmlunit.*;
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

public class TeleskopExpressDeDemo {

    @Test
    public void start() throws InterruptedException {

        // TODO it might be nice to provide progress in the logs ... like scraped 10/125 Urls ... (if the finitie number of urls is available)

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        GetDescendants getNextPageLinkElemStep = Get.descendants().byAttr("title", " nächste Seite ").stepName("get-next-page-elem");
        GetDescendants getProductTdElemsStep = Get.descendants().byClass("main").stepName("get-product-elems");
        GetDescendants getProductCodeElemStep = Get.descendants().byClass("PRODUCTS_NAME").stepName("get-product-code-elem-1");
        GetDescendants getProductPriceElemStep = Get.descendants().byClass("prod_preis").stepName("get-product-price-elem");
        FollowLink clickNextPageLinkElem = Do.followLink().stepName("click-next-page-button");
        GetDescendants getProductDetailTitleElem = Get.descendants().byAttr("itemprop", "name");
        GetDescendants getProductDescriptionElem = Get.descendants().byAttr("id", "c0");

        final Scraping productsScraping = new Scraping(new HtmlUnitSiteParser(driverManager), 1, TimeUnit.SECONDS)
                .getDebugOptions().setOnlyScrapeFirstElements(false)
                .getDebugOptions().setLogFoundElementsSource(false)
                .setSequence(
                        Do.paginate()
                                .setStepsLoadingNextPage(
                                        getNextPageLinkElemStep
                                                .next(clickNextPageLinkElem
                                                        .next(Do.returnNextPage())
                                                )
                                )
                                .next(getProductTdElemsStep
                                        .addCollector(Product::new, Product.class, new ProductScrapedListener())
                                        .next(getProductCodeElemStep
                                                .addCollector(ProductCode::new, ProductCode.class)
                                                .collectOne(Product::setProductCode, Product.class, ProductCode.class)
                                                .next(Parse.textContent().stepName("pet-2").collectOne(ProductCode::setValue, ProductCode.class))
                                        )
                                        .next(getProductPriceElemStep
                                                .next(Parse.textContent().collectOne(Product::setPrice, Product.class))
                                        )
                                        .next(getProductCodeElemStep
                                                .next(Parse.hRef(hrefVal -> "https://www.teleskop-express.de/shop/" + hrefVal).stepName("parse-product-href")
                                                        .collectOne(Product::setDetailUrl, Product.class)
                                                        .nextNavigate(Do.navigateToParsedLink(new HtmlUnitSiteParser(driverManager))
                                                                .next(getProductDetailTitleElem.stepName("get-product-detail-title")
                                                                        .next(Parse.textContent().collectOne(Product::setTitle, Product.class))
                                                                )
                                                                .next(getProductDescriptionElem
                                                                        .next(Parse.textContent().collectOne(Product::setDescription, Product.class))
                                                                )
                                                                .next(Get.descendants().byAttr("id", "MwStInfoMO")
                                                                        .next(Get.descendants().byTag("a")
                                                                                .next(Parse.hRef(hrefVal -> "https://www.teleskop-express.de/shop/" + hrefVal)
                                                                                        .nextNavigate(Do.navigateToParsedLink(new HtmlUnitSiteParser(driverManager))
                                                                                                .next(Get.byXPath("/html/body/table/tbody")
                                                                                                        .next(Get.children().byTag("tr").excludingFirstN(1) // rows doe each shipping service price; first row contains captions
                                                                                                                .addCollector(ShippingCosts::new, ShippingCosts.class)
                                                                                                                .collectMany((Product p, ShippingCosts sc) -> p.getShippingCosts().add(sc), Product.class, ShippingCosts.class)
                                                                                                                .next(Get.children().byTag("td").first()  // service name
                                                                                                                        .next(Parse.textContent().stepName("get-shipping-service").collectOne(ShippingCosts::setService, ShippingCosts.class))
                                                                                                                )
                                                                                                                .next(Get.children().byTag("td").firstNth(2)  // service price
                                                                                                                        .next(Parse.textContent().stepName("get-shipping-price").collectOne(ShippingCosts::setPrice, ShippingCosts.class))
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
                                )
                );


        String url = "https://www.teleskop-express.de/shop/index.php/cat/c6_Eyepieces-1-25-inch-up-to-55--field.html/page/2";
//        String url = "https://www.teleskop-express.de/shop/index.php/cat/c6_Eyepieces-1-25-inch-up-to-55--field.html";
        final EntryPoint entryPoint = new EntryPoint(url, productsScraping);
        final Scraper scraper = new Scraper();

        scraper.start(entryPoint);

        scraper.awaitCompletion(Duration.ofMinutes(5));
        Thread.sleep(2000); // let logging finish ...

    }


    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Brand {
        private volatile String name;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Product {
        private volatile String title;
        private volatile String price;
        private volatile ProductCode productCode;
        private volatile String detailUrl;
        private volatile String description;
        private volatile List<ShippingCosts> shippingCosts = new ArrayList<>();
    }

    @Setter
    @Getter
    @ToString
    public static class ProductCode {
        private volatile String value;
    }

    @Setter
    @Getter
    @ToString
    public static class ShippingCosts {
        private volatile String service;
        private volatile String price;
    }

    @Log4j2
    public static class ProductScrapedListener implements ScrapedDataListener<Product> {

        @Override
        public void onParsedData(Product data) {
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

}