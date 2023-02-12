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

package com.github.weather;

import com.github.scrape.flow.data.publishing.ScrapedDataListener;
import com.github.scrape.flow.scraping.Scraping;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.scrape.flow.scraping.selenium.Selenium.*;

@Log4j2
public class ScrapeChmuOblacnost {

    public static void main(String[] args) throws InterruptedException {

        final Scraping scraping = new Scraping(1, TimeUnit.SECONDS);

        scraping.getDebugOptions().setOnlyScrapeFirstElements(false)
                .getDebugOptions().setLogFoundElementsSource(false)
                .getDebugOptions().setLogFoundElementsCount(false)
                .getOptions().setMaxRequestRetries(2);

        scraping.setSequence(
                Do.navigateTo("https://www.chmi.cz/aktualni-situace/aktualni-stav-pocasi/ceska-republika/stanice/profesionalni-stanice/tabulky/oblacnost-dohlednost")
                        .next(Get.elements().byAttr("id", "loadedcontent")
                                .addCollector(CloudCoverage::new, CloudCoverage.class, new DataListener())
                        )
                        .nextBranch(Get.descendants().byTag("tbody").firstNth(1)
                                .next(Get.descendants().byTag("tr").last().stepName("rows-1"))
                                .next(Get.children().first())
                                .next(Parse.textContent())
                                .collectValue(CloudCoverage::setDateTime, CloudCoverage.class)
                        )
                        .nextBranch(Get.descendants().byTag("tbody").firstNth(2)
                                .next(Get.children().byTag("tr").stepName("rows-2")
                                        .addCollector(Station::new, Station.class)
                                        .collectValues(CloudCoverage::addStation, CloudCoverage.class, Station.class)
                                )
                                .nextBranch(Get.children().firstNth(1).stepName("station-name")
                                        .next(Parse.textContent())
                                        .collectValue(Station::setName, Station.class)
                                )
                                .nextBranch(Get.children().firstNth(3).stepName("description")
                                        .next(Parse.textContent())
                                        .collectValue(Station::setSkyDescription, Station.class)
                                )
                        )
        );

        start(scraping);
    }

    private static void start(Scraping scraping) throws InterruptedException {
        scraping.start();
        scraping.awaitCompletion(Duration.ofMinutes(2));
        Thread.sleep(2000); // let logging finish ...
    }

    @Data
    public static class CloudCoverage {
        private String dateTime;
        private List<Station> stations = new ArrayList<>();

        public void addStation(Station station) {
            this.stations.add(station);
        }
    }

    @Data
    public static class Station {
        private String name;
        private String skyDescription;
    }

    public static class DataListener implements ScrapedDataListener<CloudCoverage> {
        @Override
        public void onScrapedData(CloudCoverage data) {
            log.info("Published data");
            System.out.println(data);
        }
    }


}
