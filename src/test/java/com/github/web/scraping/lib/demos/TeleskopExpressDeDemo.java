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
import com.github.web.scraping.lib.scraping.htmlunit.*;
import com.github.web.scraping.lib.utils.JsonUtils;
import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;
import com.github.web.scraping.lib.parallelism.ParsedDataListener;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.github.web.scraping.lib.scraping.htmlunit.HtmlUnit.*;

public class TeleskopExpressDeDemo {

    @Test
    public void start() throws InterruptedException {

        // TODO it might be nice to provide progress in the logs ... like scraped 10/125 Urls ... (if the finitie number of urls is available)

        // TODO suppot something like temporary models? e.g. for communication of multiple pieces of data downstream so that an action/step can be executed ...

        // TODO any way for these to be accessible globally? So they do not need to be specified explicitly in every stage definition?
        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        // TODO the parsing/scraping steps should be better named so it is clear what action they perform ... it might not be parsing exacly but also actions like button clicks etc ...
        //  maybe it is ok to have a "parsing ste" that is not exacly parsing enything but performing an action ... it's just something that needs to be performed to do the actual parsing ...

        GetElementsByAttribute getNextPageLinkElemStep = Get.Descendants.ByAttribute.nameAndValue("title", " nächste Seite ").stepName("get-next-page-elem");
        // TODO here there are duplicates becase bellow the instances are mutated ... change this so that each call below in the sequence
        //  creates a new instance based on the previous one and only then it sets values ....
        GetElementsByCssClass getProductTdElemsStep = Get.Descendants.ByCss.byClassName("main").stepName("get-product-elems"); // TODO add by tag ... filtering
        GetElementsByCssClass getProductCodeElemStep = Get.Descendants.ByCss.byClassName("PRODUCTS_NAME").stepName("get-product-code-elem-1");
        GetElementsByCssClass getProductCodeElemStep2 = Get.Descendants.ByCss.byClassName("PRODUCTS_NAME").stepName("get-product-code-elem-2");
        GetElementsByCssClass getProductPriceElemStep = Get.Descendants.ByCss.byClassName("prod_preis").stepName("get-product-price-elem");
        GetElementsByAttribute getProductDetailHRefElemStep = Get.Descendants.ByAttribute.nameAndValue("href", "product_info.php/info").setMatchEntireValue(false).stepName("get-product-detail-elem");
        FollowLink clickNextPageLinkElem = Do.followLink().stepName("click-next-page-button");
        GetElementsByAttribute getProductDetailTitleElem = Get.Descendants.ByAttribute.nameAndValue("itemprop", "name");
        GetElementsByAttribute getProductDescriptionElem = Get.Descendants.ByAttribute.nameAndValue("id", "c0");

        final Scraping productsScraping = new Scraping(new HtmlUnitSiteParser(driverManager), 1)
                .setScrapingSequence(
                        Get.Descendants.ByTag.body()
                                .next(new EmptyStep().stepName("before-pagination"))
                                .next(Do.paginate()
                                        .setStepsLoadingNextPage(
                                                getNextPageLinkElemStep
                                                        .next(clickNextPageLinkElem
                                                                .next(Do.returnNextPage())
                                                        )
                                        )
                                        .nextExclusively(Flow.asStepGroup() // no steps with higher order than this very step can be allowed to run before this whole thing finishes ... lower steps can continue running ... send an event that this step finished so normal parallelism can resume ... also there might be nested ordered steps ...
                                                        .next(Get.Descendants.ByCss.byClassName("headerlinks").stepName("get-nav-position-elem-step")
                                                                .next(Parse.textContent().stepName("pet-1")
                                                                        .collectOne(ProductsPage::setPosition, ProductsPage.class)
                                                                )
                                                        )
                                                        .next(Get.Descendants.ByCss.byClassName("headerlinks").stepName("get-nav-position-elem-step")
                                                                .next(Parse.textContent().stepName("pet-1")
                                                                        .collectOne(ProductsPage::setPosition, ProductsPage.class)
                                                                )
                                                        )
                                        )
                                        .next(Get.Descendants.ByCss.byClassName("headerlinks").stepName("get-nav-position-elem-step")
                                                .next(Parse.textContent().stepName("pet-1")
                                                        .collectOne(ProductsPage::setPosition, ProductsPage.class)
                                                )
                                        )
                                        .next(getProductTdElemsStep
                                                .setCollector(Product::new, Product.class, new ProductParsedListener())// this step generates the Product model and does not modify it ... all the child steps need to finish
                                                // this can register the step order of this parent step with NotificationOrderingService ...
                                                // set parsed data listener ?
                                                //  ... we might wanna listen for simple String values that were parsed ... not just complex models ...
                                                // when should data be eligible to notify listeners?
                                                .next(getProductCodeElemStep
                                                        .next(new EmptyStep().stepName("before-product-code-collection")
                                                                .setCollector(ProductCode::new, ProductCode.class)
                                                                .collectOne(Product::setProductCode, Product.class, ProductCode.class)
                                                                .next(Parse.textContent().stepName("pet-2").collectOne(ProductCode::setValue, ProductCode.class))
                                                        )
                                                )
                                                .next(getProductPriceElemStep
                                                        .next(Parse.textContent().collectOne(Product::setPrice, Product.class))
                                                )
                                                .next(getProductCodeElemStep2
                                                        .next(Parse.hRef(hrefVal -> "https://www.teleskop-express.de/shop/" + hrefVal).stepName("parse-product-href")
                                                                .collectOne(Product::setDetailUrl, Product.class)
                                                                .nextNavigate(Do.navigateToParsedLink(new HtmlUnitSiteParser(driverManager))
                                                                        .next(getProductDetailTitleElem.stepName("get-product-detail-title") // TODO perhaps we need a setScrapingSequence method after all instead of then() here ... so that we are consistent with how we set up a SiteParse (allows only 1 sequence) ....
                                                                                .next(Parse.textContent().collectOne(Product::setTitle, Product.class))
                                                                        )
                                                                        .next(getProductDescriptionElem
                                                                                .next(Parse.textContent().collectOne(Product::setDescription, Product.class))
                                                                        )
                                                                        .next(Get.Descendants.ByAttribute.nameAndValue("id", "MwStInfoMO")
                                                                                .next(Get.Descendants.ByTag.anchor()
                                                                                        .next(Parse.hRef(hrefVal -> "https://www.teleskop-express.de/shop/" + hrefVal)
                                                                                                .nextNavigate(Do.navigateToParsedLink(new HtmlUnitSiteParser(driverManager))
                                                                                                        .next(GetListedElementsByFirstElementXPath.instance("/html/body/table/tbody/tr[1]")
                                                                                                                .setCollector(ShippingCosts::new, ShippingCosts.class)
                                                                                                                .collectMany((Product p, ShippingCosts sc) -> p.getShippingCosts().add(sc), Product.class, ShippingCosts.class)
                                                                                                                .next(GetListedElementByFirstElementXPath.instance("/html/body/table/tbody/tr[1]/td[1]")
                                                                                                                        .next(Parse.textContent().stepName("get-shipping-service").collectOne(ShippingCosts::setService, ShippingCosts.class))
                                                                                                                )
                                                                                                                .next(GetListedElementByFirstElementXPath.instance("/html/body/table/tbody/tr[1]/td[2]")
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


        // TODO maybe the entry url should be part of the first scraping stage? And we can have something like "FirstScrapingStage) ... or maybe entry point abstraction is good enough ?

        String url = "https://www.teleskop-express.de/shop/index.php/cat/c6_Eyepieces-1-25-inch-up-to-55--field.html/page/2";
//        String url = "https://www.teleskop-express.de/shop/index.php/cat/c6_Eyepieces-1-25-inch-up-to-55--field.html";
        final EntryPoint entryPoint = new EntryPoint(url, productsScraping);
        final Scraper scraper = new Scraper();

        scraper.scrape(entryPoint);

        scraper.awaitCompletion(Duration.ofMinutes(5));
        Thread.sleep(2000); // let logging finish ...

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
    public static class ProductParsedListener implements ParsedDataListener<Product> {

        @Override
        public void onParsingFinished(Product data) {
            log.info(JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

}
