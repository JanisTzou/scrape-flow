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

public class StepOrderGenerator {

    private final Map<StepOrder, StepOrder> parentToLastGeneratedChild = new ConcurrentHashMap<>();

    /**
     * @param stepAtPrevLevel step that was preceding on the "higher"/"previous" level
     *                        ... so has kind of a parent relationship to the next one to be generated
     */
    public StepOrder genNextAfter(StepOrder stepAtPrevLevel) {
        return this.parentToLastGeneratedChild.compute(stepAtPrevLevel, (parent0, prevLastStep) -> {
            if (prevLastStep == null) {
                return stepAtPrevLevel.nextAsChild();
            } else {
                return prevLastStep.nextAsSibling();
            }
        });
    }

}
