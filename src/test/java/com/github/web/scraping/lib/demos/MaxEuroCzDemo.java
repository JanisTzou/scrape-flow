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

import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;
import com.github.web.scraping.lib.parallelism.ParsedDataListener;
import com.github.web.scraping.lib.scraping.EntryPoint;
import com.github.web.scraping.lib.scraping.Scraper;
import com.github.web.scraping.lib.scraping.Scraping;
import com.github.web.scraping.lib.scraping.htmlunit.GetDescendants;
import com.github.web.scraping.lib.scraping.htmlunit.HtmlUnitSiteParser;
import com.github.web.scraping.lib.scraping.htmlunit.HtmlUnitUtils;
import com.github.web.scraping.lib.scraping.htmlunit.NavigateToParsedLink;
import com.github.web.scraping.lib.utils.JsonUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.github.web.scraping.lib.scraping.htmlunit.HtmlUnit.*;

public class MaxEuroCzDemo {

    @Test
    public void start() throws InterruptedException {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());
        final HtmlUnitSiteParser siteParser = new HtmlUnitSiteParser(driverManager);

        final Scraping scraping = new Scraping(siteParser, 10, TimeUnit.SECONDS);


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

        scraping.getDebugOptions().setOnlyScrapeFirstElements(true)
                .getDebugOptions().setLogFoundElementsSource(false)
                .getOptions().setRequestRetries(2);

        scraping.setSequence(
                Get.descendants().byTextContent("Mozaika skleněná")
                        .first()                                                // ... for some reason the menu is duplicated
                        .debugOptions().setLogFoundElementsSource(false)
                        .next(Do.mapElements(domNode -> Optional.ofNullable(domNode.getParentNode()))
                                .next(Get.descendants().byTag("a")
                                        .addCollector(Category::new, Category.class)
                                        .next(Parse.textContent()
                                                .collectOne(Category::setName, Category.class)
                                        )
                                        .next(Parse.hRef(href -> "https://www.maxeuro.cz" + href)
                                                .nextNavigate(
                                                        toCategoryProductList(siteParser)
                                                )
                                        )
                                )
                        )
        );


        start(scraping);
    }

    private NavigateToParsedLink toCategoryProductList(HtmlUnitSiteParser siteParser) {
        return Do.navigateToParsedLink(siteParser)
                .next(Do.paginate()
                        .setStepsLoadingNextPage(
                                getPaginatingSequence()
                        )
                        .next(getProductListAndDetails(siteParser)

                        )
                );
    }

    private GetDescendants getProductListAndDetails(HtmlUnitSiteParser siteParser) {

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
                .addCollector(Product::new, Product.class, new ProductListenerParsed())
                .collectOne(Product::setCategory, Product.class, Category.class)
                .next(Get.descendants().byClass("product-name")
                        .next(Get.descendants().byTag("a")
                                .next(Parse.textContent()
                                        .collectOne(Product::setName, Product.class)
                                )
                        )
                        .next(Get.descendants().byTag("a")
                                .next(Parse.hRef(href -> "https://www.maxeuro.cz" + href).stepName("get-product-detail-url")
                                        .collectOne(Product::setDetailUrl, Product.class)
                                        .nextNavigate(
                                                toProductDetail(siteParser)
                                        )
                                )
                        )
                )
                .next(Get.descendants().byClass("cena")
                        .next(Parse.textContent(txt -> txt.replace(" ", "").replace("Kč(m2)", "").replace(",", ".").replace("Kč(bm)", ""))
                                .collectOne(Product::setPrice, Product.class)
                        )
                );
    }


    private GetDescendants getPaginatingSequence() {

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

        return Get.descendants().byClass("pagination")
                .next(Get.descendants().byTextContent("»")  // returns anchor
                        .next(Filter.apply(domNode -> !HtmlUnitUtils.hasAttributeWithValue(domNode.getParentNode(), "class", "disabled", true))
                                .next(Do.followLink()
                                        .next(Do.returnNextPage())
                                )
                        )
                );
    }


    private NavigateToParsedLink toProductDetail(HtmlUnitSiteParser siteParser) {

        /*
            <div class="text-justify" id="productDescription1">Mozaika skleněná žlutá obkladová - 144ks skleněných čtverečků<br>Obkladová
                mozaika skleněná má celou škálu možností užití v interiéru. Od dekorativních obkladů celých stěn, částí
                interiérů až po koupelny a obklady bazénu. Obkladová mozaika je vyrobená z kvalitního skla, vodě odolná.<br>Rozměr:
                29,7 x 29,7cm<br>Rozměr čtverečků mozaiky 2,3 x 2,3 cm<br>Tloušťka: 0,4 cm<br>Barevnost: žlutá<br>Povrch: lesklý<br>Mrazuvzdornost:
                mrazuvzdorné<br>Uvedená cena je za 1ks o rozměru 29,7 x 29,7cm.<br>Barevnost skleněné mozaiky je pouze
                přibližná. Záleží vždy na individuálním nastavení monitoru.
            </div>
         */

        return Do.navigateToParsedLink(siteParser)
                .next(Get.descendants().byAttr("id", "productDescription1")
                        .next(Parse.textContent()
                                .collectOne(Product::setDescription, Product.class)
                        )
                );
    }


    private void start(Scraping productsScraping) throws InterruptedException {
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
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

}
