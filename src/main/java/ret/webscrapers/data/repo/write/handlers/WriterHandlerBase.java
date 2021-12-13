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

package ret.webscrapers.data.repo.write.handlers;





import ret.appcore.model.wrappers.DataType;
import ret.appcore.model.wrappers.JsonWrapper;
import ret.appcore.utils.FileUtils;

import java.io.File;

public abstract class WriterHandlerBase implements WriterHandler {

    private final String baseDataDir;
    private final String dataFileNamePattern = "%s_%s_%d.json";  // format: DataType_UUIDpart_Timestamp.json

    public WriterHandlerBase(String baseDataDir) {
        this.baseDataDir = baseDataDir;
    }

    @Override
    public boolean writeDataToDisc(JsonWrapper jsonWrapper) {
        if (jsonWrapper.getJson().isPresent()) {
            String fileName = makeFileName(jsonWrapper);
            ensureDirExists(baseDataDir);
            // TODO make throw exception on failure ...
            FileUtils.writeFile(new File(baseDataDir + File.separator + fileName), jsonWrapper.getJson().get());
            return true;
        } else {
            return false;
        }
    }

    String makeFileName(JsonWrapper data) {
        DataType dataType = data.getDataType();
        String uuidPart = data.getUuid();
        long now = System.currentTimeMillis();
        return String.format(dataFileNamePattern, dataType.getShortName(), uuidPart, now);
    }

    protected void ensureDirExists(String dir) {
        ensureDirExists(new File(dir));
    }

    protected void ensureDirExists(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
