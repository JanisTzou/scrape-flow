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

import okhttp3.OkHttpClient;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class HttpConnServiceImplTest {

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build();


    @Ignore
    @Test
    public void isConnected() throws InterruptedException {
        HttpClient httpClient = new HttpClient(okHttpClient);
        httpClient.sendHeartbeat();

        for (int i = 0; i < 40; i++) {
            Thread.sleep(1000);
            System.out.println("Connected = " + httpClient.isConnected());
        }

    }
}
