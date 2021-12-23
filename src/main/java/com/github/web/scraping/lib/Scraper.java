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

import com.github.web.scraping.lib.dom.data.parsing.SiteParserInternal;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class Scraper {

    // TODO preserve insertion order?
    // can the parsing here be from both selenium and htmlunit?
    private final Set<Scraping> scrapings = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void scrape(List<EntryPoint> entryPoints) {
        for (EntryPoint entryPoint : entryPoints) {
            String url = entryPoint.getUrl();
            Scraping scraping = entryPoint.getScraping();
            doScrape(url, scraping);
        }
    }

    private void doScrape(String url, Scraping scraping) {
        this.scrapings.add(scraping);
        SiteParserInternal<?> parser = scraping.getParser();
        parser.setServicesInternal(scraping.getServices());
        parser.parse(url, scraping.getParsingSequence());
    }

    public void scrape(EntryPoint entryPoint) {
        this.scrape(List.of(entryPoint));
    }

    public void awaitCompletion(Duration timeout) {
        for (Scraping scraping : scrapings) {
            scraping.awaitCompletion(timeout);
        }
    }

}
