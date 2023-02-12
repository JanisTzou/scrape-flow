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

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.scrape.StepHierarchyRepositoryMockConfig;
import com.github.scrape.TaskExecutorFakeConfig;
import com.github.scrape.TestConfiguration;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.github.scrape.flow.scraping.htmlunit.TestUtils.getMockStep;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class, TaskExecutorFakeConfig.class, StepHierarchyRepositoryMockConfig.class})
public class HtmlUnitGetDescendantsIntegrationTest {

    @Autowired
    private ScrapingServices scrapingServices;

    private HtmlUnitGetDescendants getDescendants;

    private HtmlUnitStepBlock nextStepMock1;
    private HtmlUnitStepBlock nextStepMock2;

    @Before
    public void setUp() {
        this.nextStepMock2 = getMockStep();
        this.nextStepMock1 = getMockStep();
        this.getDescendants = new HtmlUnitGetDescendants()
                .nextBranch(nextStepMock1)
                .nextBranch(nextStepMock2);
    }

    @Test
    @DirtiesContext
    public void eachNextStepIsCalledForEachFoundDescendant() throws IOException, URISyntaxException {
        HtmlPage page = TestUtils.loadTestPage("test_page_1.html");
        DomNode parent = (DomNode) page.getByXPath("/html/body/div").stream().findFirst().get();

        getDescendants.execute(new ScrapingContext(StepOrder.ROOT, parent), scrapingServices);

        verify(nextStepMock1, times(6)).execute(Mockito.any(), Mockito.any());
        verify(nextStepMock2, times(6)).execute(Mockito.any(), Mockito.any());
    }


}
