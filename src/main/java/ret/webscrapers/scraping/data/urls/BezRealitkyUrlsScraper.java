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
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BezRealitkyUrlsScraper extends UrlsScraperBase {

    private static final Logger log = LogManager.getLogger(BezRealitkyUrlsScraper.class);

    BezRealitkyUrlsScraper(UrlsParser urlsParser) {
        super(urlsParser);
    }


    @Override
    public void initiate() {
        cleanUp();
        scrapingInProgress = true;
    }

    @Override
    public void gotoNextPage(String segmentUrl) {
        incrementCurrentPage();
        urlsParser.goToPage(segmentUrl);
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
    public List<String> scrapeInzeratUrls(String segmentUrl) {
        if (hasSegmentMoreUrls()) {
            Set<String> allPropertyUrls  = urlsParser.scrapePropertyUrls();
            log.info("Parsed {} property urls.", allPropertyUrls.size());
            return new ArrayList<>(allPropertyUrls);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public void cleanUp() {
        cleanUpCommon();
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
