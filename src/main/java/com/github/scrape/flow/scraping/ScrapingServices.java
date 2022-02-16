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

import com.github.scrape.flow.data.publishing.ScrapedDataPublisher;
import com.github.scrape.flow.debugging.DebuggingOptions;
import com.github.scrape.flow.drivers.HtmlUnitDriverOperator;
import com.github.scrape.flow.drivers.HtmlUnitDriversFactory;
import com.github.scrape.flow.drivers.SeleniumDriversFactory;
import com.github.scrape.flow.drivers.SeleniumDriversManager;
import com.github.scrape.flow.execution.*;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitSiteLoader;
import com.github.scrape.flow.throttling.ScrapingRateLimiter;
import com.github.scrape.flow.throttling.ThrottlingService;
import lombok.Getter;

/**
 * Encapsulates service singleton classes that need to be accessible to all steps
 */
@Getter
public class ScrapingServices {

    private final StepOrderGenerator stepOrderGenerator;
    private final ThrottlingService throttlingService;
    private final ActiveStepsTracker activeStepsTracker;
    private final StepAndDataRelationshipTracker stepAndDataRelationshipTracker;
    private final ExclusiveExecutionTracker exclusiveExecutionTracker;
    private final ScrapedDataPublisher scrapedDataPublisher;
    private final Options options;
    private final DebuggingOptions globalDebugging;
    private final TaskExecutor taskExecutor;
    private final TaskService taskService;

    private final SeleniumDriversManager seleniumDriversManager;
    private final HtmlUnitSiteLoader htmlUnitSiteLoader; // TODO just temporary here ...

    public ScrapingServices(StepOrderGenerator stepOrderGenerator,
                            ThrottlingService throttlingService,
                            ActiveStepsTracker activeStepsTracker,
                            StepAndDataRelationshipTracker stepAndDataRelationshipTracker,
                            ExclusiveExecutionTracker exclusiveExecutionTracker,
                            ScrapedDataPublisher scrapedDataPublisher,
                            TaskExecutor taskExecutor,
                            Options options,
                            DebuggingOptions globalDebugging,
                            TaskService taskService,
                            SeleniumDriversManager seleniumDriversManager,
                            HtmlUnitSiteLoader htmlUnitSiteLoader) {
        this.stepOrderGenerator = stepOrderGenerator;
        this.throttlingService = throttlingService;
        this.activeStepsTracker = activeStepsTracker;
        this.stepAndDataRelationshipTracker = stepAndDataRelationshipTracker;
        this.exclusiveExecutionTracker = exclusiveExecutionTracker;
        this.scrapedDataPublisher = scrapedDataPublisher;
        this.options = options;
        this.globalDebugging = globalDebugging;
        this.taskExecutor = taskExecutor;
        this.taskService = taskService;
        this.seleniumDriversManager = seleniumDriversManager;
        this.htmlUnitSiteLoader = htmlUnitSiteLoader;
    }

    public ScrapingServices(ScrapingRateLimiter scrapingRateLimiter) {
        this.stepOrderGenerator = new StepOrderGenerator();
        this.throttlingService = new ThrottlingService();
        this.activeStepsTracker = new ActiveStepsTracker();
        this.stepAndDataRelationshipTracker = new StepAndDataRelationshipTracker(activeStepsTracker);
        this.exclusiveExecutionTracker = new ExclusiveExecutionTracker(activeStepsTracker);
        this.scrapedDataPublisher = new ScrapedDataPublisher(stepAndDataRelationshipTracker);
        this.options = new Options();
        this.globalDebugging = new DebuggingOptions();
        taskExecutor = new TaskExecutor(throttlingService, exclusiveExecutionTracker, scrapingRateLimiter, activeStepsTracker);
        taskService = new TaskService(taskExecutor, activeStepsTracker, scrapedDataPublisher, scrapingRateLimiter, options);
        this.seleniumDriversManager = new SeleniumDriversManager(new SeleniumDriversFactory("/Users/janis/Projects_Data/scrape-flow/chromedriver", false)); // TODO fix this mess
        this.htmlUnitSiteLoader = new HtmlUnitSiteLoader(new HtmlUnitDriverOperator(new HtmlUnitDriversFactory()));
    }

}
