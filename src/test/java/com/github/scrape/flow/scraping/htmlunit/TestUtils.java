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
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {

    public static HtmlPage loadTestPage(String fileName) throws URISyntaxException, IOException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        return loadTestPage(fileName, webClient);
    }

    public static HtmlPage loadTestPage(String fileName, WebClient webClient) throws URISyntaxException, IOException {
        URL resource = HtmlUnitGetChildrenAndParseTextContentIntegrationTest.class.getClassLoader().getResource(fileName);
        File file = Paths.get(resource.toURI()).toFile();
        return webClient.getPage("file:////" + file.getAbsolutePath());
    }

    public static HtmlPage getPageMock(String url) throws Exception {
        HtmlPage mock = mock(HtmlPage.class);
        when(mock.getUrl()).thenReturn(new URL(url));
        return mock;
    }

    public static HtmlAnchor getHtmlAnchorMock(String href, HtmlPage nextPageMock) throws Exception {
        HtmlAnchor anchorMock = mock(HtmlAnchor.class);
        when(anchorMock.hasAttribute(Mockito.any())).thenReturn(true);
        when(anchorMock.getAttributeDirect(Mockito.any())).thenReturn(href);
        when(anchorMock.getHrefAttribute()).thenReturn(href);
        HtmlPage currPageMock = getPageMock(href);
        when(anchorMock.getHtmlPageOrNull()).thenReturn(currPageMock);
        when(anchorMock.click()).thenReturn(nextPageMock);
        return anchorMock;
    }

    public static HtmlUnitStepBlock getMockStep() {
        HtmlUnitStepBlock mock = mock(HtmlUnitStepBlock.class); // any type of step is ok
        when(mock.copy()).thenReturn(mock);
        return mock;
    }


}



