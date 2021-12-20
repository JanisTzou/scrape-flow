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

package com.github.web.scraping.lib;

import com.github.web.scraping.lib.dom.data.parsing.JsonUtils;
import com.github.web.scraping.lib.dom.data.parsing.ParsedData;
import com.github.web.scraping.lib.dom.data.parsing.SiteParser;
import com.github.web.scraping.lib.dom.data.parsing.SiteParserInternal;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.List;

@Log4j2
public class Crawler {

    // can the parsing here be from both selenium and htmlunit?
    // TODO make flux based ...

    public void scrape(List<EntryPoint> entryPoints) {
        for (EntryPoint entryPoint : entryPoints) {
            String url = entryPoint.getUrl();
            Crawling crawling = entryPoint.getCrawling();
            doScrape(url, crawling);
        }
    }

    // TODO what do we want to return actually? And how ?
    private void doScrape(String url, Crawling crawling) {
        SiteParserInternal<?> siteParser = crawling.getSiteParser();
        siteParser.setServicesInternal(crawling.getServices());
        List<ParsedData> pdList = siteParser.parse(url);
        // TODO think of good ways to parallelize this ... also taking into account throttling ...

        for (ParsedData pd : pdList) {
            log.info("Scraped data:");
            log.info(JsonUtils.write(pd.getData()));
        }
    }

    public void scrape(EntryPoint entryPoint) {
        this.scrape(List.of(entryPoint));
    }

    public void awaitCompletion(Duration timeout) {
        // TODO ... delegate to taskqueue ... and await based on running tasks ...
        try {
            Thread.sleep(timeout.toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
