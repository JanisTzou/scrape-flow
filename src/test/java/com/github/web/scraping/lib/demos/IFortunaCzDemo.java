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
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.github.web.scraping.lib.scraping.htmlunit.HtmlUnit.*;
import static com.github.web.scraping.lib.scraping.htmlunit.HtmlUnit.Parse;

@Log4j2
public class IFortunaCzDemo {

    public static final String HTTPS_WWW_IFORTUNA_CZ = "https://www.ifortuna.cz";

    @Test
    public void start() throws InterruptedException {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        final Scraping matchesScraping = new Scraping(new HtmlUnitSiteParser(driverManager), 5, TimeUnit.SECONDS)
                .setSequence(
                        Get.descendants().byAttr("id", "top-bets-tab-0")
                                .next(Get.descendants().byTag("div").byClass("events-table-box")
                                        .addCollector(Match::new, Match.class, new MatchListener())
                                        .next(Get.descendants().byTag("tbody")
                                                .next(Get.children().byTag("tr").first() // gets first row containing a single match data
                                                        .next(Get.children().byTag("td").first()
                                                                .next(Get.children().byTag("a") // match detail link
                                                                        .next(Parse.hRef(href -> HTTPS_WWW_IFORTUNA_CZ + href)
                                                                                .collectOne(Match::setDetailUrl, Match.class)
                                                                        )
                                                                )
                                                                .next(Get.descendants().byTag("span").byClass("market-name") // match name (teams)
                                                                        .next(Parse.textContent()
                                                                                .collectOne(Match::setName, Match.class)
                                                                        )
                                                                )
                                                        )
                                                        .next(Get.children().byTag("td").byClass("col-date")  // match date
                                                                .next(Get.descendants().byTag("span").byClass("event-datetime")
                                                                        .next(Parse.textContent()
                                                                                .collectOne(Match::setDate, Match.class)
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                );


        final EntryPoint entryPoint = new EntryPoint("https://www.ifortuna.cz/", matchesScraping);
        final Scraper scraper = new Scraper();
        scraper.scrape(entryPoint);
        scraper.awaitCompletion(Duration.ofMinutes(5));
        Thread.sleep(1000); // let logging finish
    }


    @Getter @Setter @ToString
    public static class Match {
        private String name;
        private String detailUrl;
        private String date;
    }

    public static class MatchListener implements ParsedDataListener<Match> {
        @Override
        public void onParsingFinished(Match data) {
            log.info("\n" + JsonUtils.write(data).orElse("JSON ERROR"));
        }
    }


}
