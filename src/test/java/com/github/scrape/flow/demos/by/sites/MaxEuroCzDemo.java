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

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnit.*;

public class MaxEuroCzDemo {

    @Ignore
    @Test
    public void start() throws InterruptedException {

        final Scraping scraping = new Scraping(1, TimeUnit.SECONDS);


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

        scraping.setSequence( // TODO here we are passing a static site sequence ... but the parser is defined elsewhere ... how to deal with that?
                Do.navigateTo("https://www.maxeuro.cz/obklady-dlazby-mozaika-kat_1010.html")
                        .next(Get.descendants().byTextContent("Mozaika skleněná")
                                .first() // ... for some reason the menu is duplicated
                                .debugOptions().logFoundElementsSource(false)
                                .next(Get.natively(domNode -> Optional.ofNullable(domNode.getParentNode()))
                                        .next(Get.descendants().byTag("a")
                                                .addCollector(Category::new, Category.class)
                                                .next(Parse.textContent()
                                                        .collectValue(Category::setName, Category.class)
                                                )
                                                .next(Parse.hRef(href -> "https://www.maxeuro.cz" + href)
                                                        .next(toCategoryProductList()) // TODO uncomment
//                                                        .next(seleniumProductScraping())
                                                )
                                        )
                                )
                        )

        );

        start(scraping);
    }

    private SeleniumNavigateToParsedLink seleniumProductScraping() {
        return Selenium.Do.navigateToParsedLink()
                .next(Selenium.Get.descendants()
                        .byTag("div")
                        .byClass("product-name")
                        .next(Selenium.Get.descendants().byTag("a")
                                .addCollector(Product::new, Product.class, new ProductListenerScraped())
                                .next(Selenium.Parse.hRef()
                                        .collectValue(Product::setDetailUrl, Product.class)
                                        .next(Selenium.Do.navigateToParsedLink()
                                                .next(Selenium.Get.descendants().byClass("kratky-popis")
                                                        .next(Selenium.Parse.textContent()
                                                                .collectValue(Product::setDescription, Product.class)
                                                        )
                                                )
                                        )
                                )
                        )
                );
    }

    private HtmlUnitNavigateToParsedLink toCategoryProductList() {
        return Do.navigateToParsedLink()
                .next(Flow.withPagination()
                        .setStepsLoadingNextPage(
                                getPaginatingSequence()
                        )
                        .next( // TODO this could be overloaded and ensured that all next steps are wrapped under exclusive block ...
                                getProductListAndDetails()
                        )
                );
    }

    private HtmlUnitGetDescendants getProductListAndDetails() {

        /*
            <div class="product col-xs-12 col-sm-6 col-md-4 col-lg-4 ">
                <div class="row">
                    ...
                    <div class="col-xs-6 product-ost">
                        <div class="product-name">
                            <a href="/mozaika-35520-sklenena-zluta-29-7x29-7cm-sklo-d_82669.html">Mozaika 35520 skleněná žlutá
                                29,7x29,7cm sklo</a>
                            <p class="product-anotace">mozaika obklady do kuchyně a koupelny</p>
                        </div>
                        <div class="cena">63,00&nbsp;Kč<br>(ks)</div>
                    </div>
                </div>
            </div>
         */

        return Get.descendants().byClass("product").stepName("product-search")
                .addCollector(Product::new, Product.class, new ProductListenerScraped())
                .collectValue(Product::setCategory, Product.class, Category.class)
                .next(Get.descendants().byClass("product-name")
                        .next(Get.descendants().byTag("a")
                                .next(Parse.textContent()
                                        .collectValue(Product::setName, Product.class)
                                )
                                .next(Parse.hRef(href -> "https://www.maxeuro.cz" + href).stepName("get-product-detail-url")
                                        .collectValue(Product::setDetailUrl, Product.class)
                                        .next(
                                                toProductDetail()
                                        )
                                )
                        )
                )
                .next(Get.descendants().byClass("cena")
                        .next(Parse.textContent(txt -> txt.replace(" ", "").replace("Kč(m2)", "").replace(",", ".").replace("Kč(bm)", ""))
                                .collectValue(Product::setPrice, Product.class)
                        )
                );
    }


    private HtmlUnitGetDescendants getPaginatingSequence() {

        /*
            <ul class="pagination">
                <li class="disabled">
                    <a href="/mozaika-sklenena-kat_109.html?vp-page=0">«</a>
                </li>
                <li class="active">
                    <a href="/mozaika-sklenena-kat_109.html?vp-page=1">1</a>
                </li>
                <li>
                    <a href="/mozaika-sklenena-kat_109.html?vp-page=2">2</a>
                </li>
                <li>
                    <a href="/mozaika-sklenena-kat_109.html?vp-page=3">3</a>
                </li>
                <li>
                    <a href="/mozaika-sklenena-kat_109.html?vp-page=2">»</a>
                </li>
            </ul>
         */

        return Get.descendants().byClass("pagination").stepName("product-pagination")
                .next(Get.descendants().byTextContent("»")  // returns anchor
                        .next(Filter.natively(domNode -> !isDisabled(domNode))
                                .next(Do.followLink()
                                        .next(Flow.returnNextPage())
                                )
                        )
                );
    }

    private boolean isDisabled(DomNode domNode) {
        return HtmlUnitUtils.hasAttributeWithExactValue(domNode.getParentNode(), "class", "disabled");
    }


    private HtmlUnitNavigateToParsedLink toProductDetail() {

        /*
            <div class="text-justify" id="productDescription1">Mozaika skleněná žlutá obkladová - 144ks skleněných čtverečků<br>Obkladová
                mozaika skleněná má celou škálu možností užití v interiéru. Od dekorativních obkladů celých stěn, částí
                interiérů až po koupelny a obklady bazénu. Obkladová mozaika je vyrobená z kvalitního skla, vodě odolná.<br>Rozměr:
                29,7 x 29,7cm<br>Rozměr čtverečků mozaiky 2,3 x 2,3 cm<br>Tloušťka: 0,4 cm<br>Barevnost: žlutá<br>Povrch: lesklý<br>Mrazuvzdornost:
                mrazuvzdorné<br>Uvedená cena je za 1ks o rozměru 29,7 x 29,7cm.<br>Barevnost skleněné mozaiky je pouze
                přibližná. Záleží vždy na individuálním nastavení monitoru.
            </div>
         */

        return Do.navigateToParsedLink()
                .next(Get.descendants().byAttr("id", "productDescription1")
                        .next(Parse.textContent()
                                .collectValue(Product::setDescription, Product.class)
                        )
                );
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
