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
import com.github.web.scraping.lib.dom.data.parsing.steps.HtmlUnitSiteParser;
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

        GetElementsByAttribute getNextPageLinkElemStep = GetElementsByAttribute.instance("title", " nächste Seite ").setName("get-next-page-elem");
        // TODO here there are duplicates becase bellow the instances are mutated ... change this so that each call below in the sequence
        //  creates a new instance based on the previous one and only then it sets values ....
        GetElementsByCssClass getProductTdElemsStep = GetElementsByCssClass.instance("main").setName("get-product-elems"); // TODO add by tag ... filtering
        GetElementsByCssClass getProductCodeElemStep = GetElementsByCssClass.instance("PRODUCTS_NAME").setName("get-product-code-elem-1");
        GetElementsByCssClass getProductCodeElemStep2 = GetElementsByCssClass.instance("PRODUCTS_NAME").setName("get-product-code-elem-2");
        GetElementsByCssClass getProductPriceElemStep = GetElementsByCssClass.instance("prod_preis").setName("get-product-price-elem");
        GetElementsByAttribute getProductDetailHRefElemStep = GetElementsByAttribute.instance("href", "product_info.php/info").setMatchEntireValue(false).setName("get-product-detail-elem");
        ClickElement clickNextPageLinkElem = ClickElement.instance().setName("click-next-page-button");
        GetElementsByCssClass getNavigationPositionElemStep = GetElementsByCssClass.instance("headerlinks").setName("headerlinks").setName("get-nav-position-elem-step");
        GetElementsByAttribute getProductDetailTitleElem = GetElementsByAttribute.instance("itemprop", "name");
        GetElementsByAttribute getProductDescriptionElem = GetElementsByAttribute.instance("id", "c0");

        final Crawling productsCrawling = new Crawling()
                .setSiteParser(new HtmlUnitSiteParser(driverManager)
                        // step set root model / collector ... here somewhere ... ? ????
                        .setParsingSequence(
                                new GetHtmlBody()
                                        .setCollector(ProductsPage::new, ProductsPages::new, ProductsPages::add)
                                        .then(new EmptyStep().setName("before-pagination"))
                                        .then(new Paginate()
                                                .setPaginationTrigger(
                                                        getNextPageLinkElemStep
                                                                .then(clickNextPageLinkElem)
                                                )
                                                .thenForEachPage(getNavigationPositionElemStep
                                                        .then(new ParseElementText().setName("pet-1")
                                                                .excludeChildElements(false)
                                                                .setCollector(ProductsPage::setPosition)
                                                        )
                                                )
                                                .thenForEachPage(getProductTdElemsStep
                                                        .setCollector(Product::new, ProductsPage::add, new ProductListenerParsed())// this step generates the Product model and does not modify it ... all the child steps need to finish
                                                        // this can register the step order of this parent step with NotificationOrderingService ...
                                                        // set parsed data listener ?
                                                        //  ... we might wanna listen for simple String values that were parsed ... not just complex models ...
                                                        // when should data be eligible to notify listeners?
                                                        .then(getProductCodeElemStep
                                                                .then(new EmptyStep().setName("before-product-code-collection")
                                                                        .setCollector(ProductCode::new, Product::setProductCode)
                                                                        .then(new ParseElementText().setName("pet-2").setCollector(ProductCode::setValue))
                                                                )
                                                        )
                                                        .then(getProductPriceElemStep
                                                                .then(new ParseElementText().setCollector(Product::setPrice))
                                                        )
                                                        .then(getProductCodeElemStep2
                                                                .then(ParseElementHRef.instance().setName("parse-product-href")
                                                                        .setTransformation(hrefVal -> "https://www.teleskop-express.de/shop/" + hrefVal)
                                                                        .setCollector(Product::setDetailUrl)
                                                                        .thenNavigate(new NavigateToParsedHRef()
                                                                                .setSiteParser(new HtmlUnitSiteParser(driverManager))
                                                                                .then(getProductDetailTitleElem.setName("get-product-detail-title") // TODO perhaps we need a setParsingSequence method after all instead of then() here ... so that we are consistent with how we set up a SiteParse (allows only 1 sequence) ....
                                                                                        .then(new ParseElementText().setCollector(Product::setTitle))
                                                                                )
                                                                                .then(getProductDescriptionElem
                                                                                        .then(new ParseElementText().setCollector(Product::setDescription))
                                                                                )
                                                                                .then(GetElementsByAttribute.instance("id", "MwStInfoMO")
                                                                                        .then(GetElementsByTag.instance("a")
                                                                                                .then(ParseElementHRef.instance()
                                                                                                        .setTransformation(hrefVal -> "https://www.teleskop-express.de/shop/" + hrefVal)
                                                                                                        .thenNavigate(new NavigateToParsedHRef()
                                                                                                                .setSiteParser(new HtmlUnitSiteParser(driverManager))
                                                                                                                .then(GetListedElementsByFirstElementXPath.instance("/html/body/table/tbody/tr[1]")
                                                                                                                        .setCollector(ShippingCosts::new, (Product p, ShippingCosts sc) -> p.getShippingCosts().add(sc))
                                                                                                                        .then(GetListedElementByFirstElementXPath.instance("/html/body/table/tbody/tr[1]/td[1]")
                                                                                                                                .then(new ParseElementText().setName("get-shipping-service").setCollector(ShippingCosts::setService))
                                                                                                                        )
                                                                                                                        .then(GetListedElementByFirstElementXPath.instance("/html/body/table/tbody/tr[1]/td[2]")
                                                                                                                                .then(new ParseElementText().setName("get-shipping-price").setCollector(ShippingCosts::setPrice))
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
                        )
                );


        // TODO maybe the entry url should be part of the first scraping stage? And we can have something like "FirstScrapingStage) ... or maybe entry point abstraction is good enough ?

        String url = "https://www.teleskop-express.de/shop/index.php/cat/c6_Eyepieces-1-25-inch-up-to-55--field.html/page/2";
//        String url = "https://www.teleskop-express.de/shop/index.php/cat/c6_Eyepieces-1-25-inch-up-to-55--field.html";
        final EntryPoint entryPoint = new EntryPoint(url, productsCrawling);
        final Crawler crawler = new Crawler();

        crawler.scrape(entryPoint);

        crawler.awaitCompletion(Duration.ofSeconds(200)); // TODO await completion ...

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
