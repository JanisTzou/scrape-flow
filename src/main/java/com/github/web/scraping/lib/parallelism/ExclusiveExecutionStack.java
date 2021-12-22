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

package com.github.web.scraping.lib.parallelism;

import lombok.extern.log4j.Log4j2;

import java.util.Optional;
import java.util.Stack;

// not thread safe! Client code's responsibility ...
@Log4j2
public class ExclusiveExecutionStack {

    private final ActiveStepsTracker activeStepsTracker;

    public ExclusiveExecutionStack(ActiveStepsTracker activeStepsTracker) {
        this.activeStepsTracker = activeStepsTracker;
    }

    /**
     * holds the stack of root step orders that are to be executed exclusively
     */
    private final Stack<StepExecOrder> taskRootOrderStack = new Stack<>();


    void push(StepExecOrder rootExecOrder) {
        taskRootOrderStack.push(rootExecOrder);
    }

    void pop() {
        taskRootOrderStack.pop();
    }

    Optional<StepExecOrder> peek() {
        if (taskRootOrderStack.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(taskRootOrderStack.peek());
        }
    }

    // TODO this solution does not take into account nested exlusive steps (especially those that have intermediary non-exclusive steps between them and their eclusive parents )

    public boolean canExecute(QueuedStepTask qst) {
        StepTask st = qst.getStepTask();
        StepExecOrder order = st.getStepExecOrder();

        if (taskRootOrderStack.isEmpty()) {
            if (st.isExclusiveExecution()) {
                taskRootOrderStack.push(order);
            }
            return true;
        } else {
            // TODO implement exclusive nested stuff ...
            StepExecOrder exclusiveScopeRoot = taskRootOrderStack.peek();
            if (order.isBefore(exclusiveScopeRoot)) {
                // this is ok, there should be nothing in such a step that would depend on this subsequent step
                return true;
            }
            boolean isWithinExclusiveSequence = exclusiveScopeRoot.isParentOf(order);
            if (isWithinExclusiveSequence) {
                return true;
            } else {
                boolean isExclusiveSequenceStillRunning = activeStepsTracker.isPartOfActiveStepSequence(exclusiveScopeRoot);
                if (isExclusiveSequenceStillRunning) {
                    // the task will have to wait for the sequence to finish ...
                    log.debug("{} needs to wait for tasks with exclusive step order root {} to finish", order, exclusiveScopeRoot);
                    return false;
                } else {
                    // exclusive sequence has finished -> pop and this step is free to execute
                    log.debug("Tasks with exclusive step order root {} have finished", exclusiveScopeRoot);
                    this.pop();
                    return true;
                }
            }
        }
    }




}
