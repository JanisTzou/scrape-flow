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

import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.*;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HtmlUnitNavigateToUrl extends HtmlUnitScrapingStep<HtmlUnitNavigateToUrl>
        implements LoadingNewPage {

    private final String url;

    HtmlUnitNavigateToUrl(String url) {
        this.url = url;
    }

    @Override
    protected HtmlUnitNavigateToUrl copy() {
        return copyFieldValuesTo(new HtmlUnitNavigateToUrl(url));
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        // TODO problem ... this does not track steps for us and also the data ...
        Runnable runnable = () -> {
            // TODO if this step type has collectors then we need similar logic as in Wrapper ...
            services.getHtmlUnitSiteLoader().loadPageAndExecuteNextSteps(url, ctx, ScrapingStepInternalProxy.of(this).getNextSteps(), stepOrder, services);
        };

        submitForExecution(stepOrder, runnable, services.getTaskService(), services.getSeleniumDriversManager());

        return stepOrder;
    }


    @Override
    public boolean throttlingAllowed() {
        return true;
    }
}
