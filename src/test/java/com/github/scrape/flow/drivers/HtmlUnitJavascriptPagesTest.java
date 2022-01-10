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

package com.github.scrape.flow.drivers;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class HtmlUnitJavascriptPagesTest {


    @Ignore
    @Test
    public void testLoadedPageWhenLinkClicked() throws IOException, InterruptedException {

        final WebClient webClient = new WebClient(BrowserVersion.CHROME);

        webClient.getOptions().setTimeout(15000);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setJavaScriptEnabled(true);


        HtmlPage page = (HtmlPage) webClient.getPage("https://www.bezrealitky.cz/vypis/nabidka-prodej/byt/jihomoravsky-kraj/okres-brno-mesto");

        assertTrue(page.asXml().contains("Renneská"));

        webClient.close();

    }

}
