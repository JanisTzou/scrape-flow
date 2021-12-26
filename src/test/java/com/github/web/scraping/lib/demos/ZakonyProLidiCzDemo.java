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
import com.github.web.scraping.lib.scraping.htmlunit.HtmlUnitSiteParser;
import com.github.web.scraping.lib.scraping.htmlunit.NavigateToParsedLink;
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


        /*
             <div class="BranchTree" id="__Page">

                <div class="Node" data-groupid="15567490">
                    <a class="Icon" title="Nemá podřízené">
                        <img src="/res/tree-none.png" alt="nochildren">
                    </a>
                    <a class="Name" href="/obor/koronavirus">Koronavirus!</a>
                    <span class="Note">(336)</span>
                </div>

                <div class="Node" data-groupid="1320807">
                    <a class="Icon" title="Rozbalit">
                        <img src="/res/tree-plus0.png" alt="unpack">
                    </a>
                    <a class="Name" href="/obor/finance-a-financni-pravo">Finance</a>
                    <span class="Note">(5349)</span>
                </div>
                <!--   ...   -->
            </div>

         */

        final Scraping scraping = new Scraping(parser, 5)
                .debug().onlyScrapeFirstElements(false)
                .setScrapingSequence(
                        Get.Descendants.ByAttribute.id("__Page")
                                .next(Get.Descendants.ByCss.byClassName("Name")
                                        .addCollector(Kategorie::new, Kategorie.class, new KategorieListener())
                                        .next(Parse.textContent()
                                                .collectOne(Kategorie::setJmeno, Kategorie.class)
                                        )
                                        .next(Parse.hRef(href -> HTTPS_WWW_ZAKONYPROLIDI_CZ + href)
                                                .collectOne(Kategorie::setUrl, Kategorie.class)
                                                .nextNavigate(toKategorie(parser)
                                                )
                                        )
                                )

                );


        start(scraping);
    }

    private NavigateToParsedLink toKategorie(HtmlUnitSiteParser parser) {

        /*
            <div class="BranchNodes">
                <div class="Node">
                    <a class="Selected" href="/obor/finance-a-financni-pravo">
                        <i class="ai ai-beak-right"></i><span>Finance</span></a><span class="Count">(5349)</span>
                </div>
                <div class="Body">
                    <div class="Node">
                        <a href="/obor/bankovnictvi-peneznictvi"><i class="ai ai-beak-right"></i><span>Bankovnictví, peněžnictví</span></a>
                        <span class="Count">(1382)</span>
                    </div>
                    <div class="Node">
                        <a href="/obor/celni-pravo"><i class="ai ai-beak-right"></i><span>Celní právo</span></a>
                        <span class="Count">(582)</span>
                    </div>
                    <!--  ...-->
                </div>
            </div>
         */

        return Do.navigateToParsedLink(parser)
                .next(Get.Descendants.ByCss.byClassName("BranchNodes")
                        .getFirst() // the first section - there are multiple BranchNodes ...
                        .next(Get.nthChildElem(2) // subcategory list is 2nd DIV
                                // TODO it 2nd child does not exist then do something else ... add special handling for Koronavirus ...
                                .next(Get.Descendants.ByTag.anchor()
                                        .addCollector(PodKategorie::new, PodKategorie.class, new PodKategorieListener())
                                        .next(Parse.textContent()
                                                .collectOne(PodKategorie::setJmeno, PodKategorie.class)
                                        )
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

            /*
                <table class="DocGrid">
                    <thead>
                        <tr>
                            <th class="c1">Číslo</th>
                            <th class="c2">Název předpisu</th>
                            <th class="c3">Účinnost od</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td class="c1"><a class="TT REF_OK" href="/cs/2001-120">120/2001 Sb.</a></td>
                            <td class="c2">Exekuční řád</td>
                            <td class="c3">01.05.2001</td>
                        </tr>
                        <!--...-->
                    </tbody>
                </table>
             */

        return Do.navigateToParsedLink(parser)
                .next(Get.Descendants.ByCss.byClassName("DocGrid")
                        .getFirst()
                        .next(Get.Descendants.ByTag.tbody()
                                .next(Get.Descendants.ByTag.tr()
                                        .addCollector(PredpisInfo::new, PredpisInfo.class, new PredpisInfoListener())
                                        .addCollector(Predpis::new, Predpis.class)
                                        .collectOne(Predpis::setInfo, Predpis.class, PredpisInfo.class)
                                        .collectOne(PredpisInfo::setKategorie, PredpisInfo.class, Kategorie.class)
                                        .collectOne(PredpisInfo::setPodKategorie, PredpisInfo.class, PodKategorie.class)
                                        .next(Get.Descendants.ByCss.byClassName("c1")
                                                .next(Get.Descendants.ByTag.anchor()
                                                        .next(Parse.hRef(href -> HTTPS_WWW_ZAKONYPROLIDI_CZ + href)
                                                                .collectOne(PredpisInfo::setUrl, PredpisInfo.class)
                                                                        .nextNavigate(toPredpisDetail(parser)
                                                                        )
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

        /*
            <div class="Frags"><p class="L1 Intro"><a id="f2184409"></a>120</p>
                <p class="L1 Intro"><a id="f2184410"></a>ZÁKON</p>
                <p class="L1 Intro"><a id="f2184411"></a>ze dne 28. února 2001</p>
                <p class="L1 Intro"><a id="f2184412"></a>o soudních exekutorech a exekuční činnosti (exekuční řád) a o změně dalších zákonů</p>
                <p class="L1 Intro"><a id="f2184413"></a>Parlament se usnesl na tomto zákoně České republiky:</p>
                <div class="L0 Intro"><a id="f2184414"><i id="norma"></i></a>
                    <hr>
                </div>
                <p class="L1 CAST"><a id="f2184415"><i id="cast1"></i></a>ČÁST PRVNÍ</p>
                <h3 class="L2 NADPIS"><a id="f2184416"></a>EXEKUČNÍ ŘÁD</h3>
                <p class="L2 HLAVA"><a id="f2184417"><i id="cast1-hlava1"></i></a>HLAVA I</p>
                <h3 class="L3 NADPIS"><a id="f2184418"></a>ZÁKLADNÍ USTANOVENÍ</h3>
                <p class="L3 PARA"><a id="f2184419"><i id="p1"></i></a>§ 1</p>
                <p class="L4"><a id="f2184420"><i id="p1-1"></i></a><var>(1)</var> Soudní exekutor (dále jen "exekutor") je fyzická osoba splňující předpoklady podle tohoto zákona, kterou stát pověřil exekutorským úřadem.</p>
                ....
            </div>
         */

        return Do.navigateToParsedLink(parser)
                .next(Get.Descendants.ByCss.byClassName("Frags")
                        .next(Get.childElems()
                                        .addCollector(Radek::new, Radek.class)
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

        scraper.awaitCompletion(Duration.ofMinutes(15));
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
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
            printLine();
        }
    }

    @Log4j2
    public static class PodKategorieListener implements ParsedDataListener<PodKategorie> {
        @Override
        public void onParsingFinished(PodKategorie data) {
            printLine();
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
            printLine();
        }
    }

    @Log4j2
    public static class PredpisInfoListener implements ParsedDataListener<PredpisInfo> {
        @Override
        public void onParsingFinished(PredpisInfo data) {
            printLine();
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
            printLine();
        }
    }

    @Log4j2
    public static class PredpisListener implements ParsedDataListener<Predpis> {
        @Override
        public void onParsingFinished(Predpis data) {
            printLine();
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
            printLine();
        }
    }

}
