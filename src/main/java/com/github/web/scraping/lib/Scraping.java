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
import com.github.web.scraping.lib.dom.data.parsing.steps.ScrapingServices;
import com.github.web.scraping.lib.dom.data.parsing.steps.HtmlUnitParsingStep;
import com.github.web.scraping.lib.throttling.ScrapingRateLimiterImpl;
import lombok.Getter;

import java.time.Duration;


/**
 * Encapsulates settings of the next level to scrape data from
 */
@Getter
public class Scraping {

    /**
     * Should specific for one scraping instance
     */
    private final ScrapingServices services;
    private SiteParserInternal<?> parser;
    private HtmlUnitParsingStep<?> parsingSequence;


    public Scraping(SiteParserInternal<?> parser, int maxRequestRatePerSec) {
        this(new ScrapingServices(new ScrapingRateLimiterImpl(maxRequestRatePerSec)), parser);
    }

    public Scraping(ScrapingServices services, SiteParserInternal<?> parser) {
        this.services = services;
        this.parser = parser;
    }

    public Scraping setParsingSequence(HtmlUnitParsingStep<?> parsingSequence) {
        this.parsingSequence = parsingSequence;
        return this;
    }

    // should be only exposed to the scraper responsible for running this Scraping instance
    boolean awaitCompletion(Duration timeout) {
        return services.getStepTaskExecutor().awaitCompletion(timeout);
    }
}
