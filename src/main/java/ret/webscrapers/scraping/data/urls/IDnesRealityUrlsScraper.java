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

package ret.webscrapers.scraping.data.urls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.webscrapers.scraping.data.urls.parsers.UrlsParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IDnesRealityUrlsScraper extends UrlsScraperBase {

    private static final Logger log = LogManager.getLogger();

    private final Set<String> allPropertyUrls = new HashSet<>();

    public IDnesRealityUrlsScraper(UrlsParser urlsParser) {
        super(urlsParser);
    }

    @Override
    public void initiate() {
        cleanUp();
        scrapingInProgress = true;
    }


    @Override
    public boolean hasSegmentMoreUrls() {
        if (getCurrentPage() == 0) {
            return true;
        } else {
            return urlsParser.hasMoreUrlsToScrape();
        }
    }


    @Override
    public void gotoNextPage(String segmentUrl) {
        incrementCurrentPage();
        String pageUrl = makePageUrl(segmentUrl, getCurrentPage());
        log.info("Next page: {}", pageUrl);
        urlsParser.goToPage(pageUrl);
    }

    @Override
    public List<String> scrapeInzeratUrls(String segmentUrl) {

        Set<String> urlsToScrape = new HashSet<>();

        if (urlsParser.hasMoreUrlsToScrape()) {
            Set<String> propertyUrls = urlsParser.scrapePropertyUrls();
            log.info("Parsed {} property urls.", propertyUrls.size());
            for (String propertyUrl : propertyUrls) {
                if (!allPropertyUrls.contains(propertyUrl)) {
                    urlsToScrape.add(propertyUrl);
                    allPropertyUrls.add(propertyUrl);
                }
            }
        } else {
            log.info("No more urls to scrape.");
        }

        return new ArrayList<>(urlsToScrape);
    }

    @Override
    public void cleanUp() {
        cleanUpCommon();
        allPropertyUrls.clear();
    }



    private String makePageUrl(String segmentUrl, int currentPage) {
        // page no. == 1:
        // https://reality.idnes.cz/s/byty/olomoucky-kraj/

        // page no. > 1
        // https://reality.idnes.cz/s/byty/olomoucky-kraj/?page=1

        String pageUrl;
        if (currentPage == 1) {
            pageUrl = segmentUrl;
        } else {
            String stranaPattern = "?page=%d";
            String strana = String.format(stranaPattern, currentPage - 1);
            pageUrl = segmentUrl + strana;
        }
        return pageUrl;
    }

    @Override
    public boolean terminateDriver() {
        return urlsParser.terminateDriver();
    }

    @Override
    public boolean quitDriverIfIdle() {
        return urlsParser.quitDriverIfIdle();
    }

    @Override
    public void restartDriverImmediately() {
        urlsParser.restartDriverImmediately();
    }

    @Override
    public boolean restartDriverIfNeeded() {
        return urlsParser.restartDriverIfNeeded();
    }

    @Override
    public boolean restartOrQuitDriverIfNeeded() {
        return urlsParser.restartOrQuitDriverIfNeeded();
    }

    @Override
    public void goToDefaultPage() {
        urlsParser.goToDefaultPage();
    }

}
