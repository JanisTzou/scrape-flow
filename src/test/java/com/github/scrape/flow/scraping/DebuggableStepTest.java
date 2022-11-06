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

package com.github.scrape.flow.scraping;

import com.github.scrape.flow.scraping.htmlunit.HtmlUnit;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitGetDescendants;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DebuggableStepTest {

    private DebuggableStep<?> debuggableStep;

    @Before
    public void setUp() {
        HtmlUnitGetDescendants scrapingStep = HtmlUnit.Get.descendants();// any scraping step impl. will do
        this.debuggableStep = new DebuggableStep<>(scrapingStep);
    }

    @Test
    public void logFoundElementsSource() {
        assertFalse(debuggableStep.getStep().getStepDebugging().isLogFoundElementsSource());

        debuggableStep.logFoundElementsSource(true);

        assertTrue(debuggableStep.getStep().getStepDebugging().isLogFoundElementsSource());
    }

    @Test
    public void logFoundElementsCount() {
        assertFalse(debuggableStep.getStep().getStepDebugging().isLogFoundElementsCount());

        debuggableStep.logFoundElementsCount(true);

        assertTrue(debuggableStep.getStep().getStepDebugging().isLogFoundElementsCount());
    }
}
