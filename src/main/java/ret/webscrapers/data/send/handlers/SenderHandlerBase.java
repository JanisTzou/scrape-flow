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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.webscrapers.http.HttpClient;
import ret.webscrapers.http.HttpResponse;
import ret.webscrapers.http.HttpServiceBase;
import ret.webscrapers.http.ResponseType;

import java.util.function.Supplier;

public abstract class SenderHandlerBase extends HttpServiceBase implements SenderHandler {

    private static Logger log = LogManager.getLogger(SenderHandlerBase.class);
    protected final String url;

    public SenderHandlerBase(HttpClient httpClient, String url) {
        super(httpClient);
        this.url = url;
    }


    public <T> HttpResponse<T> makeRequest(int maxNumberOfAttempts, int delayBetweenAttemptsMillis, Supplier<HttpResponse<T>> codeToRun) {

        if (maxNumberOfAttempts <= 0) {
            throw new IllegalArgumentException("maxNumberOfAttempts < 0 ... this cannot be ...");
        }

        HttpResponse<T> httpResponse = null;
        for (int attemptNo = 0; attemptNo < maxNumberOfAttempts; attemptNo++) {
            httpResponse = codeToRun.get();
            if (isSuccessful(httpResponse)) {
                return httpResponse;
            } else {
                if(canRetry(maxNumberOfAttempts, attemptNo)) {
                    log.warn("Attempt no. {} to send data to server failed with response: {}. Going to retry in {} millis.", attemptNo, httpResponse.getResponseType(), delayBetweenAttemptsMillis);
                    sleepBeforeNextAttempt(delayBetweenAttemptsMillis);
                }
            }
        }

        log.warn(">>  All attempts send data to server failed!  <<");
        return httpResponse;
    }


    private <T> boolean isSuccessful(HttpResponse<T> httpResponse) {
        return httpResponse.getResponseType() == ResponseType.SUCCESS;
    }

    private boolean canRetry(int maxNumberOfAttempts, int attemptNo) {
        return attemptNo < (maxNumberOfAttempts - 1);
    }

    private void sleepBeforeNextAttempt(int delayBetweenAttemptsMillis) {
        try {
            Thread.sleep(delayBetweenAttemptsMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
