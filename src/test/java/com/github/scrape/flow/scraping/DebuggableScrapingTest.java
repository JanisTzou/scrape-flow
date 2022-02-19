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

import com.github.scrape.flow.debugging.DebuggingOptions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DebuggableScrapingTest {

    private DebuggableScraping debuggableScraping;
    private DebuggingOptions debuggingOptions;

    @Before
    public void setUp() throws Exception {
        this.debuggingOptions = new DebuggingOptions();
        Scraping scrapingMock = Mockito.mock(Scraping.class);
        ScrapingServices servicesMock = Mockito.mock(ScrapingServices.class);
        Mockito.when(scrapingMock.getServices()).thenReturn(servicesMock);
        Mockito.when(servicesMock.getGlobalDebugging()).thenReturn(debuggingOptions);
        this.debuggableScraping = new DebuggableScraping(scrapingMock);
    }

    @Test
    public void onlyScrapeFirstElements() {
        assertFalse(debuggingOptions.isOnlyScrapeFirstElements());

        debuggableScraping.onlyScrapeFirstElements(true);

        assertTrue(debuggingOptions.isOnlyScrapeFirstElements());
    }

    @Test
    public void logFoundElementsSource() {
        assertFalse(debuggingOptions.isLogFoundElementsSource());

        debuggableScraping.logFoundElementsSource(true);

        assertTrue(debuggingOptions.isLogFoundElementsSource());
    }

    @Test
    public void logFoundElementsCount() {
        assertFalse(debuggingOptions.isLogFoundElementsCount());

        debuggableScraping.logFoundElementsCount(true);

        assertTrue(debuggingOptions.isLogFoundElementsCount());
    }
}
