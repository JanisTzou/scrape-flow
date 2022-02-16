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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HtmlUnitGetChildrenTest {

    private final HtmlUnitGetChildren getChildren = new HtmlUnitGetChildren();
    private static WebClient webClient;

    @BeforeClass
    public static void beforeClass() {
        webClient = new WebClient(BrowserVersion.CHROME);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        webClient.close();
    }

    @Test
    public void childNodesAreFound() throws IOException, URISyntaxException {

        HtmlPage page = TestUtils.loadTestPage("test_page_1.html", webClient);
        DomNode parent = (DomNode) page.getByXPath("/html/body/div").stream().findFirst().get();

        List<DomNode> foundNodes = getChildren.nodesSearch(parent).get();

        assertEquals(4, foundNodes.size());
    }

}
