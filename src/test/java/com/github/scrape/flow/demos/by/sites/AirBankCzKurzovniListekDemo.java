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
import com.github.scrape.flow.utils.JsonUtils;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.scrape.flow.scraping.selenium.Selenium.*;

public class AirBankCzKurzovniListekDemo {

    @Ignore
    @Test
    public void start() throws InterruptedException {

        final Scraping scraping = new Scraping(1, TimeUnit.SECONDS);

        scraping.getDebugOptions().setOnlyScrapeFirstElements(false)
                .getDebugOptions().setLogFoundElementsSource(false)
                .getDebugOptions().setLogFoundElementsCount(false)
                .getOptions().setMaxRequestRetries(2);

        scraping.setSequence(
                Do.navigateToUrl("https://www.airbank.cz/kurzovni-listek/")
                        .next(Get.descendants().byTag("tbody")
                                .last()
                                .addCollector(KurzovniListek::new, KurzovniListek.class, new KurzovniListekScraped())
                                .next(Get.descendants()
                                        .addCollector(Kurz::new, Kurz.class)
                                        .collectValue(KurzovniListek::addKurz, KurzovniListek.class, Kurz.class)
                                        .byTag("tr")
                                        .next(Get.descendants()
                                                .byTag("td")
                                                .firstNth(2)
                                                .next(Parse.textContent().collectValue(Kurz::setMena, Kurz.class))
                                        )
                                        .next(Get.descendants()
                                                .byTag("td")
                                                .firstNth(3)
                                                .next(Parse.textContent().collectValue(Kurz::setMnozstvi, Kurz.class))
                                        )
                                        .next(Get.descendants()
                                                .byTag("td")
                                                .firstNth(4)
                                                .next(Parse.textContent().collectValue(Kurz::setNakup, Kurz.class))
                                        )
                                        .next(Get.descendantsBySelector("td:last-child") // do this using a scc selector
                                                .next(Get.descendants()
                                                        .byTag("div")
                                                        .first()
                                                        .next(Parse.textContent().collectValue(Kurz::setProdej, Kurz.class))
                                                )
                                        )
                                )
                        )

        );

        start(scraping);
    }

    private void start(Scraping scraping) throws InterruptedException {
        scraping.start();
        scraping.awaitCompletion(Duration.ofMinutes(2));
        Thread.sleep(2000); // let logging finish ...
    }


    @Data
    public static class KurzovniListek {
        private volatile List<Kurz> kurzList = new ArrayList<>();

        public void addKurz(Kurz kurz) {
            this.kurzList.add(kurz);
        }
    }

    @Data
    public static class Kurz {
        private volatile String mena;
        private volatile String mnozstvi;
        private volatile String nakup;
        private volatile String prodej;
    }


    @Log4j2
    public static class KurzovniListekScraped implements ScrapedDataListener<KurzovniListek> {

        @Override
        public void onScrapedData(KurzovniListek data) {
            log.info("\n" + JsonUtils.write(data).orElse("FAILED TO GENERATE JSON"));
        }
    }

}
