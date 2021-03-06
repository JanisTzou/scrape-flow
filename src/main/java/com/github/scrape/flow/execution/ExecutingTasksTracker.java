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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutingTasksTracker {

    private final Map<StepOrder, Task> tracked = new ConcurrentHashMap<>();

    public void track(Task task) {
        tracked.put(task.getStepOrder(), task);
    }

    public void untrack(Task task) {
        tracked.remove(task.getStepOrder());
    }

    public int countOfExecutingTasks() {
        return tracked.size();
    }

    public int countOfExecutingThrottlableTasks() {
        return (int) tracked.values().stream().filter(Task::isThrottlingAllowed).count();
    }
}
