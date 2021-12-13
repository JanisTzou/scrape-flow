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


import ret.appcore.model.wrappers.DataType;
import ret.webscrapers.data.repo.read.handlers.EmptyReaderHandler;
import ret.webscrapers.data.repo.read.handlers.GeneralReaderHandler;
import ret.webscrapers.data.repo.read.handlers.InzeratAndImageReaderHandler;
import ret.webscrapers.data.repo.read.handlers.ReaderHandler;
import ret.webscrapers.data.repo.remove.handlers.EmptyRemoverHandler;
import ret.webscrapers.data.repo.remove.handlers.GeneralRemoverHandler;
import ret.webscrapers.data.repo.remove.handlers.InzeratAndImageRemoverHandler;
import ret.webscrapers.data.repo.remove.handlers.RemoverHandler;
import ret.webscrapers.data.repo.write.handlers.EmptyWriterHandler;
import ret.webscrapers.data.repo.write.handlers.GeneralWriterHandler;
import ret.webscrapers.data.repo.write.handlers.InzeratAndImageWriterHandler;
import ret.webscrapers.data.repo.write.handlers.WriterHandler;
import ret.webscrapers.data.send.handlers.EmptySenderHandler;
import ret.webscrapers.data.send.handlers.GeneralSenderHandler;
import ret.webscrapers.data.send.handlers.InzeratAndImageSenderHandler;
import ret.webscrapers.data.send.handlers.SenderHandler;
import ret.webscrapers.http.HttpClient;
import ret.webscrapers.http.RetApiConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HttpHandlersManager {

    private ConcurrentMap<DataType, SenderHandler> sendHandlers = new ConcurrentHashMap<>();
    private ConcurrentMap<DataType, WriterHandler> writerHandlers = new ConcurrentHashMap<>();
    private ConcurrentMap<DataType, ReaderHandler> readerHandlers = new ConcurrentHashMap<>();
    private ConcurrentMap<DataType, RemoverHandler> removedHandlers = new ConcurrentHashMap<>();

    public HttpHandlersManager(HttpClient httpClient, String jsonsDir, String imagesDir) {
        initHandlers(httpClient, jsonsDir, imagesDir);
    }


    private void initHandlers(HttpClient httpClient, String jsonsDir, String imagesDir) {
        sendHandlers.put(DataType.INZERAT_DATA, new InzeratAndImageSenderHandler(httpClient, RetApiConfig.inzeratImagesDataURL));
        sendHandlers.put(DataType.WEBSEGMENT_STATS, new GeneralSenderHandler(httpClient, RetApiConfig.finishedWebsegmentDataURL));
        sendHandlers.put(DataType.CYCLE_STARTED, new GeneralSenderHandler(httpClient, RetApiConfig.startedCycleDataURL));
        sendHandlers.put(DataType.CYCLE_FINISHED, new GeneralSenderHandler(httpClient, RetApiConfig.finishedCycleDataURL));

        writerHandlers.put(DataType.INZERAT_DATA, new InzeratAndImageWriterHandler(jsonsDir, imagesDir));
        writerHandlers.put(DataType.WEBSEGMENT_STATS, new GeneralWriterHandler(jsonsDir));
        writerHandlers.put(DataType.CYCLE_STARTED, new GeneralWriterHandler(jsonsDir));
        writerHandlers.put(DataType.CYCLE_FINISHED, new GeneralWriterHandler(jsonsDir));

        readerHandlers.put(DataType.INZERAT_DATA, new InzeratAndImageReaderHandler());
        readerHandlers.put(DataType.WEBSEGMENT_STATS, new GeneralReaderHandler());
        readerHandlers.put(DataType.CYCLE_STARTED, new GeneralReaderHandler());
        readerHandlers.put(DataType.CYCLE_FINISHED, new GeneralReaderHandler());

        removedHandlers.put(DataType.INZERAT_DATA, new InzeratAndImageRemoverHandler());
        removedHandlers.put(DataType.WEBSEGMENT_STATS, new GeneralRemoverHandler());
        removedHandlers.put(DataType.CYCLE_STARTED, new GeneralRemoverHandler());
        removedHandlers.put(DataType.CYCLE_FINISHED, new GeneralRemoverHandler());
    }


    public SenderHandler getSenderHandler(DataType dataType) {
        return sendHandlers.getOrDefault(dataType, new EmptySenderHandler());
    }

    public WriterHandler getWriterHandler(DataType dataType) {
        return writerHandlers.getOrDefault(dataType, new EmptyWriterHandler());
    }

    public ReaderHandler getReaderHandler(DataType dataType) {
        return readerHandlers.getOrDefault(dataType, new EmptyReaderHandler());
    }

    public RemoverHandler getRemoverHandler(DataType dataType) {
        return removedHandlers.getOrDefault(dataType, new EmptyRemoverHandler());
    }

}
