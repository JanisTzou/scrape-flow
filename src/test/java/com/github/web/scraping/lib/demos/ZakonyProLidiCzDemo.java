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
import java.util.concurrent.CopyOnWriteArrayList;

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
                        GetElements.Descendants.ByAttribute.id("__Page")
                                .next(GetElements.Descendants.ByCss.byClassName("Name")
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
        return Actions.navigateToParsedLink(parser)
                .next(GetElements.Descendants.ByCss.byClassName("BranchNodes") // TODO add special handling for Koronavirus ...
                        .next(GetElements.ByDomTraversal.nthChildElem(2) // the subcategory
                                .next(GetElements.Descendants.ByTag.anchor()
                                        .setCollector(PodKategorie::new, PodKategorie.class)
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

    private NavigateToParsedLink toPredpisy(HtmlUnitSiteParser parser) {
        return Actions.navigateToParsedLink(parser)
                .next(GetElements.Descendants.ByCss.byClassName("DocGrid")
                        .getFirst()
                        .next(GetElements.Descendants.ByTag.tbody()
                                .next(GetElements.Descendants.ByTag.tr()
                                        .getFirst()
                                        .setCollector(PredpisInfo::new, PredpisInfo.class)
                                        .setCollector(Predpis::new, Predpis.class, new PredpisListener())
                                        .collectOne(Predpis::setInfo, Predpis.class, PredpisInfo.class)
                                        .collectOne(PredpisInfo::setKategorie, PredpisInfo.class, Kategorie.class)
                                        .collectOne(PredpisInfo::setPodKategorie, PredpisInfo.class, PodKategorie.class)
                                        .next(GetElements.Descendants.ByCss.byClassName("c1")
                                                .next(GetElements.Descendants.ByTag.anchor()
                                                        .next(Parse.hRef(href -> HTTPS_WWW_ZAKONYPROLIDI_CZ + href)
                                                                .collectOne(PredpisInfo::setUrl, PredpisInfo.class)
                                                                .nextNavigate(toPredpisDetail(parser)
                                                                )
                                                        )
                                                )
                                                .next(Parse.textContent().collectOne(PredpisInfo::setCislo, PredpisInfo.class))
                                        )
                                        .next(GetElements.Descendants.ByCss.byClassName("c2")
                                                .next(Parse.textContent()
                                                        .collectOne(PredpisInfo::setNazev, PredpisInfo.class)
                                                )
                                        )
                                        .next(GetElements.Descendants.ByCss.byClassName("c3")
                                                .next(Parse.textContent()
                                                        .collectOne(PredpisInfo::setPlatnostOd, PredpisInfo.class)
                                                )
                                        )
                                )
                        )
                );
    }

    private NavigateToParsedLink toPredpisDetail(HtmlUnitSiteParser parser) {
        return Actions.navigateToParsedLink(parser)
                .next(GetElements.Descendants.ByCss.byClassName("Frags")
                        .getFirst()
                        .next(GetElements.ByDomTraversal.childElems()
                                .setCollector(Radek::new, Radek.class)
                                .collectMany((Predpis p, Radek r) -> p.getText().add(r), Predpis.class, Radek.class)
                                .next(Parse.textContent()
                                        .collectOne(Radek::setText, Radek.class)
                                )
                                // TODO add css class to the model ...
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
