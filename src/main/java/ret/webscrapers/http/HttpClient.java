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

package ret.webscrapers.http;


import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ret.appcore.json.Json;
import ret.webscrapers.AppConfig;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class HttpClient {

    private final static Logger log = LogManager.getLogger(HttpClient.class);

    private final OkHttpClient client;
    private volatile boolean isConnected;
    private List<ConnectionListener> connectionListeners = new ArrayList<>();


    public enum Method {
        GET, POST, PUT, DELETE;
    }

    public HttpClient(OkHttpClient client) {
        this.client = client;
        log.info("About to send HttpClient initialisation heartbeat.");
    }


    public Request buildRequest(String url, Method method, @Nullable RequestBody requestBody) {
        Request.Builder rqBuilder = new Request.Builder();
        rqBuilder.method(method.name(), requestBody);
        return rqBuilder.url(url)
                .addHeader("Authorization", Credentials.basic(RetApiConfig.adminUserName, RetApiConfig.adminPassword))
                .build();
    }

    public <T> HttpResponse<T> makeRequest(String url, Method method, @Nullable RequestBody requestBody, Class<T> responseDataType, boolean makeRequestOnlyIfConnected) {
        if (makeRequestOnlyIfConnected && ! isConnected) {
            return new HttpResponse<T>(ResponseType.NO_CONNECTION_OR_TIMEOUT);
        }
        Request request = buildRequest(url, method, requestBody);
        return makeRequest(request, responseDataType, makeRequestOnlyIfConnected);
    }


    public <T> HttpResponse<T> makeRequest(Request request, Class<T> responseDataType, boolean makeRequestOnlyIfConnected) {
        if (makeRequestOnlyIfConnected && ! isConnected) {
            return new HttpResponse<T>(ResponseType.NO_CONNECTION_OR_TIMEOUT);
        }
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            log.debug("HTTP response code: {}", response.code());
            return processResponse(response, responseDataType);
        } catch (IOException e) {
            return new HttpResponse<T>(ResponseType.NO_CONNECTION_OR_TIMEOUT);
        }
    }

    // TODO reconsider this ... it's a mess
    private <T> HttpResponse<T> processResponse(Response response, Class<T> responseDataType) {
        try {
            int code = response.code();
            String body = response.body().string();
            Request request = response.request();
            if (code < 300) {
                if (body.equals("") || responseDataType == null) {
                    return new HttpResponse<>(ResponseType.SUCCESS);
                }
                Optional<T> dataOp = Json.parse(body, responseDataType);
                if (dataOp.isPresent()) {
                    T data = dataOp.get();
                    return new HttpResponse<>(ResponseType.SUCCESS, data);
                } else {
                    log.error("Failed to parse data for type {} from response body: {}", responseDataType, body);
                    return new HttpResponse<>(ResponseType.ERROR);
                }
            } else if (code == 404) {
                return new HttpResponse<>(ResponseType.NO_DATA);
            } else if (!response.isSuccessful()) {
                log.error("Got unsuccessful response code: {}", code);
                return new HttpResponse<>(ResponseType.FAILED);
            } else {
                log.error("Got unexpected response code: {} with body: {}", code, body);
                return new HttpResponse<>(ResponseType.ERROR);
            }
        } catch (Exception e) {
            log.error("Error reading body from response.", e);
            return new HttpResponse<>(ResponseType.ERROR);
        } finally {
            response.close();
        }
    }


    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public void sendInitialisationHeartbeat() {
        sendHeartbeat();
    }

    public void sendHeartbeat() {
        log.info("Sending heartbeat!");
        String heartbeatURL = String.format(RetApiConfig.heartbeatURLPattern, AppConfig.scraperClientId);
        HttpResponse<String> httpResponse = makeRequest(heartbeatURL, Method.GET, null, String.class, false);
        if (httpResponse.getResponseType().equals(ResponseType.SUCCESS)) {
            log.info("Connected to server");
            setConnectedStatusIfChanged(true);
        } else {
            log.info("Disconnected from server");
            setConnectedStatusIfChanged(false);
        }
    }

    private void setConnectedStatusIfChanged(boolean isConnectedStatus) {
        if (this.isConnected && !isConnectedStatus) {
            setConnected(false);
            log.warn(">>> DISCONNECTED FROM RetApp !!!!! <<< ");
            for (ConnectionListener connectionListener : connectionListeners) {
                connectionListener.onDisconnected();
            }
        } else if (! this.isConnected && isConnectedStatus) {
            setConnected(true);
            log.info(">>> RE-CONNECTED TO RetApp! <<< ");
            for (ConnectionListener connectionListener : connectionListeners) {
                connectionListener.onConnected();
            }
        } else {
            // connection status unchanged
        }
    }

    public void register(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public void unregister(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }


}
