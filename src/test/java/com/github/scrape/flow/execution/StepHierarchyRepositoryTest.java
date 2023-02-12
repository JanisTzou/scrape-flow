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
import com.github.scrape.flow.scraping.htmlunit.HtmlUnit;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitScrapingStep;
import com.github.scrape.flow.scraping.selenium.Selenium;
import com.github.scrape.flow.scraping.selenium.SeleniumScrapingStep;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StepHierarchyRepositoryTest {

    private final HtmlUnitScrapingStep<?> step_a_loading = HtmlUnit.Do.navigateToParsedLink();
    private final HtmlUnitScrapingStep<?> step_b = HtmlUnit.Get.descendants();
    private final SeleniumScrapingStep<?> step_c_loading = Selenium.Do.navigateToParsedLink();

    private final HtmlUnitScrapingStep<?> sequence =
            step_a_loading // 0-1
                    .nextBranch(step_b // 0-1-1
                            .nextBranch(step_a_loading) // 0-1-1-1
                            .nextBranch(step_b) // 0-1-1-2
                    )
                    .nextBranch(step_c_loading); // 0-1-2

    private final StepOrder step_0_1 = StepOrder.from(0, 1);
    private final StepOrder step_0_1_1_1 = StepOrder.from(0, 1, 1, 1);
    private final StepOrder step_0_1_1_2 = StepOrder.from(0, 1, 1, 2);
    private final StepOrder step_0_1_2 = StepOrder.from(0, 1, 2);

    @Test
    public void testCreatingRepository() {
        StepHierarchyRepository repo = StepHierarchyRepository.createFrom(sequence);

        assertEquals(5, repo.size());
    }

    @Test
    public void testRetrievingMetadata() {
        StepHierarchyRepository repo = StepHierarchyRepository.createFrom(sequence);

        StepMetadata meta = repo.getMetadataFor(step_0_1_2);
        assertNotNull(meta);
        assertEquals(meta, repo.getMetadataFor(meta.getStep()));
    }

    @Test
    public void testFindLongestLoadingPathDepth() {
        StepHierarchyRepository repo = StepHierarchyRepository.createFrom(sequence);

        int depth;
        depth = repo.findLongestLoadingPathDepth(step_0_1, ClientType.HTMLUNIT);
        assertEquals(2, depth);

        depth = repo.findLongestLoadingPathDepth(step_0_1_1_1, ClientType.HTMLUNIT);
        assertEquals(2, depth);

        depth = repo.findLongestLoadingPathDepth(step_0_1_1_2, ClientType.HTMLUNIT);
        assertEquals(1, depth);

        depth = repo.findLongestLoadingPathDepth(step_0_1, ClientType.SELENIUM);
        assertEquals(1, depth);
    }

    @Test
    public void testGetRemainingLoadingPathDepth() {
        StepHierarchyRepository repo = StepHierarchyRepository.createFrom(sequence);

        int depth;
        depth = repo.getRemainingLoadingPathDepth(step_0_1, ClientType.HTMLUNIT);
        assertEquals(2, depth);

        depth = repo.getRemainingLoadingPathDepth(step_0_1_1_1, ClientType.HTMLUNIT);
        assertEquals(1, depth);

        depth = repo.getRemainingLoadingPathDepth(step_0_1_1_2, ClientType.HTMLUNIT);
        assertEquals(0, depth);

        depth = repo.getRemainingLoadingPathDepth(step_0_1, ClientType.SELENIUM);
        assertEquals(1, depth);
    }

    @Test
    public void testGetRemainingLoadingPathDepthMax() {
        StepHierarchyRepository repo = StepHierarchyRepository.createFrom(sequence);

        int depth;
        depth = repo.getRemainingLoadingStepsDepthMax(List.of(step_0_1, step_0_1_1_1), ClientType.HTMLUNIT);
        assertEquals(2, depth);

        depth = repo.getRemainingLoadingStepsDepthMax(List.of(step_0_1), ClientType.HTMLUNIT);
        assertEquals(2, depth);

        depth = repo.getRemainingLoadingStepsDepthMax(List.of(step_0_1_1_1), ClientType.HTMLUNIT);
        assertEquals(1, depth);

        depth = repo.getRemainingLoadingStepsDepthMax(List.of(step_0_1, step_0_1_2), ClientType.SELENIUM);
        assertEquals(1, depth);

        depth = repo.getRemainingLoadingStepsDepthMax(List.of(step_0_1), ClientType.SELENIUM);
        assertEquals(1, depth);

        depth = repo.getRemainingLoadingStepsDepthMax(List.of(step_0_1_2), ClientType.SELENIUM);
        assertEquals(1, depth);

    }


}
