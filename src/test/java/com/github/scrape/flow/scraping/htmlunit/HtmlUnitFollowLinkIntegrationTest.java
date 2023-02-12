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

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.scrape.flow.scraping.htmlunit.TestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class, TaskExecutorFakeConfig.class, StepHierarchyRepositoryMockConfig.class})
public class HtmlUnitFollowLinkIntegrationTest {

    private HtmlUnitFollowLink followLink;
    private HtmlPage nextPageMock;
    private HtmlAnchor anchorMock;

    @Autowired
    private ScrapingServices services;

    private HtmlUnitStepBlock nextStepMock1;
    private HtmlUnitStepBlock nextStepMock2;

    @Before
    public void setUp() throws Exception {
        nextPageMock = getPageMock("http://next_url");
        anchorMock = getHtmlAnchorMock("http://curr_url", nextPageMock);

        this.nextStepMock2 = getMockStep();
        this.nextStepMock1 = getMockStep();

        followLink = new HtmlUnitFollowLink()
                .nextBranch(nextStepMock1)
                .nextBranch(nextStepMock2);
    }

    @Test
    @DirtiesContext
    public void allNextStepsAreCalledWithNextPage() {
        followLink.execute(new ScrapingContext(StepOrder.ROOT, anchorMock), services);

        ArgumentCaptor<ScrapingContext> captor1 = ArgumentCaptor.forClass(ScrapingContext.class);
        ArgumentCaptor<ScrapingContext> captor2 = ArgumentCaptor.forClass(ScrapingContext.class);

        verify(nextStepMock1, times(1)).execute(captor1.capture(), Mockito.any());
        verify(nextStepMock2, times(1)).execute(captor2.capture(), Mockito.any());

        assertEquals(nextPageMock, captor1.getValue().getNode());
        assertEquals(nextPageMock, captor2.getValue().getNode());
    }


}
