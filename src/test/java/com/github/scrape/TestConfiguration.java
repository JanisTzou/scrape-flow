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
import com.github.scrape.flow.clients.*;
import com.github.scrape.flow.data.publishing.ScrapedDataPublisher;
import com.github.scrape.flow.debugging.DebuggingOptions;
import com.github.scrape.flow.execution.*;
import com.github.scrape.flow.scraping.Options;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitPageLoader;
import com.github.scrape.flow.throttling.ScrapingRateLimiter;
import com.github.scrape.flow.throttling.ScrapingRateLimiterImpl;
import com.github.scrape.flow.throttling.ThrottlingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class TestConfiguration {

    @Bean
    public ScrapingServices scrapingServices(StepHierarchyRepository stepHierarchyRepository) {
        return new ScrapingServices(
                stepOrderGenerator(),
                throttlingService(),
                activeStepsTracker(),
                clientReservationTracker(),
                clientReservationHandler(),
                stepAndDataRelationshipTracker(),
                exclusiveExecutionTracker(),
                scrapedDataPublisher(),
                options(),
                globalDebugging(),
                taskExecutor(),
                taskService(),
                seleniumClientManager(),
                htmlUnitClientManager(),
                htmlUnitPageLoader(),
                stepHierarchyRepository,
                orderedClientAccessHandler()
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
    public ClientReservationTracker clientReservationTracker() {
        return new ClientReservationTracker();
    }

    @Bean
    public ClientReservationHandler clientReservationHandler() {
        return new ClientReservationHandler(clientReservationTracker(), seleniumClientManager(), htmlUnitClientManager(), orderedClientAccessHandler());
    }

    @Bean
    public StepAndDataRelationshipTracker stepAndDataRelationshipTracker() {
        return new StepAndDataRelationshipTracker(activeStepsTracker());
    }

    @Bean
    public ExclusiveExecutionTracker exclusiveExecutionTracker() {
        return new ExclusiveExecutionTracker(activeStepsTracker());
    }

    @Bean
    public ScrapedDataPublisher scrapedDataPublisher() {
        return new ScrapedDataPublisher(stepAndDataRelationshipTracker());
    }

    @Bean
    public Options options() {
        return new Options();
    }

    @Bean
    public DebuggingOptions globalDebugging() {
        return new DebuggingOptions();
    }

    @Bean
    public ScrapingRateLimiter scrapingRateLimiter() {
        return new ScrapingRateLimiterImpl(1, TimeUnit.MILLISECONDS);
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new TaskExecutorSingleQueue(
                throttlingService(),
                exclusiveExecutionTracker(),
                scrapingRateLimiter(),
                activeStepsTracker(),
                clientReservationHandler()
        );
    }

    @Bean
    public TaskService taskService() {
        return new TaskService(taskExecutor(), activeStepsTracker(), scrapedDataPublisher(), scrapingRateLimiter(), options());
    }

    @Bean(destroyMethod = "close")
    public WebClient webClient() {
        return new WebClient(BrowserVersion.CHROME);
    }

    @Bean
    public SeleniumClientManager seleniumClientManager() {
        return new SeleniumClientManager(new SeleniumClientFactory("/Users/janis/Projects_Data/scrape-flow/chromedriver", false)); // TODO fix this mess
    }

    @Bean
    public HtmlUnitClientManager htmlUnitClientManager() {
        return new HtmlUnitClientManager(new HtmlUnitClientFactory());
    }

    @Bean
    public HtmlUnitPageLoader htmlUnitPageLoader() {
        return new HtmlUnitPageLoader();
    }

    @Bean
    public OrderedClientAccessHandler orderedClientAccessHandler() {
        return new OrderedClientAccessHandler(activeStepsTracker());
    }

}
