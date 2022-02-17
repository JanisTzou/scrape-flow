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

import com.github.scrape.flow.data.publishing.ScrapedDataListener;
import com.github.scrape.flow.scraping.Scraping;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitNavigateToParsedLink;
import com.github.scrape.flow.utils.JsonUtils;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnitFlow.*;

@Log4j2
public class ZakonyProLidiCzDemo {

    public static final String HTTPS_WWW_ZAKONYPROLIDI_CZ = "https://www.zakonyprolidi.cz";

    private static void printLine() {
        String line = IntStream.range(0, 200).mapToObj(i -> "-").collect(Collectors.joining());
        log.info(line);
    }

    @Test
    public void start() throws InterruptedException {

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

        final Scraping scraping = new Scraping(2, TimeUnit.SECONDS)
                .getDebugOptions().setOnlyScrapeFirstElements(false)
                .setSequence(
                        Do.navigateToUrl("https://www.zakonyprolidi.cz/obory")
                                .next(Get.descendants().byAttr("id", "__Page")
                                        .next(Get.descendants().byClass("Name")
                                                .addCollector(Kategorie::new, Kategorie.class, new KategorieListener())
                                                .next(Parse.textContent()
                                                        .collectOne(Kategorie::setJmeno, Kategorie.class)
                                                )
                                                .next(Parse.hRef(href -> HTTPS_WWW_ZAKONYPROLIDI_CZ + href)
                                                        .collectOne(Kategorie::setUrl, Kategorie.class)
                                                        .next(toKategorie()
                                                        )
                                                )
                                        )
                                )
                );


        start(scraping);
    }

    private HtmlUnitNavigateToParsedLink toKategorie() {

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

        return Do.navigateToParsedLink()
                .next(Get.descendants().byClass("BranchNodes")
                        .first() // the first section - there are multiple BranchNodes ...
                        .next(Get.children().firstNth(2) // subcategory list is 2nd DIV
                                // TODO it 2nd child does not exist then do something else ... add special handling for Koronavirus ...
                                .next(Get.descendants().byTag("a")
                                        .addCollector(PodKategorie::new, PodKategorie.class, new PodKategorieListener())
                                        .next(Parse.textContent()
                                                .collectOne(PodKategorie::setJmeno, PodKategorie.class)
                                        )
                                        .next(Parse.hRef(href -> HTTPS_WWW_ZAKONYPROLIDI_CZ + href)
                                                .collectOne(PodKategorie::setUrl, PodKategorie.class)
                                                .next(toPredpisyList()
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
//                                        .next(Filter.apply(domNode -> !HtmlUnitUtils.hasAttributeWithValue(domNode, "class", "disabled", true))
//                                                .next(Actions.followLink()
//                                                        .next(Actions.returnNextPage())
//                                                )
//                                        )
//
//                        )

    // not working  , needs JS ...
//    Do.paginate()
//            .setStepsLoadingNextPage(
//            Get.descendants().byAttr("title", "Jdi na Další")
//                                        .next(Filter.apply(domNode -> !HtmlUnitUtils.hasAttributeWithValue(domNode, "class", "disabled", true))
//            .next(Do.followLink()
//                                                        .next(Do.returnNextPage())
//            )
//            )
//
//            )

    private HtmlUnitNavigateToParsedLink toPredpisyList() {

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

        return Do.navigateToParsedLink()
                .next(Get.descendants().byClass("DocGrid")
                        .first()
                        .next(Get.descendants().byTag("tbody")
                                .next(Get.descendants().byTag("tr")
                                        .addCollector(PredpisInfo::new, PredpisInfo.class, new PredpisInfoListener())
                                        .addCollector(Predpis::new, Predpis.class)
                                        .collectOne(Predpis::setInfo, Predpis.class, PredpisInfo.class)
                                        .collectOne(PredpisInfo::setKategorie, PredpisInfo.class, Kategorie.class)
                                        .collectOne(PredpisInfo::setPodKategorie, PredpisInfo.class, PodKategorie.class)
                                        .next(Get.descendants().byClass("c1")
                                                .next(Get.descendants().byTag("a")
                                                        .next(Parse.hRef(href -> HTTPS_WWW_ZAKONYPROLIDI_CZ + href)
                                                                .collectOne(PredpisInfo::setUrl, PredpisInfo.class)
                                                                .next(toPredpisDetail()
                                                                )
                                                        )
                                                )
                                                .next(Parse.textContent().collectOne(PredpisInfo::setCislo, PredpisInfo.class))
                                        )
                                        .next(Get.descendants().byClass("c2")
                                                .next(Parse.textContent()
                                                        .collectOne(PredpisInfo::setNazev, PredpisInfo.class)
                                                )
                                        )
                                        .next(Get.descendants().byClass("c3")
                                                .next(Parse.textContent()
                                                        .collectOne(PredpisInfo::setPlatnostOd, PredpisInfo.class)
                                                )
                                        )
                                )
                        )
                )
                ;
    }


    private HtmlUnitNavigateToParsedLink toPredpisDetail() {

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

        return Do.navigateToParsedLink()
                .next(Get.descendants().byClass("Frags")
                        .next(Get.children()
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
        productsScraping.start(Duration.ofMinutes(15));
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
    public static class KategorieListener implements ScrapedDataListener<Kategorie> {
        @Override
        public void onScrapedData(Kategorie data) {
            printLine();
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
            printLine();
        }
    }

    @Log4j2
    public static class PodKategorieListener implements ScrapedDataListener<PodKategorie> {
        @Override
        public void onScrapedData(PodKategorie data) {
            printLine();
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
            printLine();
        }
    }

    @Log4j2
    public static class PredpisInfoListener implements ScrapedDataListener<PredpisInfo> {
        @Override
        public void onScrapedData(PredpisInfo data) {
            printLine();
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
            printLine();
        }
    }

    @Log4j2
    public static class PredpisListener implements ScrapedDataListener<Predpis> {
        @Override
        public void onScrapedData(Predpis data) {
            printLine();
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
            printLine();
        }
    }

}
