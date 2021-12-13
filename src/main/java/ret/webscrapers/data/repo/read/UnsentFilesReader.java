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

package ret.webscrapers.data.repo.read;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.wrappers.DataType;
import ret.webscrapers.data.model.DataFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class UnsentFilesReader {

    private static Logger log = LogManager.getLogger(UnsentFilesReader.class);
    private static final int EXPECTED_FILE_NAME_PARTS_NUM = 3;
    private static final int TIMESTAMP_LEN = 13;
    private final String jsonDataDir;
    private final String imageDataDir;

    public UnsentFilesReader(String jsonDataDir, String imageDataDir) {
        this.jsonDataDir = jsonDataDir;
        this.imageDataDir = imageDataDir;
    }

    public boolean unsentFilesExist() {
        boolean filesExist = false;
        Path dir = FileSystems.getDefault().getPath(jsonDataDir);
        if (!dir.toFile().exists()) {
            return false;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir);) {
            for (Path path : stream) {
                filesExist = isJsonFile(path.toString());
                if (filesExist) {
                    break;
                }
            }
        } catch (IOException e) {
            filesExist = true;
            log.error("Exception while checking unsent files, returning unsentFilesExist = true but we canot be sure.");
        }
        return filesExist;
    }

    public List<DataFile> getSortedDataFiles() {
        File[] files = readFilesFromDisc(jsonDataDir);
        List<DataFile> fileList = filterDataFiles(files);
        Collections.sort(fileList);
        return fileList;
    }


    private List<DataFile> filterDataFiles(File[] files) {
        List<DataFile> result = new ArrayList<>();
        for (File file : files) {
            Optional<DataFile> dataFileOp = toDataFile(file);
            dataFileOp.ifPresent(result::add);
        }
        return result;
    }


    private Optional<DataFile> toDataFile(File file) {

        String fName = file.getName();
        boolean isJson = isJsonFile(fName);

        if (!isJson) {
            log.warn("File is not of type JSON - ignoring: {}", fName);
            return Optional.empty();
        }

        fName = fName.replace(".json", "");
        String[] parts = fName.split("_");

        if (parts.length != EXPECTED_FILE_NAME_PARTS_NUM) {
            log.warn("File name does not have expected number of parts [{}] - ignoring: {}", EXPECTED_FILE_NAME_PARTS_NUM, fName);
            return Optional.empty();
        }

        String dataTypePart = parts[0];
        String uuidPart = parts[1];
        String timestampPart = parts[2];

        if (!isValidTimestamp(timestampPart)) {
            log.warn("File name does not contain a valid timestamp value at expected position - ignoring: {}", fName);
            return Optional.empty();
        }

        if (!isValidUUID(uuidPart)) {
            log.warn("File name does not contain a valid UUID value at expected position - ignoring: {}", fName);
            return Optional.empty();
        }

        Optional<DataType> dataTypeOp = DataType.mapDataTypeFor(dataTypePart);
        if (!dataTypeOp.isPresent()) {
            log.warn("File name does not contain a valid DataType value at expected position - ignoring: {}", fName);
            return Optional.empty();
        }

        Optional<File> imagesDir = getImagesDir(dataTypeOp.get(), uuidPart);

        log.info("UnsentFilesReader: read file =  {} of type =  {} for uuid = {}", file, dataTypeOp.get(), uuidPart);

        DataFile dataFile = new DataFile(file, imagesDir, dataTypeOp.get(), uuidPart, Long.valueOf(timestampPart));
        return Optional.of(dataFile);
    }

    private boolean isJsonFile(String fName) {
        return fName.endsWith(".json");
    }


    private Optional<File> getImagesDir(DataType dataType, String uuid) {
        if (dataType == DataType.INZERAT_DATA) {
            File dir = new File(imageDataDir + File.separator + uuid);
            if (dir.exists()) {
                return Optional.of(dir);
            }
        }
        return Optional.empty();
    }


    private File[] readFilesFromDisc(String dataDir) {
        File dir = new File(dataDir);
        File[] files = null;
        try {
            files = dir.listFiles();
        } catch (Exception e) {
            log.error("Error trying to read json files from dir: {}", dir, e);
        }
        if (files == null) {
            files = new File[0];
        }
        return files;
    }

    private boolean isValidTimestamp(String value) {
        return isNumber(value) && value.length() == TIMESTAMP_LEN;
    }

    private boolean isValidUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNumber(String value) {
        try {
            Long.valueOf(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
