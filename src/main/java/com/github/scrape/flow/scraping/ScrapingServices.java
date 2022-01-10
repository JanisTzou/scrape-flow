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

import com.github.scrape.flow.data.publishing.DataPublisher;
import com.github.scrape.flow.debugging.DebuggingOptions;
import com.github.scrape.flow.parallelism.*;
import com.github.scrape.flow.throttling.ScrapingRateLimiter;
import com.github.scrape.flow.throttling.ThrottlingService;
import lombok.Getter;

/**
 * Encapsulates service singleton classes that need to be accessible to all steps
 */
@Getter
public class ScrapingServices {

    private final StepExecOrderGenerator stepExecOrderGenerator;
    private final ThrottlingService throttlingService;
    private final ActiveStepsTracker activeStepsTracker;
    private final StepAndDataRelationshipTracker stepAndDataRelationshipTracker;
    private final ExclusiveExecutionTracker exclusiveExecutionTracker;
    private final DataPublisher dataPublisher;
    private final Options options;
    private final DebuggingOptions globalDebugging;
    private final TaskExecutor taskExecutor;
    private final TaskService taskService;

    public ScrapingServices(StepExecOrderGenerator stepExecOrderGenerator,
                            ThrottlingService throttlingService,
                            ActiveStepsTracker activeStepsTracker,
                            StepAndDataRelationshipTracker stepAndDataRelationshipTracker,
                            ExclusiveExecutionTracker exclusiveExecutionTracker,
                            DataPublisher dataPublisher,
                            TaskExecutor taskExecutor,
                            Options options,
                            DebuggingOptions globalDebugging,
                            TaskService taskService) {
        this.stepExecOrderGenerator = stepExecOrderGenerator;
        this.throttlingService = throttlingService;
        this.activeStepsTracker = activeStepsTracker;
        this.stepAndDataRelationshipTracker = stepAndDataRelationshipTracker;
        this.exclusiveExecutionTracker = exclusiveExecutionTracker;
        this.dataPublisher = dataPublisher;
        this.options = options;
        this.globalDebugging = globalDebugging;
        this.taskExecutor = taskExecutor;
        this.taskService = taskService;
    }

    public ScrapingServices(ScrapingRateLimiter scrapingRateLimiter) {
        this.stepExecOrderGenerator = new StepExecOrderGenerator();
        this.throttlingService = new ThrottlingService();
        this.activeStepsTracker = new ActiveStepsTracker();
        this.stepAndDataRelationshipTracker = new StepAndDataRelationshipTracker(activeStepsTracker);
        this.exclusiveExecutionTracker = new ExclusiveExecutionTracker(activeStepsTracker);
        this.dataPublisher = new DataPublisher(stepAndDataRelationshipTracker);
        this.options = new Options();
        this.globalDebugging = new DebuggingOptions();
        taskExecutor = new TaskExecutor(throttlingService, exclusiveExecutionTracker, scrapingRateLimiter, activeStepsTracker);
        taskService = new TaskService(taskExecutor, activeStepsTracker, dataPublisher, scrapingRateLimiter, options);
    }

}
