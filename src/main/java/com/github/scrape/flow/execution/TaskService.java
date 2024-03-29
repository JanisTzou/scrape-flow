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

package com.github.scrape.flow.execution;

import com.github.scrape.flow.data.publishing.ScrapedDataPublisher;
import com.github.scrape.flow.scraping.Options;
import com.github.scrape.flow.throttling.ScrapingRateLimiter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class TaskService {

    private final TaskExecutor taskExecutor;
    private final ActiveStepsTracker activeStepsTracker;
    private final ScrapedDataPublisher scrapedDataPublisher;
    private final ScrapingRateLimiter scrapingRateLimiter;
    private final Options options;

    public void submitForExecution(TaskDefinition taskDefinition) {
        Task task = createStepTask(taskDefinition);

        StepOrder stepOrder = task.getStepOrder();
        activeStepsTracker.track(stepOrder, taskDefinition.getStepHierarchyOrder(), task.getStepName());
        taskExecutor.submit(
                task,
                r -> handleFinishedStep(stepOrder),
                e -> handleFinishedStep(stepOrder) // even when we finish in error there might be successfully parsed other data that might be waiting to get published outside
        );
    }

    private Task createStepTask(TaskDefinition taskDefinition) {
        scrapingRateLimiter.getRequestFreq();
        int retries = options.getMaxRequestRetries();
        Task task;
        if (retries == 0) {
            task = Task.from(taskDefinition, retries, Duration.ZERO);
        } else {
            task = Task.from(taskDefinition, retries, scrapingRateLimiter.getRequestFreq().dividedBy(retries));
        }
        return task;
    }

    private void handleFinishedStep(StepOrder stepOrder) {
        activeStepsTracker.untrack(stepOrder);
        scrapedDataPublisher.publishDataAfterStepFinished(stepOrder);
    }

}
