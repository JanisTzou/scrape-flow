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

package com.github.scrape.flow.scraping.htmlunit;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.scrape.ClientReservationHandlerMockConfig;
import com.github.scrape.TestConfiguration;
import com.github.scrape.flow.data.publishing.ScrapedDataListener;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.execution.TaskExecutor;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnitFlow.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class, ClientReservationHandlerMockConfig.class})
public class HtmlUnitGetChildrenAndParseTextContentIntegrationTest {

    @Autowired
    private ScrapingServices scrapingServices;
    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private WebClient webClient;

    @SuppressWarnings("unchecked")
    private final ScrapedDataListener<ScrapedValue> dataListenerMock = mock(ScrapedDataListener.class);


    @Test
    public void childNodesAreFoundAndTextContentIsParsedAndCollectedToModels() throws IOException, URISyntaxException {

        HtmlPage page = TestUtils.loadTestPage("test_page_1.html", webClient);
        DomNode parent = (DomNode) page.getByXPath("/html/body/div/div").stream().findFirst().get();
        ScrapingContext ctx = new ScrapingContext(StepOrder.INITIAL, parent);

        HtmlUnitGetChildren testSequence = new HtmlUnitGetChildren()
                .addCollector(ScrapedValue::new, ScrapedValue.class, dataListenerMock)
                .next(Parse.textContent()
                        .collectValue(ScrapedValue::setVal, ScrapedValue.class)
                );

        testSequence.execute(ctx, scrapingServices);
        taskExecutor.awaitCompletion(Duration.ofSeconds(1));

        ArgumentCaptor<ScrapedValue> argument = ArgumentCaptor.forClass(ScrapedValue.class);
        verify(dataListenerMock, times(2)).onScrapedData(argument.capture());
        List<ScrapedValue> parsedValues = argument.getAllValues();
        assertEquals(2, parsedValues.size());
        assertEquals("div-3-1_text", parsedValues.get(0).getVal());
        assertEquals("div-3-2_text", parsedValues.get(1).getVal());
    }


    @Data
    private static class ScrapedValue {
        private String val;
    }


}
