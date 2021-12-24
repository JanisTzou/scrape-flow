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

import com.github.web.scraping.lib.scraping.Scraper;
import com.github.web.scraping.lib.scraping.Scraping;
import com.github.web.scraping.lib.scraping.EntryPoint;
import com.github.web.scraping.lib.drivers.HtmlUnitDriverManager;
import com.github.web.scraping.lib.drivers.HtmlUnitDriversFactory;
import com.github.web.scraping.lib.scraping.htmlunit.Actions;
import com.github.web.scraping.lib.scraping.htmlunit.GetElements;
import com.github.web.scraping.lib.scraping.htmlunit.GetElementsByXPath;
import com.github.web.scraping.lib.scraping.htmlunit.HtmlUnitSiteParser;
import org.junit.Test;

import java.time.Duration;

public class SupraDalekohledyCzDemo {

    @Test
    public void start() throws InterruptedException {

        final HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(new HtmlUnitDriversFactory());

        GetElementsByXPath getNextBtnLink = GetElements.Descendants.ByXPath.xPath("/html/body/div[2]/div[1]/div[4]/div/div/div[2]/div[3]/div[1]/ul/li[4]/a");

        final Scraping productsScraping = new Scraping(new HtmlUnitSiteParser(driverManager), 1)
                .setScrapingSequence(getNextBtnLink
                        .next(Actions.followLink())
                );

        final EntryPoint entryPoint = new EntryPoint("http://www.supra-dalekohledy.cz/prislusenstvi4/okulary/tele-vue/", productsScraping);
        final Scraper scraper = new Scraper();
        scraper.scrape(entryPoint);

        scraper.awaitCompletion(Duration.ofMinutes(5));
        Thread.sleep(2000); // let logging finish ...

    }

}
