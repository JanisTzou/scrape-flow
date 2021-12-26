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
import com.github.web.scraping.lib.scraping.htmlunit.*;
import com.github.web.scraping.lib.utils.JsonUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.web.scraping.lib.scraping.htmlunit.HtmlUnit.*;

@Log4j2
public class ZakonyProLidiCzDemo {

    public static final String HTTPS_WWW_ZAKONYPROLIDI_CZ = "https://www.zakonyprolidi.cz";

    private static void printLine() {
        log.info("-".repeat(200));
    }

    @Test
    public void start() throws InterruptedException {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());
        final HtmlUnitSiteParser parser = new HtmlUnitSiteParser(driverManager);

        final Scraping productsScraping = new Scraping(parser, 3)
                .setScrapingSequence(
                        Get.Descendants.ByAttribute.id("__Page")
                                .next(Get.Descendants.ByCss.byClassName("Name")
                                        .setCollector(Kategorie::new, Kategorie.class)
                                        .next(Parse.textContent().collectOne(Kategorie::setJmeno, Kategorie.class))
                                        .next(Parse.hRef(href -> HTTPS_WWW_ZAKONYPROLIDI_CZ + href)
                                                .collectOne(Kategorie::setUrl, Kategorie.class)
                                                .nextNavigate(toKategorie(parser)
                                                )
                                        )
                                )

                );


        start(productsScraping);
    }

    private NavigateToParsedLink toKategorie(HtmlUnitSiteParser parser) {
        return Do.navigateToParsedLink(parser)
                .next(Get.Descendants.ByCss.byClassName("BranchNodes")
                        .getFirst() // the first section
                        .next(Get.nthChildElem(2) // subcategory list is 2nd DIV
                                // TODO it 2nd child does not exist then do something else ... add special handling for Koronavirus ...
                                .next(Get.Descendants.ByTag.anchor()
//                                        .getFirst() // TODO remove ...
                                        .setCollector(PodKategorie::new, PodKategorie.class, new PodKategorieListener())
                                        .next(Parse.textContent().collectOne(PodKategorie::setJmeno, PodKategorie.class))
                                        .next(Parse.hRef(href -> HTTPS_WWW_ZAKONYPROLIDI_CZ + href)
                                                .collectOne(PodKategorie::setUrl, PodKategorie.class)
                                                .nextNavigate(toPredpisy(parser)
                                                )
                                        )
                                )

                        )
                );
    }

//                        Actions.paginate()
//                         NOT WORKING .... required JS ...
//                        .setStepsLoadingNextPage(
//                                GetElements.Descendants.ByAttribute.nameAndValue("title", "Jdi na Další")
//                                        .next(Actions.filterElements(domNode -> !HtmlUnitUtils.hasAttributeWithValue(domNode, "class", "disabled", true))
//                                                .next(Actions.followLink()
//                                                        .next(Actions.returnNextPage())
//                                                )
//                                        )
//
//                        )

    private NavigateToParsedLink toPredpisy(HtmlUnitSiteParser parser) {
        return Do.navigateToParsedLink(parser)
                .next(Get.Descendants.ByCss.byClassName("DocGrid")
                        .getFirst()
                        .next(Get.Descendants.ByTag.tbody()
                                .next(Get.Descendants.ByTag.tr()
                                        .setCollector(PredpisInfo::new, PredpisInfo.class)
                                        .setCollector(Predpis::new, Predpis.class)
                                        .collectOne(Predpis::setInfo, Predpis.class, PredpisInfo.class)
                                        .collectOne(PredpisInfo::setKategorie, PredpisInfo.class, Kategorie.class)
                                        .collectOne(PredpisInfo::setPodKategorie, PredpisInfo.class, PodKategorie.class)
                                        .next(Get.Descendants.ByCss.byClassName("c1")
                                                .next(Get.Descendants.ByTag.anchor()
                                                        .next(Parse.hRef(href -> HTTPS_WWW_ZAKONYPROLIDI_CZ + href)
                                                                .collectOne(PredpisInfo::setUrl, PredpisInfo.class)
//                                                                        .nextNavigate(toPredpisDetail(parser) // TODO uncomment ...
//                                                                        )
                                                        )
                                                )
                                                .next(Parse.textContent().collectOne(PredpisInfo::setCislo, PredpisInfo.class))
                                        )
                                        .next(Get.Descendants.ByCss.byClassName("c2")
                                                .next(Parse.textContent()
                                                        .collectOne(PredpisInfo::setNazev, PredpisInfo.class)
                                                )
                                        )
                                        .next(Get.Descendants.ByCss.byClassName("c3")
                                                .next(Parse.textContent()
                                                        .collectOne(PredpisInfo::setPlatnostOd, PredpisInfo.class)
                                                )
                                        )
                                )
                        )
        );
    }


    private NavigateToParsedLink toPredpisDetail(HtmlUnitSiteParser parser) {
        return Do.navigateToParsedLink(parser)
                .next(Get.Descendants.ByCss.byClassName("Frags")
                        .next(Get.childElems()
                                        .setCollector(Radek::new, Radek.class)
                                        .collectMany((Predpis p, Radek r) -> p.getText().add(r), Predpis.class, Radek.class)
                                        .next(Parse.textContent()
                                                .collectOne(Radek::setText, Radek.class)
                                        )
                                // TODO add css class parsing ... to the model ...
                        )
                );
    }

    private void start(Scraping productsScraping) throws InterruptedException {
        final EntryPoint entryPoint = new EntryPoint("https://www.zakonyprolidi.cz/obory", productsScraping);
        final Scraper scraper = new Scraper();
        scraper.scrape(entryPoint);

        scraper.awaitCompletion(Duration.ofMinutes(5));
        Thread.sleep(2000); // let logging finish ...
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Kategorie {
        private volatile String jmeno;
        private volatile String url;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class PodKategorie {
        private volatile String jmeno;
        private volatile String url;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class PredpisInfo {
        private volatile Kategorie kategorie;
        private volatile PodKategorie podKategorie;
        private volatile String url;
        private volatile String cislo;
        private volatile String nazev;
        private volatile String platnostOd;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Predpis {
        private volatile PredpisInfo info;
        private volatile List<Radek> text = Collections.synchronizedList(new ArrayList<>());
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Radek {
        // TODO css ...
        private volatile String text;
    }

    @Log4j2
    public static class KategorieListener implements ParsedDataListener<Kategorie> {
        @Override
        public void onParsingFinished(Kategorie data) {
            printLine();
            log.info(JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
            printLine();
        }
    }

    @Log4j2
    public static class PodKategorieListener implements ParsedDataListener<PodKategorie> {
        @Override
        public void onParsingFinished(PodKategorie data) {
            printLine();
            log.info(JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
            printLine();
        }
    }

    @Log4j2
    public static class PredpisInfoListener implements ParsedDataListener<PredpisInfo> {
        @Override
        public void onParsingFinished(PredpisInfo data) {
            printLine();
            log.info(JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
            printLine();
        }
    }

    @Log4j2
    public static class PredpisListener implements ParsedDataListener<Predpis> {
        @Override
        public void onParsingFinished(Predpis data) {
            printLine();
            log.info(JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
            printLine();
        }
    }

}
