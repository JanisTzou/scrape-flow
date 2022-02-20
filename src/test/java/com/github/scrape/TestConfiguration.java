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

package com.github.scrape;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.github.scrape.flow.data.publishing.ScrapedDataPublisher;
import com.github.scrape.flow.debugging.DebuggingOptions;
import com.github.scrape.flow.drivers.HtmlUnitDriverOperator;
import com.github.scrape.flow.drivers.HtmlUnitDriversFactory;
import com.github.scrape.flow.drivers.SeleniumDriversFactory;
import com.github.scrape.flow.drivers.SeleniumDriversManager;
import com.github.scrape.flow.execution.*;
import com.github.scrape.flow.scraping.Options;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitPageLoader;
import com.github.scrape.flow.throttling.ScrapingRateLimiter;
import com.github.scrape.flow.throttling.ScrapingRateLimiterImpl;
import com.github.scrape.flow.throttling.ThrottlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class TestConfiguration {

    @Bean
    @Autowired
    public ScrapingServices scrapingServices(StepOrderGenerator stepOrderGenerator,
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
                                             HtmlUnitPageLoader htmlUnitSiteParser) {
        return new ScrapingServices(stepOrderGenerator,
                throttlingService,
                activeStepsTracker,
                stepAndDataRelationshipTracker,
                exclusiveExecutionTracker,
                scrapedDataPublisher,
                options,
                globalDebugging,
                taskExecutor,
                taskService,
                seleniumDriversManager,
                htmlUnitSiteParser
        );
    }

    @Bean
    public StepOrderGenerator stepOrderGenerator() {
        return new StepOrderGenerator();
    }

    @Bean
    public ThrottlingService throttlingService() {
        return new ThrottlingService();
    }

    @Bean
    public ActiveStepsTracker activeStepsTracker() {
        return new ActiveStepsTracker();
    }

    @Bean
    public StepAndDataRelationshipTracker stepAndDataRelationshipTracker(ActiveStepsTracker activeStepsTracker) {
        return new StepAndDataRelationshipTracker(activeStepsTracker);
    }

    @Bean
    public ExclusiveExecutionTracker exclusiveExecutionTracker(ActiveStepsTracker activeStepsTracker) {
        return new ExclusiveExecutionTracker(activeStepsTracker);
    }

    @Bean
    @Autowired
    public ScrapedDataPublisher dataPublisher(StepAndDataRelationshipTracker stepAndDataRelationshipTracker) {
        return new ScrapedDataPublisher(stepAndDataRelationshipTracker);
    }

    @Bean
    public Options options() {
        return new Options();
    }

    @Bean
    public DebuggingOptions debuggingOptions() {
        return new DebuggingOptions();
    }

    @Bean
    public ScrapingRateLimiter scrapingRateLimiter() {
        return new ScrapingRateLimiterImpl(1, TimeUnit.MILLISECONDS);
    }

    @Bean
    @Autowired
    public TaskExecutor taskExecutor(ThrottlingService throttlingService,
                                     ExclusiveExecutionTracker exclusiveExecutionTracker,
                                     ScrapingRateLimiter scrapingRateLimiter,
                                     ActiveStepsTracker activeStepsTracker) {
        return new TaskExecutorSingleQueue(throttlingService, exclusiveExecutionTracker, scrapingRateLimiter, activeStepsTracker);
    }

    @Bean
    @Autowired
    public TaskService taskService(TaskExecutor taskExecutor,
                                   ActiveStepsTracker activeStepsTracker,
                                   ScrapedDataPublisher scrapedDataPublisher,
                                   ScrapingRateLimiter scrapingRateLimiter,
                                   Options options) {
        return new TaskService(taskExecutor, activeStepsTracker, scrapedDataPublisher, scrapingRateLimiter, options);
    }

    @Bean(destroyMethod = "close")
    public WebClient webClient() {
        return new WebClient(BrowserVersion.CHROME);
    }

    @Bean
    @Autowired
    public SeleniumDriversManager seleniumDriversManager() {
        return new SeleniumDriversManager(new SeleniumDriversFactory("/Users/janis/Projects_Data/scrape-flow/chromedriver", false)); // TODO fix this mess
    }

    @Bean
    @Autowired
    public HtmlUnitPageLoader htmlUnitPageLoader() {
        return new HtmlUnitPageLoader(new HtmlUnitDriverOperator(new HtmlUnitDriversFactory()));
    }

}
