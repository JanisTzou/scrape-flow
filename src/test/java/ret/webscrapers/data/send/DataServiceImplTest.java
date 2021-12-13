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

package ret.webscrapers.data.send;

import okhttp3.OkHttpClient;
import org.junit.Ignore;
import org.junit.Test;
import ret.webscrapers.AppConfig;
import ret.webscrapers.data.DataServiceImpl;
import ret.webscrapers.data.HttpHandlersManager;
import ret.webscrapers.data.repo.read.UnsentFilesReader;
import ret.webscrapers.http.HttpClient;

import java.util.concurrent.TimeUnit;

@Ignore
public class DataServiceImplTest {

    @Test
    public void tryResendStoredData() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .build();

        HttpClient httpClient = new HttpClient(okHttpClient);

        UnsentFilesReader fileListReader = new UnsentFilesReader(AppConfig.jsonsDir, AppConfig.imagesDir);
        HttpHandlersManager handlersManager = new HttpHandlersManager(httpClient, AppConfig.jsonsDir, AppConfig.imagesDir);
        DataServiceImpl dataService = new DataServiceImpl(handlersManager, fileListReader);

        dataService.tryResendStoredData();

    }
}
