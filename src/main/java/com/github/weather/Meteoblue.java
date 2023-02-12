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

import com.github.scrape.flow.scraping.Scraping;
import com.github.scrape.flow.scraping.selenium.SeleniumPeek;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.github.scrape.flow.scraping.selenium.Selenium.*;

@Log4j2
public class Meteoblue {

    public static void main(String[] args) throws InterruptedException {

        final Scraping scraping = new Scraping(1, TimeUnit.SECONDS);

        scraping.getDebugOptions().setOnlyScrapeFirstElements(false)
                .getDebugOptions().setLogFoundElementsSource(false)
                .getDebugOptions().setLogFoundElementsCount(true)
                .getOptions().setMaxRequestRetries(2);

        setSequence(scraping);
        start(scraping);

        System.out.println("end");
    }

    private static void setSequence(Scraping scraping) {
        scraping.setSequence(
                Do.navigateTo("https://www.meteoblue.com/cs/po%C4%8Das%C3%AD/t%C3%BDden")
                        // TODO add as nextExclusively...
                        .nextBranchExclusively(Get.descendants().byTag("input").byAttr("value", "Akceptovat a pokračovat")
                                .next(Do.click())
                        )
                        .nextBranch(Get.elements().byAttr("id", "gls")
                                .next(Get.elements().byAttr("id", "gls"))
                                .next(Do.click())
                                .next(Do.sendKeys("Ostrava"))
                                .next(Do.pause(2000))
                                .next(Get.elements().byAttr("class", "search-results"))
                                .next(Get.children().firstNth(2))
                                .next(Get.children().firstNth(2))
                                .next(Do.click())
                                .next(scrapeDays())
                        )
        );
    }

    private static SeleniumPeek scrapeDays() {
        return Get.elements().byAttr("id", "tab_results")
                .next(Get.elements().byAttr("class", "tab-content"))
                .next(Get.children().byAttr("class", "weather day").stepName("day").debugOptions().logFoundElementsCount(true))
                .next(Do.click().stepName("click-day"))
                .next(Do.pause(1000))
                .next(Do.peek(e -> log.info("... inside peek")))
                ;
    }

    private static void start(Scraping scraping) throws InterruptedException {
        scraping.start();
        scraping.awaitCompletion(Duration.ofMinutes(2));
        Thread.sleep(2000); // let logging finish ...
    }


}
