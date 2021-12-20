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

package com.github.web.scraping.lib.dom.data.parsing.steps;

import com.github.web.scraping.lib.parallelism.StepOrderGenerator;
import com.github.web.scraping.lib.parallelism.TaskQueue;
import com.github.web.scraping.lib.throttling.ThrottlingService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Encapsulates service singleton classes that need to be accessible to all steps
 */
@Getter
public class CrawlingServices {

    private final StepOrderGenerator stepOrderGenerator = new StepOrderGenerator();
    private final ThrottlingService throttlingService = new ThrottlingService();
    private final TaskQueue taskQueue = new TaskQueue(throttlingService);

}
