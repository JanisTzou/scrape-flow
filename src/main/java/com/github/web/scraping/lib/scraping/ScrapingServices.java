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

import com.github.web.scraping.lib.debugging.Debugging;
import com.github.web.scraping.lib.parallelism.*;
import com.github.web.scraping.lib.throttling.ScrapingRateLimiter;
import com.github.web.scraping.lib.throttling.ThrottlingService;
import lombok.Getter;

/**
 * Encapsulates service singleton classes that need to be accessible to all steps
 */
@Getter
public class ScrapingServices {

    private final StepExecOrderGenerator stepExecOrderGenerator = new StepExecOrderGenerator();
    private final ThrottlingService throttlingService = new ThrottlingService();
    private final ActiveStepsTracker activeStepsTracker = new ActiveStepsTracker();
    private final StepAndDataRelationshipTracker stepAndDataRelationshipTracker = new StepAndDataRelationshipTracker(activeStepsTracker);
    private final ExclusiveExecutionTracker exclusiveExecutionTracker = new ExclusiveExecutionTracker(activeStepsTracker);
    private final NotificationService notificationService = new NotificationService(stepAndDataRelationshipTracker);
    private final StepTaskExecutor stepTaskExecutor;
    private final Debugging globalDebugging = new Debugging();

    public ScrapingServices(ScrapingRateLimiter scrapingRateLimiter) {
        stepTaskExecutor = new StepTaskExecutor(throttlingService, exclusiveExecutionTracker, scrapingRateLimiter);
    }

}
