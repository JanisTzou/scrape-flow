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
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.scrape.TaskExecutorFakeConfig;
import com.github.scrape.TestConfiguration;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ScrapingContext;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static com.github.scrape.flow.scraping.htmlunit.TestUtils.getHtmlAnchorMock;
import static com.github.scrape.flow.scraping.htmlunit.TestUtils.getPageMock;
import static org.junit.Assert.assertEquals;

@ContextConfiguration(classes = {TestConfiguration.class, TaskExecutorFakeConfig.class})
public class HtmlUnitFollowLinkTest {

//    private HtmlUnitFollowLinkRunnable followLink;
//    private HtmlPage nextPageMock;
//    private HtmlAnchor anchorMock;
//
//    @Before
//    public void setUp() throws Exception {
//        followLink = new HtmlUnitFollowLinkRunnable(
//                new ScrapingContext(StepOrder.ROOT, anchorMock),
//                StepOrder.ROOT.getFirstChild(),
//                null,
//                "name"
//        );
//
//        nextPageMock = getPageMock("http://next_url");
//        anchorMock = getHtmlAnchorMock("http://curr_url", nextPageMock);
//    }
//
//    @Test
//    public void anchorElementIsClickedAndNextPageIsReturned() {
//        List<DomNode> nextNodes = followLink.clickLinkAndGetNextPage(StepOrder.ROOT, anchorMock).get();
//
//        assertEquals(1, nextNodes.size());
//        assertEquals(nextPageMock, nextNodes.get(0));
//    }

}
