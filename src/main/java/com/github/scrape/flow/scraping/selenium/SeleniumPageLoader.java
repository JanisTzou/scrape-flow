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

package com.github.scrape.flow.scraping.selenium;

import com.github.scrape.flow.clients.ClientOperator;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.*;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Log4j2
public class SeleniumPageLoader implements PageLoader<ClientOperator<WebDriver>> {

    public SeleniumPageLoader() {
    }

    @Override
    public void loadPageAndExecuteNextSteps(String url,
                                            ScrapingContext ctx,
                                            List<ScrapingStep<?>> parsingSequences,
                                            StepOrder currStepOrder,
                                            ScrapingServices services,
                                            ClientOperator<WebDriver> clientOperator) {
        loadPage(url, currStepOrder, clientOperator).ifPresent(rootWebElement -> {
            ScrapingContext nextCtx = ctx.toBuilder()
                    .setWebElement(rootWebElement)
                    .setPrevStepOrder(currStepOrder)
                    .build();
            executeNextSteps(nextCtx, parsingSequences, services);
        });
    }

    private Optional<WebElement> loadPage(String url, @Nullable StepOrder currStepOrder, ClientOperator<WebDriver> clientOperator) {
        return loadPage(url, clientOperator, currStepOrder);
    }

    private void executeNextSteps(ScrapingContext ctx, List<ScrapingStep<?>> parsingSequences, ScrapingServices services) {
        parsingSequences.forEach(s -> ScrapingStepInternalAccessor.of(s).execute(ctx, services));
    }

    private Optional<WebElement> loadPage(String pageUrl, ClientOperator<WebDriver> clientOperator, @Nullable StepOrder currStepOrder) {
        String logInfo = currStepOrder != null ? currStepOrder + " - " : "";
        try {
            log.info("{}Loading page URL: {}", logInfo, pageUrl);
            WebDriver client = clientOperator.getClient();
            client.get(pageUrl);  // we have one clientOperator instance per thread so this call is ok -> each client will have its own "current top WebWindow"
            // https://www.browserstack.com/guide/selenium-wait-for-page-to-load
//            client.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10)); // TODO ... uncomment?
//            client.manage().window().maximize();
            WebElement root = client.findElement(By.tagName("html"));

            log.info("{}Loaded page URL: {}", logInfo, pageUrl);
            return Optional.of(root);

        } catch (Exception e) {
            log.error("{}Error when getting htmlPage for URL: {}", logInfo, pageUrl, e);
            throw new RequestException(e);
        }
    }


}
