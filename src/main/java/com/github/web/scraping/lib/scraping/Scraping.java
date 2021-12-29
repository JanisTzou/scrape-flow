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

package com.github.web.scraping.lib.scraping;

import com.github.web.scraping.lib.scraping.htmlunit.HtmlUnitScrapingStep;
import com.github.web.scraping.lib.scraping.htmlunit.HtmlUnitScrapingStepUpdater;
import com.github.web.scraping.lib.scraping.htmlunit.StepsUtils;
import com.github.web.scraping.lib.throttling.ScrapingRateLimiterImpl;
import lombok.Getter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


/**
 * Encapsulates settings of the next level to scrape data from
 */
@Getter
public class Scraping {

    /**
     * Should specific for one scraping instance
     */
    private final ScrapingServices services;
    private SiteParser parser;
    // TODO the parsing sequence needs to be something generic - not HtmlUnit-specific ...
    private HtmlUnitScrapingStep<?> scrapingSequence;

    // TODO add option to not crawl duplicate URLS ...


    public Scraping(SiteParser parser, int rqLimitPerTimeUnit, TimeUnit timeUnit) {
        this(new ScrapingServices(new ScrapingRateLimiterImpl(rqLimitPerTimeUnit, timeUnit)), parser);
    }

    Scraping(ScrapingServices services, SiteParser parser) {
        this.services = services;
        this.parser = parser;
    }

    public <T extends HtmlUnitScrapingStep<T>> Scraping setSequence(T sequence) {
        this.scrapingSequence = new HtmlUnitScrapingStepUpdater<>(sequence).setScrapingSequence(StepsUtils.getStackTraceElementAt(2));
        return this;
    }

    // should be only exposed to the scraper responsible for running this Scraping instance
    public boolean awaitCompletion(Duration timeout) {
        return services.getStepTaskExecutor().awaitCompletion(timeout);
    }

    public DebuggableScraping debugOptions() {
        return new DebuggableScraping(this);
    }

}
