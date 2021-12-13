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

package ret.webscrapers.data.model;



import ret.appcore.model.wrappers.DataType;

import java.io.File;
import java.util.Optional;

public class DataFile implements Comparable<DataFile> {

    private final File dataFile;
    private final Optional<File> imagesDir;
    private final DataType dataType;
    private final String uuid;
    private final long timestamp;

    public DataFile(File dataFile, Optional<File> imagesDir, DataType dataType, String uuid, long timestamp) {
        this.dataFile = dataFile;
        this.imagesDir = imagesDir;
        this.dataType = dataType;
        this.uuid = uuid;
        this.timestamp = timestamp;
    }

    public File getDataFile() {
        return dataFile;
    }

    public Optional<File> getImagesDir() {
        return imagesDir;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getUuid() {
        return uuid;
    }

    public long getTimestamp() {
        return timestamp;
    }


    @Override
    public int compareTo(DataFile other) {
        if (this.timestamp < other.timestamp) {
            return -1;
        } else if (this.timestamp == other.timestamp) {
            return 0;
        } else {
            return 1;
        }
    }


    @Override
    public String toString() {
        return "DataFile{" +
                "dataFile=" + dataFile +
                ", imagesDir=" + imagesDir +
                ", dataType=" + dataType +
                ", uuid='" + uuid + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
