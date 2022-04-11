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

import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.LoadingNewPage;
import com.github.scrape.flow.scraping.ScrapingContext;
import com.github.scrape.flow.scraping.ScrapingServices;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HtmlUnitFollowLink extends HtmlUnitScrapingStep<HtmlUnitFollowLink>
        implements LoadingNewPage {

    HtmlUnitFollowLink() {
    }

    @Override
    protected HtmlUnitFollowLink copy() {
        return copyFieldValuesTo(new HtmlUnitFollowLink());
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());
        Runnable runnable = new HtmlUnitFollowLinkRunnable(ctx, stepOrder, getHelper(services), getName());
        submitForExecution(stepOrder, runnable, services);
        return stepOrder;
    }

    private void logWarn() {
        log.warn("{}: No anchor element with href attribute provided -> cannot click element! Check the steps sequence above step {} " +
                "and maybe provide search step for an anchor tag. It might be necessary to use scraping with JS support here", getName(), getName());
    }

    @Override
    public boolean throttlingAllowed() {
        return true;
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }

}
