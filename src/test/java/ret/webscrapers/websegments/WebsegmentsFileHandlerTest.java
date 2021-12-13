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

import org.junit.Test;
import ret.appcore.model.WebSegment;
import ret.webscrapers.AppConfig;
import ret.webscrapers.websegments.model.WebSegmentProcessingProgress;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

public class WebsegmentsFileHandlerTest {

    @Test
    public void writeCurrentlyProcessedSegment() {

        WebsegmentsProgressFileHandler wsgHandler = new WebsegmentsProgressFileHandler(AppConfig.websegmentsDir, AppConfig.websegmentBeingProcessedFileName);
        WebSegment webSegment = new WebSegment();
        webSegment.setId(1);
        Set<WebSegment> segmentSet = Collections.singleton(webSegment);
        WebSegmentProcessingProgress webSegmentInProcessing = new WebSegmentProcessingProgress(LocalDateTime.now(), segmentSet);
        wsgHandler.writeProcessingProgress(webSegmentInProcessing);

    }

    @Test
    public void removeProcessedSegment() {

        WebsegmentsProgressFileHandler wsgHandler = new WebsegmentsProgressFileHandler(AppConfig.websegmentsDir, AppConfig.websegmentBeingProcessedFileName);
        wsgHandler.removeProcessedProgress();

    }
}
