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

package ret.webscrapers.websegments;


import okhttp3.OkHttpClient;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import ret.appcore.model.WebSegment;
import ret.webscrapers.AppConfig;
import ret.webscrapers.http.HttpClient;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Ignore
public class WebSegmentsServiceImplTest {

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build();


    @Test
    public void pullNextWebSegment() {

        HttpClient httpClient = new HttpClient(okHttpClient);

        WebsegmentsProgressFileHandler fileHandlerMock = Mockito.mock(WebsegmentsProgressFileHandler.class);
        WebSegmentsServiceImpl webSegmentsService = new WebSegmentsServiceImpl(httpClient, new WebSegmentsData(), fileHandlerMock, AppConfig.lastProcessedSegmentsAgeToConsider);

        Optional<WebSegment> webSegment = webSegmentsService.nextWebSegment();

        System.out.println(webSegment);
    }

    @Test
    public void sendFinishedWebSegmentOverview() {
    }


}
