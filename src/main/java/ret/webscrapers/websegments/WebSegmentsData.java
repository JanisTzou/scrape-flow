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

package ret.webscrapers.websegments;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.WebSegment;
import ret.webscrapers.AppConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class WebSegmentsData {

    private static Logger log = LogManager.getLogger(WebSegmentsData.class);

    /**
     * Holds all default segments pertaining to this web scraper instance.
     */
    private Set<WebSegment> allWebSegmentsForThisScraper = Collections.synchronizedSet(new HashSet<>());

    /**
     * this should only hold web segments pertaining to this instance of webscrapers ...
     * pulls data at app's startup
     */
    private final Map<Integer, WebSegment> unprocessedSegmentIdToSegments = new ConcurrentHashMap<>();

    /**
     * ids of all web segments that have already been pulled in the current cycle of pulling
     */
    private final Set<WebSegment> processedSegments = Collections.synchronizedSet(new HashSet<>());



    // constructor
    public WebSegmentsData() {}



    // return 'true' if webSegments for this particular scraper client were received ...
    public boolean initializeSegmentsForThisScraper(List<WebSegment> webSegments) {
        if (webSegments != null && !webSegments.isEmpty()) {
            Set<WebSegment> scraperSegments = Collections.synchronizedSet(new HashSet<>());
            for (WebSegment webSegment : webSegments) {
                if (webSegment.getScraperClientId() == AppConfig.scraperClientId && webSegment.getActive()) {
                    scraperSegments.add(webSegment);
                    log.info("Got segment for this client: " + webSegment);
                }
            }
            if (scraperSegments.isEmpty()) {
                log.error("NO SEGMENTS RECEIVED FOR THIS CLIENT with id = {}", AppConfig.scraperClientId);
                return false;
            } else {
                this.allWebSegmentsForThisScraper = scraperSegments;
                allSegmentsToUnprocessed();
                return true;
            }
        } else {
            log.error("Got empty WebSegments list");
            return false;
        }
    }


    public Optional<WebSegment> nextWebSegment() {
        if (unprocessedSegmentIdToSegments.isEmpty()) {
            return Optional.empty();
        } else {
            Optional<WebSegment> next = unprocessedSegmentIdToSegments.values().stream().sorted(Comparator.comparingInt(WebSegment::getId)).findFirst();
            return next;
        }
    }


    public void addProcessed(WebSegment segment) {
        unprocessedSegmentIdToSegments.remove(segment.getId());
        processedSegments.add(segment);
    }


    public void reset() {
        processedSegments.clear();
        unprocessedSegmentIdToSegments.clear();
        allSegmentsToUnprocessed();
    }


    private void allSegmentsToUnprocessed() {
        for (WebSegment webSegment : allWebSegmentsForThisScraper) {
            unprocessedSegmentIdToSegments.put(webSegment.getId(), webSegment);
        }
    }


    public Set<WebSegment> processedSegments() {
        HashSet<WebSegment> webSegments = new HashSet<>(processedSegments.size());
        webSegments.addAll(processedSegments);
        return webSegments;
    }

    public int unprocessedCount() {
        return unprocessedSegmentIdToSegments.size();
    }


}
