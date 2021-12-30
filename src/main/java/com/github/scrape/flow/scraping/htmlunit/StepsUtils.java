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

package com.github.scrape.flow.scraping.htmlunit;

import com.github.scrape.flow.scraping.ScrapingServices;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;
import java.util.Set;

@Log4j2
public class StepsUtils {

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
