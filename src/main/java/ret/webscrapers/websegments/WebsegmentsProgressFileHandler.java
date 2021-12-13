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
import ret.appcore.json.Json;
import ret.appcore.utils.FileUtils;
import ret.webscrapers.websegments.model.WebSegmentProcessingProgress;

import java.io.File;
import java.util.Optional;

public class WebsegmentsProgressFileHandler {

    private static Logger log = LogManager.getLogger(WebsegmentsProgressFileHandler.class);
    private final String dirPath;
    private final String fileName;

    public WebsegmentsProgressFileHandler(String dirPath, String fileName) {
        this.dirPath = dirPath;
        this.fileName = fileName;
    }


    public void writeProcessingProgress(WebSegmentProcessingProgress segmentInProcessing) {
        File file = new File(fileName);
        makeSureDirExists();
        Optional<String> json = Json.write(segmentInProcessing);
        if (json.isPresent()) {
            FileUtils.writeFile(file, json.get());
        } else {
            log.error("Could not write WebSegmentInProcessing object into json: " + segmentInProcessing);
        }
    }

    public void removeProcessedProgress() {
        File file = new File(fileName);
        file.delete();
    }

    private void makeSureDirExists() {
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();
    }


    public Optional<WebSegmentProcessingProgress> readWebSegmentProcessingProgress() {
        File file = new File(fileName);
        if (file.exists()) {
            Optional<WebSegmentProcessingProgress> segmentInProcessingOp = Json.parse(file, WebSegmentProcessingProgress.class);
            if (!segmentInProcessingOp.isPresent()) {
                log.error("Error reading data from file {}", fileName);
            } else {
                return segmentInProcessingOp;
            }
        }
        return Optional.empty();
    }

}
