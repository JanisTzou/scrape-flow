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

package com.github.web.scraping.lib.drivers;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;

import java.io.IOException;

public class TestHtmlUnitWebClient {

    @Test
    public void testInitialisationEfficiency() throws IOException {

        long start = System.currentTimeMillis();

        for (int i = 0; i < 1; i++) {
            final WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(false);
//            HtmlPage htmlPage = (HtmlPage) webClient.getPage("https://www.sreality.cz/detail/prodej/byt/2+kk/praha-hloubetin-u-elektry/1213263452#img=0&fullscreen=false");
//            HtmlPage htmlPage = (HtmlPage) webClient.getPage("https://www.bezrealitky.cz/nemovitosti-byty-domy/576696-nabidka-prodej-domu");
//            HtmlPage htmlPage = (HtmlPage) webClient.getPage("https://www.ceskereality.cz/byty/byty-3-1/?id=FFO69766J06220&sfset=sff%3D1%7Coperace%3D0%7Ctyp%3D200%7Cpouze_kod_obce%3D0%7Cnegacetypu%3D0%7Cid%3D%7Crozcestnik%3D%7Csf_kde%3D5%7Cscroll-y%3D538");
//            HtmlPage htmlPage = (HtmlPage) webClient.getPage("https://realitymix.cz/detail/praha/byt-3-1-s-prostornym-balkonem-v-objektu-dvorecke-namesti-v-oblibene-lokalite-praha-podoli-nyni-s-7152429.html");
//            HtmlPage htmlPage = (HtmlPage) webClient.getPage("https://reality.idnes.cz/detail/prodej/byt/praha-1-karoliny-svetle/5c9b957be880541c3e587f56/?s-et=flat&s-l=VUSC-19");
            // realcity.cz -> tihle maji asi blby mapy .. nedaji se parsovat coordinates ...
//            HtmlPage htmlPage = (HtmlPage) webClient.getPage("https://www.realcity.cz/nemovitost/pronajem-bytu-2-1-praha-10-na-vysocine-3837769");
//            HtmlPage htmlPage = (HtmlPage) webClient.getPage("https://www.reality.cz/prodej/byty/obvod-Praha-4/cena-0-2000000-2/FE2-003129/?c=3");

            HtmlPage htmlPage = (HtmlPage) webClient.getPage("https://www.reality.cz/prodej/byty/obvod-Praha-4/cena-0-2000000-2/FE2-003129/?c=3");


            System.out.println(htmlPage.asXml());
//            WebWindow window = webClient.openWindow(new URL("https://www.sreality.cz/detail/prodej/byt/2+kk/praha-hloubetin-u-elektry/1213263452#img=0&fullscreen=false"), "window");
//            HtmlPage htmlPage = (HtmlPage) window.getEnclosedPage();
            webClient.close();
        }

        long finish = System.currentTimeMillis();

        System.out.println(finish - start);

    }

}
