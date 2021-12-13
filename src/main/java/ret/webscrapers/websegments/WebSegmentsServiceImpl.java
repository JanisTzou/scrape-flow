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
import ret.appcore.model.scraping.WebSegmentList;
import ret.webscrapers.AppConfig;
import ret.webscrapers.http.*;
import ret.webscrapers.websegments.model.WebSegmentProcessingProgress;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public class WebSegmentsServiceImpl extends HttpServiceBase implements WebSegmentsService {

    private static Logger log = LogManager.getLogger(WebSegmentsServiceImpl.class);
    private WebSegmentsData segmentsData;
    private final WebsegmentsProgressFileHandler fileHandler;
    private final Duration lastProcessedSegmentsAgeToConsider;


    /**
     *
     * @param httpClient
     * @param segmentsData
     * @param fileHandler
     * @param lastProcessedSegmentsAgeToConsider - The age of the last progressed websegment so that it is not ignored and we start scraping from that segment ...
     */
    public WebSegmentsServiceImpl(HttpClient httpClient,
                                  WebSegmentsData segmentsData,
                                  WebsegmentsProgressFileHandler fileHandler,
                                  Duration lastProcessedSegmentsAgeToConsider) {
        super(httpClient);
        this.segmentsData = segmentsData;
        this.fileHandler = fileHandler;
        this.lastProcessedSegmentsAgeToConsider = lastProcessedSegmentsAgeToConsider;
    }


    @Override
    public synchronized Optional<WebSegment> nextWebSegment() {
        Optional<WebSegment> webSegmentOp = segmentsData.nextWebSegment();
        return webSegmentOp;
    }

    @Override
    public synchronized void processFinished(WebSegment segmentStats) {
        segmentsData.addProcessed(segmentStats);
        WebSegmentProcessingProgress webSegmentProcessingProgress
                = new WebSegmentProcessingProgress(LocalDateTime.now(), segmentsData.processedSegments());
        fileHandler.writeProcessingProgress(webSegmentProcessingProgress);
    }


    @Override
    public boolean initialiseOrUpdate() {
        boolean success = false;
        String allWebSegmentsUrl = RetApiConfig.makeAllWebSegmentsUrl(AppConfig.scraperClientId);
        HttpResponse<WebSegmentList> httpResponse = httpClient.makeRequest(allWebSegmentsUrl, HttpClient.Method.GET, null, WebSegmentList.class, true);
        if (httpResponse.getResponseType().equals(ResponseType.SUCCESS)) {
            if (httpResponse.getData().isPresent()) {
                List<WebSegment> webSegments = httpResponse.getData().get().getWebSegments();
                success = segmentsData.initializeSegmentsForThisScraper(webSegments);
                if (success) {
                    handleLastProcessedSegments();
                }
            }
        }
        return success;
    }


    /**
     * We remove last processed segments from the list of segments to process if they were processed recently.
     * Otherwise we process all of them anew.
     */
    private void handleLastProcessedSegments() {
        Optional<WebSegmentProcessingProgress> lastProgressOp = fileHandler.readWebSegmentProcessingProgress();
        if (lastProgressOp.isPresent()) {
            WebSegmentProcessingProgress lastProgress = lastProgressOp.get();
            boolean doConsider = LocalDateTime.now().isBefore(lastProgress.getLastSegmentProcessedDt().plusSeconds(lastProcessedSegmentsAgeToConsider.getSeconds()));
            if (doConsider) {
                log.info("We have some processed segments from last cycle that we can ignore:");
                // make segments processed so we start from the next available ...
                for (WebSegment processedSegment : lastProgress.getProcessedSegments()) {
                    segmentsData.addProcessed(processedSegment);
                    log.info("Ignoring segment (added to processed): {}", processedSegment);
                }
                // pojistka
                if (segmentsData.unprocessedCount() == 0) {
                    log.warn("After handling last processed segments that number of segments left to process is ZERO! Some error must have occure. Reseting the service!");
                    reset();
                }
            } else {
                // we do not need it ...
                fileHandler.removeProcessedProgress();
            }
        }
    }

    @Override
    public void reset() {
        log.info("Resetting WebSegmentsServiceImpl.");
        segmentsData.reset();
        fileHandler.removeProcessedProgress();
    }


}
