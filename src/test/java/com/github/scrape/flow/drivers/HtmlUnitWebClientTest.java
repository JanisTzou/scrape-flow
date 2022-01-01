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
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class HtmlUnitWebClientTest {

    @Ignore
    @Test
    public void testMemoryFootprint() throws IOException, InterruptedException {

        final WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setJavaScriptEnabled(false);

        for (int i = 0; i < 100000; i++) {
            Thread.sleep(50);

//            HtmlPage htmlPage1 = (HtmlPage) webClient.getPage("https://www.sreality.cz/detail/prodej/byt/1+1/praha-vrsovice-kosicka/4259829852#img=0&fullscreen=false");
//            HtmlPage htmlPage2 = (HtmlPage) webClient.getPage("https://www.sreality.cz/detail/prodej/byt/1+kk/praha-nove-mesto-na-rybnicku/932460636#img=0&fullscreen=false");
            HtmlPage htmlPage1 = (HtmlPage) webClient.getPage("https://javascript.spotrebitelskepravo.cz/");
            HtmlPage htmlPage2 = (HtmlPage) webClient.getPage("https://javascript.spotrebitelskepravo.cz/");

            System.out.println(htmlPage1);
            System.out.println(htmlPage2);

            System.out.println(htmlPage1.asXml().length() == htmlPage2.asXml().length());
            System.out.println(htmlPage1.asXml().contains("N02320"));
            System.out.println(htmlPage2.asXml().contains("NRU224"));

            System.out.println(htmlPage1.getEnclosingWindow().hashCode());
            System.out.println(htmlPage2.getEnclosingWindow().hashCode());

            System.out.println(htmlPage1.getEnclosingWindow() == htmlPage2.getEnclosingWindow());

            System.out.println(htmlPage1.getEnclosingWindow().getEnclosedPage());
            System.out.println(htmlPage2.getEnclosingWindow().getEnclosedPage());


//            WebWindow window = webClient.openWindow(new URL("https://www.sreality.cz/detail/prodej/byt/1+kk/praha-vinohrady-perucka/1552288604#img=0&fullscreen=false"), "window_" + System.currentTimeMillis());
//            HtmlPage htmlPage3 = (HtmlPage) window.getEnclosedPage();
//            System.out.println(htmlPage3.getEnclosingWindow());
//            System.out.println(webClient.getTopLevelWindows().size());

        }

        webClient.close();

    }


    @Ignore
    @Test
    public void testLoadedPageWhenLinkClicked() throws IOException, InterruptedException {

        final WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setJavaScriptEnabled(false);

        for (int i = 0; i < 1; i++) {
            Thread.sleep(50);

//            HtmlPage htmlPage1 = (HtmlPage) webClient.getPage("https://www.sreality.cz/detail/prodej/byt/1+1/praha-vrsovice-kosicka/4259829852#img=0&fullscreen=false");
//            HtmlPage htmlPage2 = (HtmlPage) webClient.getPage("https://www.sreality.cz/detail/prodej/byt/1+kk/praha-nove-mesto-na-rybnicku/932460636#img=0&fullscreen=false");
            HtmlPage htmlPage1 = (HtmlPage) webClient.getPage("https://javascript.spotrebitelskepravo.cz/");

            // copy the first page to preserve it if the click is JS driven ...
            HtmlPage htmlPage1Copy = new HtmlPage(htmlPage1.getWebResponse(), htmlPage1.getEnclosingWindow());

            int lenghtBeforeClick = htmlPage1.asXml().length();
            Page newPageAfterClick = htmlPage1.getByXPath("/html/body/div/nav/div[1]/a").stream().map(o -> (HtmlElement) o).findFirst().get().click();
            int lengthAfterClick = htmlPage1.asXml().length();
            int newPageLength = ((HtmlPage) newPageAfterClick).asXml().length();

            System.out.println(lenghtBeforeClick == lengthAfterClick);
            System.out.println(lenghtBeforeClick == newPageLength);

            System.out.println(htmlPage1.getEnclosingWindow());
            System.out.println(newPageAfterClick.getEnclosingWindow());
        }

        webClient.close();

    }

}
