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
import com.github.web.scraping.lib.demos.models.Product;
import com.github.web.scraping.lib.demos.models.Products;
import com.github.web.scraping.lib.dom.data.parsing.HtmlUnitSiteParser;
import com.github.web.scraping.lib.dom.data.parsing.steps.*;
import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;

import static com.github.web.scraping.lib.demos.TeleskopExpressDeCrawler.Identifiers.*;

public class TeleskopExpressDeCrawler {

    public void start() {

        // TODO any way for these to be accessible globally? So they do not need to be specified explicitly in every stage definition?
        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        // TODO the parsing/scraping steps should be better named so it is clear what action they perform ... it might not be parsing exacly but also actions like button clicks etc ...
        //  maybe it is ok to have a "parsing ste" that is not exacly parsing enything but performing an action ... it's just something that needs to be performed to do the actual parsing ...

        // TODO consider not using builders at all ...
        GetElementsByAttribute.Builder getNextBtnLinkElemStep = GetElementsByAttribute.builder("title", " nächste Seite ");
        GetElementsByCssClass.Builder getProductTdElemsStep = GetElementsByCssClass.builder("main"); // TODO add by tag ... filtering
        GetElementsByCssClass.Builder getProductTitleElemStep = GetElementsByCssClass.builder("PRODUCTS_NAME");
        GetElementsByCssClass.Builder getProductTitleElemStep2 = GetElementsByCssClass.builder("PRODUCTS_NAME");
        GetElementsByCssClass.Builder getProductPriceElemStep = GetElementsByCssClass.builder("prod_preis");
        GetElementsByAttribute.Builder getProductDetailHRefElemStep = GetElementsByAttribute.builder("href", "product_info.php/info").setMatchEntireValue(false);
        ClickElement clickNextPageBtnElem = ClickElement.builder().build();

        // TODO the next (then(...)) operations should be decoupled from the individual parsing steps... they should just decorate them !

        final CrawlingStage.Builder productListStage = CrawlingStage.builder()
                .setParser(HtmlUnitSiteParser.builder(driverManager)
                        .setPaginatingSequence(getNextBtnLinkElemStep
                                .then(clickNextPageBtnElem)
                                .build())
                        // TODO have top level collector here ? Or make it a list as a default and not worry about it ?
                        //  it will probably be needed ... pagination produces multiople instances of containers that should only be one instance ...
                        .addParsingSequence(getProductTdElemsStep
                                // TODO express somehow that the next operation involves collection of elemets? ... collectors would then make more sense ...
                                .collector(Products::new, Product::new, Products::add)  // TODO how to add connection to existing container?
                                .then(getProductTitleElemStep
                                        .then(ParseElementText.builder()
                                                .collectToModel(Product::setCode)
                                                .build())
                                        .build()
                                )
                                .then(getProductPriceElemStep
                                        .then(ParseElementText.builder()
                                                .collectToModel(Product::setPrice)
                                                .build())
                                        .build()
                                )
                                .then(getProductTitleElemStep2   // TODO perhaps a better abstraction of what is "then" and what is "next to"
                                        .then(ParseElementHRef.builder(PRODUCT_DETAIL_LINK).build())
                                        .build()
                                )
                                .build()
                        )
                        .build()
                );

//        List.of().stream().collect(null, null, null);

        GetElementsByAttribute.Builder getProductDetailTitle = GetElementsByAttribute.builder("itemprop", "name");


        final CrawlingStage productDetailStage = CrawlingStage.builder()
                .setReferenceForParsedHrefToCrawl(PRODUCT_DETAIL_LINK, hrefVal -> "https://www.teleskop-express.de/" + hrefVal)
                .setParser(HtmlUnitSiteParser.builder(driverManager)
                        .addParsingSequence(getProductDetailTitle
                                .then(getProductTitleElemStep
                                        .then(ParseElementText.builder(PRODUCT_TITLE).build())
                                        .build()
                                )
                                .build()
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
        PRODUCT_CODE,
        PRODUCT_TITLE,
        PRODUCT_PRICE,
        NEXT_BTN_LINK,
        PRODUCT_DETAIL_LINK
    }

}
