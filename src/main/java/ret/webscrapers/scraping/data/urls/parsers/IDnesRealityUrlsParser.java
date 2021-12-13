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

package ret.webscrapers.scraping.data.urls.parsers;

import aaanew.drivers.DriverOperatorBase;
import aaanew.drivers.HtmlUnitDriverManager;
import aaanew.utils.HtmlUnitUtils;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class IDnesRealityUrlsParser extends DriverOperatorBase<WebClient> implements UrlsParser {

    private static final Logger log = LogManager.getLogger();
    HtmlPage page = null;
    private Set<String> lastScrapedUrls = new HashSet<>();
    private boolean hasMoreToScrape = true;

    public IDnesRealityUrlsParser(HtmlUnitDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public void goToPage(String pageUrl) {
        try {
            page = (HtmlPage) driverManager.getDriver().getPage(pageUrl);
        } catch (IOException e) {
            log.error("Error shile getting page: {}", pageUrl);
        }
    }

    @Override
    public boolean hasMoreUrlsToScrape() {
        log.info("hasMoreToScrape = {}", hasMoreToScrape);
        return hasMoreToScrape;
    }

    @Override
    public Set<String> scrapePropertyUrls() {

        Set<String> result = new HashSet<>();
        List<DomElement> allUrlsDivs = HtmlUnitUtils.getAllChildElements(page.getBody(), "div", "class", "c-list-products__content", true);

        for (DomElement urlsDiv : allUrlsDivs) {
            Optional<String> inzeratUrlOpt = HtmlUnitUtils.getAttributeValueByItsExpectedPart(urlsDiv, "a", "href", "/detail");
            if (inzeratUrlOpt.isPresent()) {
                String sanitizedUrl = sanitizeUrl(inzeratUrlOpt.get());
                result.add("https://reality.idnes.cz" + sanitizedUrl);
            }
        }
        updateHasMoreUrlsToScrape(result);
        lastScrapedUrls.clear();
        lastScrapedUrls.addAll(result);

        return result;
    }

    private void updateHasMoreUrlsToScrape(Set<String> result) {
        if (lastScrapedUrls.containsAll(result) || result.isEmpty()) {
            hasMoreToScrape = false;
            logFinished();
        } else {
            hasMoreToScrape = true;
        }
    }

    protected void logFinished() {
        log.info("No more urls to scrape.");
    }

    @Override
    public void reset() {
        hasMoreToScrape = true; // this is actually the default !
        lastScrapedUrls.clear();
        log.info("Reset parser");
    }

    private String sanitizeUrl(String inzeratUrl) {
        int questionMarkIdx = inzeratUrl.indexOf("?");
        if (questionMarkIdx > -1 && inzeratUrl.length() > 0) {
            return inzeratUrl.substring(0, questionMarkIdx);
        } else {
            return inzeratUrl;
        }
    }
}
