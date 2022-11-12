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

import com.github.scrape.flow.execution.StepHierarchyRepository;
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
    private ScrapingStep<?> scrapingSequence;

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
        this.scrapingSequence.execute(new ScrapingContext(StepOrder.ROOT), services);
    }

    /**
     * Enables specifying the scraping sequence to be executed
     */
    public <T extends ScrapingStep<T>> Scraping setSequence(T sequence) {
        this.scrapingSequence = sequence;
        StepHierarchyRepository stepHierarchyRepository = StepHierarchyRepository.createFrom(sequence);
        this.services.setStepHierarchyRepository(stepHierarchyRepository);
        return this;
    }

    // should be only exposed to the scraper responsible for running this Scraping instance
    @SuppressWarnings("UnusedReturnValue")
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
