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

import com.github.scrape.flow.execution.Task;
import com.github.scrape.flow.execution.TaskError;
import com.github.scrape.flow.execution.TaskExecutor;
import com.github.scrape.flow.execution.TaskResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Consumer;

@Configuration
public class TaskExecutorFakeConfig {

    @Bean
    public TaskExecutor taskExecutor() {
        return new TaskExecutorFake();
    }

    private static class TaskExecutorFake implements TaskExecutor {

        @Override
        public void submit(Task task, Consumer<TaskResult> taskResultConsumer, Consumer<TaskError> taskErrorConsumer) {
            task.getStepRunnable().run();
        }

        @Override
        public boolean awaitCompletion(Duration timeout) {
            return false;
        }
    }

}
