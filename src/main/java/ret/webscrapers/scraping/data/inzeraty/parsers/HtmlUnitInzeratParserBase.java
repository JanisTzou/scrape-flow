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

package ret.webscrapers.scraping.data.inzeraty.parsers;

import aaanew.drivers.DriverManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class HtmlUnitInzeratParserBase extends InzeratParserBase<WebClient> {

    public HtmlUnitInzeratParserBase(DriverManager<WebClient> driverManager) {
        super(driverManager);
    }


    protected HtmlPage getHtmlPage(String inzeratUrl, WebClient webClient) throws MalformedURLException {
        String winName = "window_name";
        URL url = new URL(inzeratUrl);
        webClient.openWindow(url, winName);
        return (HtmlPage) webClient.getWebWindowByName(winName).getEnclosedPage();
    }

}
