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
import com.github.web.scraping.lib.CrawlingStage;
import com.github.web.scraping.lib.EntryPoint;
import com.github.web.scraping.lib.dom.data.parsing.HtmlUnitSiteParser;
import com.github.web.scraping.lib.dom.data.parsing.steps.*;
import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static com.github.web.scraping.lib.demos.TeleskopExpressDeCrawler.Identifiers.PRODUCT_DETAIL_LINK;

public class TeleskopExpressDeCrawler {

    public void start() {

        // TODO suppot something like temporary models? e.g. for communication of multiple pieces of data downstream so that an action/step can be executed ...

        // TODO any way for these to be accessible globally? So they do not need to be specified explicitly in every stage definition?
        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        // TODO the parsing/scraping steps should be better named so it is clear what action they perform ... it might not be parsing exacly but also actions like button clicks etc ...
        //  maybe it is ok to have a "parsing ste" that is not exacly parsing enything but performing an action ... it's just something that needs to be performed to do the actual parsing ...

        // TODO consider not using builders at all ...
        GetElementsByAttribute getNextBtnLinkElemStep = GetElementsByAttribute.instance("title", " nächste Seite ");
        // TODO here there are duplicates becase bellow the instances are mutated ... change this so that each call below in the sequence
        //  creates a new instance based on the previous one and only then it sets values ....
        GetElementsByCssClass getProductTdElemsStep = GetElementsByCssClass.instance("main"); // TODO add by tag ... filtering
        GetElementsByCssClass getProductCodeElemStep = GetElementsByCssClass.instance("PRODUCTS_NAME");
        GetElementsByCssClass getProductCodeElemStep2 = GetElementsByCssClass.instance("PRODUCTS_NAME");
        GetElementsByCssClass getProductTitleElemStep2 = GetElementsByCssClass.instance("PRODUCTS_NAME");
        GetElementsByCssClass getProductPriceElemStep = GetElementsByCssClass.instance("prod_preis");
        GetElementsByAttribute getProductDetailHRefElemStep = GetElementsByAttribute.instance("href", "product_info.php/info").setMatchEntireValue(false);
        ClickElement clickNextPageBtnElem = ClickElement.instance();

        // TODO the next (then(...)) operations should be decoupled from the individual parsing steps... they should just decorate them !

        final CrawlingStage.Builder productListStage = CrawlingStage.builder()
                .setParser(HtmlUnitSiteParser.builder(driverManager)
                        .setPaginatingSequence(getNextBtnLinkElemStep
                                .then(clickNextPageBtnElem))
                        // TODO have top level collector here ? Or make it a list as a default and not worry about it ?
                        //  it will probably be needed ... pagination produces multiple instances of containers that should only be one instance ...
                        .addParsingSequence(
                                getProductTdElemsStep           // TODO express somehow that the next operation involves collection of elements? ... collectors would then make more sense ...
                                        .collector(Product::new, Products::new, Products::add)
                                        .then(getProductCodeElemStep
                                                .then(new EmptyStep()
                                                        .collector(ProductCode::new, Product::setProductCode)
                                                        .then(new ParseElementText().thenCollectToModel(ProductCode::setValue)
                                                                .then(null)) // TODO sanitise, tranform ... scrape something else based on this ...
                                                )
                                        )
                                        .then(getProductCodeElemStep2 // this needs to be new instance ... throws exception otherwise ...
                                                .then(new ParseElementText().thenCollectToModel(Product::setCode))
                                        )
                                        .then(getProductPriceElemStep
                                                .then(new ParseElementText().thenCollectToModel(Product::setPrice))
                                        )
                                        .then(getProductTitleElemStep2
                                                .then(ParseElementHRef.instance(PRODUCT_DETAIL_LINK))
                                        )
                        )
                        .build()
                );

        GetElementsByAttribute getProductDetailTitle = GetElementsByAttribute.instance("itemprop", "name");

        final CrawlingStage productDetailStage = CrawlingStage.builder()
                .setReferenceForParsedHrefToCrawl(PRODUCT_DETAIL_LINK, hrefVal -> "https://www.teleskop-express.de/" + hrefVal)
                .setParser(HtmlUnitSiteParser.builder(driverManager)
                        .addParsingSequence(getProductDetailTitle
                                .then(getProductCodeElemStep
                                        .then(new ParseElementText())
                                )
                        )
                        .build()
                )
                .build();


        final CrawlingStage allCrawling = productListStage
//                .addNextStage(productDetailStage)
                .build();

        // TODO maybe the entry url should be part of the first scraping stage? And we can have something like "FirstScrapingStage) ... or maybe entry point abstraction is good enough ?
        final EntryPoint entryPoint = new EntryPoint("https://www.teleskop-express.de/shop/index.php/cat/c6_Eyepieces-1-25-inch-up-to-55--field.html", allCrawling);

        final Crawler crawler = new Crawler();

        crawler.scrape(entryPoint);

    }


    public enum Identifiers {
        PRODUCT_DETAIL_LINK
    }


    @Getter
    @NoArgsConstructor
    @ToString
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
    }

    @Setter
    @Getter
    @ToString
    public static class ProductCode {
        private String value;
    }

}
