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

package com.github.scrape.flow.scraping;

import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.throttling.ScrapingRateLimiterImpl;
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
    // TODO the parsing sequence needs to be something generic - not HtmlUnit-specific ...
    private ScrapingStepBase<?> scrapingSequence;

    // TODO add option to not crawl duplicate URLS ...

    public Scraping() {
        this(new ScrapingServices(new ScrapingRateLimiterImpl(1, TimeUnit.SECONDS)));
    }

    public Scraping(int rqLimitPerTimeUnit, TimeUnit timeUnit) {
        this(new ScrapingServices(new ScrapingRateLimiterImpl(rqLimitPerTimeUnit, timeUnit)));
    }

    Scraping(ScrapingServices services) {
        this.services = services;
    }

    public void start() {
        startSequenceExecution();
    }

    public void start(Duration timeout) {
        startSequenceExecution();
        this.awaitCompletion(timeout);
    }

    private void startSequenceExecution() {
        this.scrapingSequence.execute(new ScrapingContext(StepOrder.INITIAL), services);
    }

    // TODO create another method for dynamic sites ? ... maybe put the parse here as well? So it is next to the

    /**
     * Enables specifying the scraping sequence to be executed
     */
    public <T extends ScrapingStepBase<T>> Scraping setSequence(T sequence) {
        this.scrapingSequence = sequence;
        return this;
    }

    // should be only exposed to the scraper responsible for running this Scraping instance
    public boolean awaitCompletion(Duration timeout) {
        return services.getTaskExecutor().awaitCompletion(timeout);
    }

    public ConfigurableScraping getOptions() {
        return new ConfigurableScraping(this);
    }

    public DebuggableScraping getDebugOptions() {
        return new DebuggableScraping(this);
    }

}
