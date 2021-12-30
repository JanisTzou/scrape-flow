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

package com.github.scraping.flow.scraping;

import com.github.scraping.flow.scraping.htmlunit.StepsUtils;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Log4j2
public class Scraper {

    // TODO can the parsing here be from both selenium and htmlunit?
    private final List<Scraping> scrapings = new CopyOnWriteArrayList<>();

    public void start(Scraping scraping, String url) {
        this.start(List.of(new EntryPoint(url, scraping)));
    }

    public void start(EntryPoint entryPoint) {
        this.start(List.of(entryPoint));
    }

    public void start(List<EntryPoint> entryPoints) {
        for (EntryPoint entryPoint : entryPoints) {
            String url = entryPoint.getUrl();
            Scraping scraping = entryPoint.getScraping();
            doScrape(url, scraping);
        }
    }

    private void doScrape(String url, Scraping scraping) {
        this.scrapings.add(scraping);
        SiteParser parser = scraping.getParser();
        StepsUtils.propagateServicesRecursively(scraping.getScrapingSequence(), scraping.getServices(), new HashSet<>());
        parser.parse(url, scraping.getScrapingSequence());
    }

    public void awaitCompletion(Duration timeout) {
        for (Scraping scraping : scrapings) {
            scraping.awaitCompletion(timeout);
        }
    }

}
