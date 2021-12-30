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

package com.github.scraping.flow.scraping.htmlunit;

import com.github.scraping.flow.scraping.ScrapingServices;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;
import java.util.Set;

@Log4j2
public class StepsUtils {

    // TODO this doe snot exactly find sircular dependencies but repeated dependencies ...
    // TODO not ideal ... especially that it has to be called repeatedly when e.g. in class Paginate
    //  ... if steps have reference to parents then we do not have to do this ... we can propagate it upwards to parents ...
    // TODO ... this needs to be propagated via the steps execute() method ...
    @Deprecated
    public static void propagateServicesRecursively(HtmlUnitScrapingStep<?> nextStep, ScrapingServices services, Set<HtmlUnitScrapingStep<?>> visited) {
        if (visited.contains(nextStep)) {
            throw new IllegalStateException("Circular step dependencies detected for step: " + nextStep.getName());
        }
        nextStep.setServicesMutably(services);
        visited.add(nextStep);
        for (HtmlUnitScrapingStep<?> ns : nextStep.getNextSteps()) {
            propagateServicesRecursively(ns, services, visited);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends HtmlUnitScrapingStep<T>> Optional<T> findStepOfTypeInSequence(HtmlUnitScrapingStep<?> sequence, Class<T> stepType) {
        if (stepType.isAssignableFrom(sequence.getClass())) {
            return Optional.of((T) sequence);
        } else {
            for (HtmlUnitScrapingStep<?> nextStep : sequence.getNextSteps()) {
                return findStepOfTypeInSequence(nextStep, stepType);
            }
        }
        return Optional.empty();
    }

    public static StackTraceElement getStackTraceElementAt(int idx) {
        return Thread.currentThread().getStackTrace()[idx];
    }

}
