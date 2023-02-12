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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.github.scrape.flow.scraping.selenium.Selenium.Do;
import static com.github.scrape.flow.scraping.selenium.Selenium.Get;

public class Google {

    public static void main(String[] args) throws InterruptedException {

        final Scraping scraping = new Scraping(1, TimeUnit.SECONDS);

        scraping.getDebugOptions().setOnlyScrapeFirstElements(false)
                .getDebugOptions().setLogFoundElementsSource(false)
                .getDebugOptions().setLogFoundElementsCount(true)
                .getOptions().setMaxRequestRetries(2);

        scraping.setSequence(
                Do.navigateTo("https://www.google.com")
                        .nextBranchExclusively(Get.descendants().byAttr("id", "L2AGLb")
                                .next(Do.click())
                        )
                        .nextBranch(Do.reloadPage()
                                .next(Get.descendants().byAttr("class", "gLFyf").stepName("find-search-field"))
                                .next(Do.sendKeys("Liberec"))
                                .next(Do.submit())
                        )
        );

        start(scraping);
    }

    private static void start(Scraping scraping) throws InterruptedException {
        scraping.start();
        scraping.awaitCompletion(Duration.ofMinutes(2));
        Thread.sleep(2000); // let logging finish ...
    }


}
