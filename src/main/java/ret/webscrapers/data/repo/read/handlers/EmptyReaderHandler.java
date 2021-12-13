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

package ret.webscrapers.data.repo.read.handlers;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.wrappers.JsonWrapper;
import ret.webscrapers.data.model.DataFile;

import java.util.Optional;

public class EmptyReaderHandler implements ReaderHandler {

    private static Logger log = LogManager.getLogger(EmptyReaderHandler.class);

    @Override
    public JsonWrapper read(DataFile dataFile) {
        log.error("Got request to read data from unhandled type for file: '{}' !!!!!", dataFile.getDataFile().getName());
        return new JsonWrapper(dataFile.getDataType(), dataFile.getUuid(), Optional.empty());
    }
}
