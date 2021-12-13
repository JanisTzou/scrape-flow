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

package ret.webscrapers.data;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.wrappers.AbstractDataWrapper;
import ret.appcore.model.wrappers.JsonWrapper;
import ret.webscrapers.AppConfig;
import ret.webscrapers.data.model.DataFile;
import ret.webscrapers.data.repo.read.UnsentFilesReader;
import ret.webscrapers.data.send.handlers.SendResult;
import ret.webscrapers.data.send.handlers.SenderHandler;

import java.util.List;

public class DataServiceImpl implements DataService {

    private static Logger log = LogManager.getLogger(DataServiceImpl.class);

    private final HttpHandlersManager handlersManager;
    private final UnsentFilesReader fileListReader;

    // constructor
    public DataServiceImpl(HttpHandlersManager handlersManager, UnsentFilesReader fileListReader) {
        this.handlersManager = handlersManager;
        this.fileListReader = fileListReader;
    }

    @Override
    public boolean sendData(AbstractDataWrapper data, boolean writeOnSendFailure) {
        JsonWrapper jsonWrapper = data.toJsonWrapper();
        return sendData(jsonWrapper, writeOnSendFailure);
    }

    @Override
    public boolean sendData(JsonWrapper jsonWrapper, boolean writeOnSendFailure) {
        if (AppConfig.dontSendNewDataWhileUnsentFilesExist && fileListReader.unsentFilesExist()) {
            writeData(jsonWrapper);
            log.warn("Could not send data, files awaiting sending exist.");
            return false;
        } else {
            return doSendData(jsonWrapper, writeOnSendFailure);
        }
    }

    private boolean doSendData(JsonWrapper jsonWrapper, boolean writeOnSendFailure) {
        SenderHandler senderHandler = handlersManager.getSenderHandler(jsonWrapper.getDataType());
        SendResult sendResult = senderHandler.send(jsonWrapper);
        if (!sendResult.isSuccessful() && writeOnSendFailure) {
            writeData(jsonWrapper);
        }
        return sendResult.isSuccessful();
    }

    @Override
    public boolean writeData(JsonWrapper jsonWrapper) {
        boolean writeSuccess = handlersManager.getWriterHandler(jsonWrapper.getDataType()).writeDataToDisc(jsonWrapper);
        if (!writeSuccess) {
            log.error("Failed to write to disc jsonWrapper - DATA LOST!");
        }
        return writeSuccess;
    }

    @Override
    public JsonWrapper readData(DataFile dataFile) {
        return handlersManager.getReaderHandler(dataFile.getDataType()).read(dataFile);
    }

    @Override
    public void tryResendStoredData() {
        // TODO it would be great if we could somehow trigger resending other than server restart ...
        List<DataFile> dataFiles = getDataFilesSortedByTs();
        log.info("Going to try to resend saved data from {} files", dataFiles.size());
        for (DataFile dataFile : dataFiles) {
            JsonWrapper jsonWrapper = readData(dataFile);
            boolean success = doSendData(jsonWrapper, false);
            if (success) {
                handlersManager.getRemoverHandler(dataFile.getDataType()).remove(dataFile);
            }
            log.info("Resend success = {} for file: {} a dtype = {}", success, dataFile.getDataFile(), dataFile.getDataType());
        }
        List<DataFile> dataFilesSortedByTs = getDataFilesSortedByTs();
        if (dataFilesSortedByTs.size() > 0) {
            log.error("We have resent all saved data but there is some more: {}", dataFilesSortedByTs.stream().map(dataFile -> dataFile.getDataFile()));
        }
    }

    @Override
    public List<DataFile> getDataFilesSortedByTs() {
        return fileListReader.getSortedDataFiles();
    }

}
