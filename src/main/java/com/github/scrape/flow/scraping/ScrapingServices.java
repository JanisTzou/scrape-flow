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

import com.github.scrape.flow.clients.*;
import com.github.scrape.flow.data.publishing.ScrapedDataPublisher;
import com.github.scrape.flow.debugging.DebuggingOptions;
import com.github.scrape.flow.execution.*;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitPageLoader;
import com.github.scrape.flow.throttling.ScrapingRateLimiter;
import com.github.scrape.flow.throttling.ThrottlingService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Encapsulates service singleton classes that need to be accessible to all steps
 */
@Getter
@RequiredArgsConstructor
public class ScrapingServices {

    private final StepOrderGenerator stepOrderGenerator;
    private final ThrottlingService throttlingService;
    private final ActiveStepsTracker activeStepsTracker;
    private final ClientReservationTracker clientReservationTracker;
    private final ClientReservationHandler clientReservationHandler;
    private final StepAndDataRelationshipTracker stepAndDataRelationshipTracker;
    private final ExclusiveExecutionTracker exclusiveExecutionTracker;
    private final ScrapedDataPublisher scrapedDataPublisher;
    private final Options options;
    private final DebuggingOptions globalDebugging;
    private final TaskExecutor taskExecutor;
    private final TaskService taskService;
    private final SeleniumClientManager seleniumClientManager;
    private final HtmlUnitClientManager htmlUnitClientManager;
    private final HtmlUnitPageLoader htmlUnitSiteLoader;

    public ScrapingServices(ScrapingRateLimiter scrapingRateLimiter) {
        this.stepOrderGenerator = new StepOrderGenerator();
        this.throttlingService = new ThrottlingService();
        this.activeStepsTracker = new ActiveStepsTracker();
        this.clientReservationTracker = new ClientReservationTracker();
        this.stepAndDataRelationshipTracker = new StepAndDataRelationshipTracker(activeStepsTracker);
        this.exclusiveExecutionTracker = new ExclusiveExecutionTracker(activeStepsTracker);
        this.scrapedDataPublisher = new ScrapedDataPublisher(stepAndDataRelationshipTracker);
        this.options = new Options();
        this.globalDebugging = new DebuggingOptions();
        this.seleniumClientManager = new SeleniumClientManager(new SeleniumClientFactory("/Users/janis/Projects_Data/scrape-flow/chromedriver", false)); // TODO fix this mess
        HtmlUnitClientFactory clientFactory = new HtmlUnitClientFactory();
        this.htmlUnitClientManager = new HtmlUnitClientManager(clientFactory);
        this.clientReservationHandler = new ClientReservationHandler(clientReservationTracker, seleniumClientManager, htmlUnitClientManager);
        taskExecutor = new TaskExecutorSingleQueue(throttlingService, exclusiveExecutionTracker, scrapingRateLimiter, activeStepsTracker, clientReservationHandler);
        taskService = new TaskService(taskExecutor, activeStepsTracker, scrapedDataPublisher, scrapingRateLimiter, options);
        this.htmlUnitSiteLoader = new HtmlUnitPageLoader();
    }

}
