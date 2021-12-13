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

package ret.webscrapers.data.send.handlers;


import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.wrappers.JsonWrapper;
import ret.webscrapers.http.HttpClient;
import ret.webscrapers.http.HttpResponse;
import ret.webscrapers.http.ResponseType;

import java.util.function.Supplier;

public class GeneralSenderHandler extends SenderHandlerBase {

    private static Logger log = LogManager.getLogger(GeneralSenderHandler.class);

    public GeneralSenderHandler(HttpClient httpClient, String url) {
        super(httpClient, url);
    }

    @Override
    public SendResult send(JsonWrapper jsonWrapper) {
        if (jsonWrapper.getJson().isPresent()) {
            log.debug("Sending JsonWrapper: {}", jsonWrapper.getJson().get());
            RequestBody requestBody = RequestBody.create(MediaType.parse("Application/Json"), jsonWrapper.getJson().get());
            Supplier<HttpResponse<Void>> requestCode = () -> httpClient.makeRequest(url, HttpClient.Method.POST, requestBody, null, true);
            HttpResponse<Void> response = makeRequest(100, 1000, requestCode);
            log.info("Got response for dataType '{}'. Response: {}", jsonWrapper.getDataType(), response.getResponseType());
            boolean success = response.getResponseType() == ResponseType.SUCCESS;
            return new SendResult(success, jsonWrapper);
        }
        return new SendResult(false, jsonWrapper);
    }

}
