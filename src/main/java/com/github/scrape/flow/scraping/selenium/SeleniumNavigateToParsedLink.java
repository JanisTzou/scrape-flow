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
import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.LoadingNewPage;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public class SeleniumNavigateToParsedLink extends SeleniumScrapingStep<SeleniumNavigateToParsedLink>
        implements LoadingNewPage {

    @Override
    protected SeleniumNavigateToParsedLink copy() {
        return copyFieldValuesTo(new SeleniumNavigateToParsedLink());
    }

    // the URL must come from the parsing context!!
    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {

        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        // TODO problem ... this does not track steps for us and also the data ...
        Runnable runnable = () -> {
            if (ctx.getParsedURL() != null) {

                Optional<ClientOperator<WebDriver>> driverOperator = services.getClientAccessManager().getSeleniumClient(stepOrder);

                if (driverOperator.isPresent()) {
                    Supplier<List<WebElement>> elementSearch = () -> {
                        WebDriver driver = driverOperator.get().getClient();
                        driver.get(ctx.getParsedURL());
                        WebElement html = driver.findElement(By.tagName("html")); // TODO do until successful ... think about where the retry should be taking palce  ...
                        return List.of(html);
                    };
                    getHelper().execute(elementSearch, ctx, stepOrder, services);
                } else {
                    log.error("Step {} cannot execute as a webDriver that was supposed to be reserved for it was not available!", getName());
                }

            } else {
                log.error("{}: Cannot navigate to next site - the previously parsed URL is null!", getName());
            }
        };

        submitForExecution(stepOrder, runnable, services);

        return stepOrder;
    }


    @Override
    protected boolean throttlingAllowed() {
        return true;
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.LOADING;
    }


}
