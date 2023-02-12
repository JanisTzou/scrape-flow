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
import java.util.concurrent.TimeUnit;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnit.*;

@Log4j2
public class IFortunaCzDemo {

    public static final String HTTPS_WWW_IFORTUNA_CZ = "https://www.ifortuna.cz";

    //    @Ignore
    @Test
    public void start() throws InterruptedException {

        Scraping scraping = new Scraping(5, TimeUnit.SECONDS);
        scraping.setSequence(Do.navigateTo("https://www.ifortuna.cz/")
                        .next(Get.descendants().byAttr("id", "top-bets-tab-0"))
                        .next(Get.descendants().byTag("div").byClass("events-table-box"))
                        .addCollector(Match::new, Match.class, new MatchListener())
                        .next(Get.descendants().byTag("tbody"))
                        .next(Get.children().byTag("tr").first()) // gets first row containing a single match data
                        .nextBranch(Get.children().byTag("td").first()
                                .nextBranch(Get.children().byTag("a") // match detail link
                                        .next(Parse.hRef(href -> HTTPS_WWW_IFORTUNA_CZ + href))
                                        .collectValue(Match::setDetailUrl, Match.class)
                                )
                                .nextBranch(Get.descendants().byTag("span").byClass("market-name") // match name (teams)
                                        .next(Parse.textContent())
                                        .collectValue(Match::setName, Match.class)
                                )
                        )
                        .nextBranch(Get.children().byTag("td").byClass("col-date") // match date
                                .next(Get.descendants().byTag("span").byClass("event-datetime"))
                                .next(Parse.textContent())
                                .collectValue(Match::setDate, Match.class)
                        )

//                                )
        );


        scraping.start(Duration.ofMinutes(5));
        Thread.sleep(1000); // let logging finish
    }


    @Data
    public static class Match {
        private String name;
        private String detailUrl;
        private String date;
    }

    public static class MatchListener implements ScrapedDataListener<Match> {
        @Override
        public void onScrapedData(Match data) {
            log.info("\n" + JsonUtils.write(data).orElse("JSON ERROR"));
        }
    }


}
