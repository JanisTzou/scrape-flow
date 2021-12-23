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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TeleskopExpressDeDemo {

    @Test
    public void start() {

        // TODO it might be nice to provide progress in the logs ... like scraped 10/125 Urls ... (if the finitie number of urls is available)

        // TODO suppot something like temporary models? e.g. for communication of multiple pieces of data downstream so that an action/step can be executed ...

        // TODO any way for these to be accessible globally? So they do not need to be specified explicitly in every stage definition?
        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        // TODO the parsing/scraping steps should be better named so it is clear what action they perform ... it might not be parsing exacly but also actions like button clicks etc ...
        //  maybe it is ok to have a "parsing ste" that is not exacly parsing enything but performing an action ... it's just something that needs to be performed to do the actual parsing ...

        GetElementsByAttribute getNextPageLinkElemStep = GetElements.ByAttribute.nameAndValue("title", " nächste Seite ").stepName("get-next-page-elem");
        // TODO here there are duplicates becase bellow the instances are mutated ... change this so that each call below in the sequence
        //  creates a new instance based on the previous one and only then it sets values ....
        GetElementsByCssClass getProductTdElemsStep = GetElements.ByCssClass.className("main").stepName("get-product-elems"); // TODO add by tag ... filtering
        GetElementsByCssClass getProductCodeElemStep = GetElements.ByCssClass.className("PRODUCTS_NAME").stepName("get-product-code-elem-1");
        GetElementsByCssClass getProductCodeElemStep2 = GetElements.ByCssClass.className("PRODUCTS_NAME").stepName("get-product-code-elem-2");
        GetElementsByCssClass getProductPriceElemStep = GetElements.ByCssClass.className("prod_preis").stepName("get-product-price-elem");
        GetElementsByAttribute getProductDetailHRefElemStep = GetElements.ByAttribute.nameAndValue("href", "product_info.php/info").setMatchEntireValue(false).stepName("get-product-detail-elem");
        FollowLink clickNextPageLinkElem = Actions.followLink().stepName("click-next-page-button");
        GetElementsByCssClass getNavigationPositionElemStep = GetElements.ByCssClass.className("headerlinks").stepName("headerlinks").stepName("get-nav-position-elem-step");
        GetElementsByAttribute getProductDetailTitleElem = GetElements.ByAttribute.nameAndValue("itemprop", "name");
        GetElementsByAttribute getProductDescriptionElem = GetElements.ByAttribute.nameAndValue("id", "c0");

        final Scraping productsScraping = new Scraping(new HtmlUnitSiteParser(driverManager))
                .setParsingSequence(
                        GetElements.ByTag.body()
                                .setCollector(ProductsPage::new, ProductsPages::new, ProductsPages::add)
                                .next(new EmptyStep().stepName("before-pagination"))
                                .next(Actions.paginate()
                                        .setStepsLoadingNextPage(
                                                getNextPageLinkElemStep
                                                        .next(clickNextPageLinkElem
                                                                .next(Actions.returnNextPage())
                                                        )
                                        )
                                        .nextForEachPageExclusively(
                                                StepFlow.asStepGroup() // no steps with higher order than this very step can be allowed to run before this whole thing finishes ... lower steps can continue running ... send an event that this step finished so normal parallelism can resume ... also there might be nested ordered steps ...
                                                        .next(getNavigationPositionElemStep
                                                                .next(Parse.textContent().stepName("pet-1")
                                                                        .setCollector(ProductsPage::setPosition)
                                                                )
                                                        )
                                                        .next(getNavigationPositionElemStep
                                                                .next(Parse.textContent().stepName("pet-1")
                                                                        .setCollector(ProductsPage::setPosition)
                                                                )
                                                        )
                                        )
                                        .nextForEachPage(getNavigationPositionElemStep
                                                .next(Parse.textContent().stepName("pet-1")
                                                        .setCollector(ProductsPage::setPosition)
                                                )
                                        )
                                        .nextForEachPage(getProductTdElemsStep
                                                .setCollector(Product::new, ProductsPage::add, new ProductListenerParsed())// this step generates the Product model and does not modify it ... all the child steps need to finish
                                                // this can register the step order of this parent step with NotificationOrderingService ...
                                                // set parsed data listener ?
                                                //  ... we might wanna listen for simple String values that were parsed ... not just complex models ...
                                                // when should data be eligible to notify listeners?
                                                .next(getProductCodeElemStep
                                                        .next(new EmptyStep().stepName("before-product-code-collection")
                                                                .setCollector(ProductCode::new, Product::setProductCode)
                                                                .next(Parse.textContent().stepName("pet-2").setCollector(ProductCode::setValue))
                                                        )
                                                )
                                                .next(getProductPriceElemStep
                                                        .next(Parse.textContent().setCollector(Product::setPrice))
                                                )
                                                .next(getProductCodeElemStep2
                                                        .next(Parse.hRef(hrefVal -> "https://www.teleskop-express.de/shop/" + hrefVal).stepName("parse-product-href")
                                                                .setCollector(Product::setDetailUrl)
                                                                .nextNavigate(Actions.navigateToParsedLink(new HtmlUnitSiteParser(driverManager))
                                                                        .next(getProductDetailTitleElem.stepName("get-product-detail-title") // TODO perhaps we need a setParsingSequence method after all instead of then() here ... so that we are consistent with how we set up a SiteParse (allows only 1 sequence) ....
                                                                                .next(Parse.textContent().setCollector(Product::setTitle))
                                                                        )
                                                                        .next(getProductDescriptionElem
                                                                                .next(Parse.textContent().setCollector(Product::setDescription))
                                                                        )
                                                                        .next(GetElements.ByAttribute.nameAndValue("id", "MwStInfoMO")
                                                                                .next(GetElements.ByTag.anchor()
                                                                                        .next(Parse.hRef(hrefVal -> "https://www.teleskop-express.de/shop/" + hrefVal)
                                                                                                .nextNavigate(Actions.navigateToParsedLink(new HtmlUnitSiteParser(driverManager))
                                                                                                        .next(GetListedElementsByFirstElementXPath.instance("/html/body/table/tbody/tr[1]")
                                                                                                                .setCollector(ShippingCosts::new, (Product p, ShippingCosts sc) -> p.getShippingCosts().add(sc))
                                                                                                                .next(GetListedElementByFirstElementXPath.instance("/html/body/table/tbody/tr[1]/td[1]")
                                                                                                                        .next(Parse.textContent().stepName("get-shipping-service").setCollector(ShippingCosts::setService))
                                                                                                                )
                                                                                                                .next(GetListedElementByFirstElementXPath.instance("/html/body/table/tbody/tr[1]/td[2]")
                                                                                                                        .next(Parse.textContent().stepName("get-shipping-price").setCollector(ShippingCosts::setPrice))
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

        scraper.awaitCompletion(Duration.ofSeconds(200)); // TODO await completion ...

    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class ProductsPages {
        private final List<ProductsPage> pageList = new ArrayList<>();

        public void add(ProductsPage products) {
            this.pageList.add(products);
        }
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
    public static class ProductListenerParsed implements ParsedDataListener<Product> {

        @Override
        public void onParsingFinished(Product data) {
            log.info(JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

}
