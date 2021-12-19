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
import com.github.web.scraping.lib.dom.data.parsing.HtmlUnitSiteParser;
import com.github.web.scraping.lib.dom.data.parsing.steps.*;
import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.junit.Test;

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
        GetElementsByCssClass getProductCodeElemStep3 = GetElementsByCssClass.instance("PRODUCTS_NAME").setName("get-product-code-elem-3");
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
                                                .setPaginationTrigger(getNextPageLinkElemStep.then(clickNextPageLinkElem))
                                                .thenForEachPage(getNavigationPositionElemStep
                                                        .then(new ParseElementText().setName("pet-1")
                                                                .excludeChildElements(false)
                                                                .setCollector(ProductsPage::setPosition)
                                                        )
                                                )
                                                .thenForEachPage(getProductTdElemsStep
                                                        .setCollector(Product::new, ProductsPage::add)
                                                        .then(getProductCodeElemStep
                                                                .then(new EmptyStep()
                                                                        .setCollector(ProductCode::new, Product::setProductCode)
                                                                        .then(new ParseElementText().setName("pet-2").setCollector(ProductCode::setValue))
                                                                )
                                                        )
                                                        .then(getProductCodeElemStep2 // this needs to be new instance ... throws exception otherwise ...
                                                                .then(new ParseElementText().setCollector(Product::setCode))
                                                        )
                                                        .then(getProductPriceElemStep
                                                                .then(new ParseElementText().setCollector(Product::setPrice))
                                                        )
                                                        .then(getProductCodeElemStep3
                                                                .then(ParseElementHRef.instance()
                                                                        .setTransformation(hrefVal -> "https://www.teleskop-express.de/shop/" + hrefVal)
                                                                        .setCollector(Product::setDetailUrl)
                                                                        .thenNavigate(new NavigateToNewSite()
                                                                                .setSiteParser(new HtmlUnitSiteParser(driverManager))
                                                                                .then(getProductDetailTitleElem // TODO perhaps we need a setParsingSequence method after all instead of then() here ... so that we are consistent with how we set up a SiteParse (allows only 1 sequence) ....
                                                                                        .then(new ParseElementText().setCollector(Product::setTitle))
                                                                                )
                                                                                .then(getProductDescriptionElem
                                                                                        .then(new ParseElementText().setCollector(Product::setDescription))
                                                                                )
                                                                                .then(GetElementsByAttribute.instance("id", "MwStInfoMO")
                                                                                        .then(GetElementsByTag.instance("a")
                                                                                                .then(ParseElementHRef.instance()
                                                                                                        .setTransformation(hrefVal -> "https://www.teleskop-express.de/shop/" + hrefVal)
                                                                                                        .thenNavigate(new NavigateToNewSite()
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
        final EntryPoint entryPoint = new EntryPoint("https://www.teleskop-express.de/shop/index.php/cat/c6_Eyepieces-1-25-inch-up-to-55--field.html/page/2", productsCrawling);
        final Crawler crawler = new Crawler();

        crawler.scrape(entryPoint);

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
        private String position;

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
        private String title;
        private String code;
        private String price;
        private ProductCode productCode;
        private String detailUrl;
        private String description;
        private List<ShippingCosts> shippingCosts = new ArrayList<>();
    }

    @Setter
    @Getter
    @ToString
    public static class ProductCode {
        private String value;
    }

    @Setter
    @Getter
    @ToString
    public static class ShippingCosts {
        private String service;
        private String price;
    }

}
