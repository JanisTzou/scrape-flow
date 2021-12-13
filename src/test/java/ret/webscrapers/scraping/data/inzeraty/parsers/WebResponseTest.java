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

package ret.webscrapers.scraping.data.inzeraty.parsers;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;

public class WebResponseTest {

    @Test
    public void test() throws IOException {

        long start = System.currentTimeMillis();

        for (int i = 0; i < 1; i++) {
            final WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setJavaScriptEnabled(false);
            System.out.println("here 1: " + LocalDateTime.now());
            WebRequest webRequest = new WebRequest(new URL("https://reality.idnes.cz/detail/pronajem/byt/praha-2-londynska/5d5aa7f9558f07034444a403/?s-et=flat&s-l=VUSC-19&s-qc%5BsubtypeFlat%5D%5B0%5D=11"));
            WebResponse webResponse = webClient.loadWebResponse(webRequest);
            System.out.println("here 2: " + LocalDateTime.now());
            WebClient webClient2 = new WebClient();

            webClient2.loadWebResponseInto(webResponse, webClient2.getCurrentWindow());
            System.out.println("here 3: " + LocalDateTime.now());

            webClient.close();
            webClient2.close();
        }

        long finish = System.currentTimeMillis();

        System.out.println(finish - start);

    }

}
