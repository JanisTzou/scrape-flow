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
import com.github.scrape.flow.scraping.ScrapingStepInternalAccessor;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public class SeleniumReloadPage extends SeleniumScrapingStep<SeleniumReloadPage>
        implements LoadingNewPage {

    SeleniumReloadPage() {
    }

    @Override
    protected SeleniumReloadPage copy() {
        return copyFieldValuesTo(new SeleniumReloadPage());
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Optional<ClientOperator<WebDriver>> operator = services.getClientAccessManager().getSeleniumClient(stepOrder);
            WebElement htmlElement;
            if (operator.isPresent()) {
                WebDriver client = operator.get().getClient();
                client.navigate().refresh();
                htmlElement = client.findElement(By.tagName("html"));
                // TODO wait for page loading to finish ... someway?
            } else {
                throw new IllegalStateException("No client!");
            }
            Supplier<List<WebElement>> elementSearch = () -> List.of(htmlElement);
            getHelper().execute(elementSearch, ctx, stepOrder, services);
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
        return ClientReservationType.MODIFYING;
    }

}
