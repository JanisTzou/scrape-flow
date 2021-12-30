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

package com.github.scrape.flow.parallelism;

import com.github.scrape.flow.scraping.Options;
import com.github.scrape.flow.throttling.ScrapingRateLimiter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class TaskService {

    private final StepTaskExecutor stepTaskExecutor;
    private final ActiveStepsTracker activeStepsTracker;
    private final NotificationService notificationService;
    private final ScrapingRateLimiter scrapingRateLimiter;
    private final Options options;

    public void handleExecution(StepTaskBasis stepTaskBasis) {
        StepTask stepTask = createStepTask(stepTaskBasis);

        StepExecOrder execOrder = stepTask.getStepExecOrder();
        activeStepsTracker.track(execOrder, stepTask.getStepName());
        stepTaskExecutor.submit(
                stepTask,
                r -> handleFinishedStep(execOrder),
                e -> handleFinishedStep(execOrder) // even when we finish in error there might be successfully parsed other data that might be waiting to get published outside
        );
    }

    private StepTask createStepTask(StepTaskBasis stepTaskBasis) {
        scrapingRateLimiter.getRequestFreq();
        int retries = options.getRequestRetries();
        StepTask stepTask;
        if (retries == 0) {
            stepTask = StepTask.from(stepTaskBasis, retries, Duration.ZERO);
        } else {
            stepTask = StepTask.from(stepTaskBasis, retries, scrapingRateLimiter.getRequestFreq().dividedBy(retries));
        }
        return stepTask;
    }

    private void handleFinishedStep(StepExecOrder stepExecOrder) {
        activeStepsTracker.untrack(stepExecOrder);
        notificationService.notifyAfterStepFinished(stepExecOrder);
    }

}
