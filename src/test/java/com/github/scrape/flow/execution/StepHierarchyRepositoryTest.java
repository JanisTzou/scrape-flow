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

import com.github.scrape.flow.scraping.ClientType;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitFlow;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitScrapingStep;
import com.github.scrape.flow.scraping.selenium.SeleniumFlow;
import com.github.scrape.flow.scraping.selenium.SeleniumScrapingStep;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StepHierarchyRepositoryTest {

    private final HtmlUnitScrapingStep<?> step_a = HtmlUnitFlow.Do.navigateToParsedLink();
    private final HtmlUnitScrapingStep<?> step_b = HtmlUnitFlow.Get.descendants();
    private final SeleniumScrapingStep<?> step_c = SeleniumFlow.Do.navigateToParsedLink();

    private final HtmlUnitScrapingStep<?> sequence =
            step_a // 0
                    .next(step_b // 0-1
                            .next(step_a) // 0-1-1
                            .next(step_b) // 0-1-2
                    )
                    .next(step_c); // 0-2

    @Test
    public void testCreatingRepository() {
        StepHierarchyRepository repo = StepHierarchyRepository.createFrom(sequence);

        assertEquals(5, repo.size());
    }

    @Test
    public void testRetrievingMetadata() {
        StepHierarchyRepository repo = StepHierarchyRepository.createFrom(sequence);

        StepMetadata meta = repo.getMetadataFor(StepOrder.from(0, 1, 2));
        assertNotNull(meta);
        assertEquals(meta, repo.getMetadataFor(meta.getStep()));
    }

    @Test
    public void testFindLongestLoadingPathDepth() {
        StepHierarchyRepository repo = StepHierarchyRepository.createFrom(sequence);

        int longestLoadingPath;
        longestLoadingPath = repo.findLongestLoadingPathDepth(StepOrder.INITIAL, ClientType.HTMLUNIT);
        assertEquals(2, longestLoadingPath);

        longestLoadingPath = repo.findLongestLoadingPathDepth(StepOrder.from(0, 1, 1), ClientType.HTMLUNIT);
        assertEquals(2, longestLoadingPath);

        longestLoadingPath = repo.findLongestLoadingPathDepth(StepOrder.from(0, 1, 2), ClientType.HTMLUNIT);
        assertEquals(1, longestLoadingPath);

        longestLoadingPath = repo.findLongestLoadingPathDepth(StepOrder.INITIAL, ClientType.SELENIUM);
        assertEquals(1, longestLoadingPath);
    }

    @Test
    public void testGetRemainingLoadingPathDepth() {
        StepHierarchyRepository repo = StepHierarchyRepository.createFrom(sequence);

        int depth;
        depth = repo.getRemainingLoadingPathDepth(StepOrder.INITIAL, ClientType.HTMLUNIT);
        assertEquals(2, depth);

        depth = repo.getRemainingLoadingPathDepth(StepOrder.from(0, 1, 1), ClientType.HTMLUNIT);
        assertEquals(1, depth);

        depth = repo.getRemainingLoadingPathDepth(StepOrder.from(0, 1, 2), ClientType.HTMLUNIT);
        assertEquals(0, depth);

        depth = repo.getRemainingLoadingPathDepth(StepOrder.INITIAL, ClientType.SELENIUM);
        assertEquals(1, depth);
    }


}
