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

package ret.webscrapers.data.repo.remove.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.webscrapers.data.model.DataFile;

public class EmptyRemoverHandler implements RemoverHandler {

    private static Logger log = LogManager.getLogger(EmptyRemoverHandler.class);

    @Override
    public boolean remove(DataFile dataFile) {
        log.error("Got request to remove data for unhandled type of file: '{}' !!!!!", dataFile.getDataFile().getName());
        return false;
    }
}
