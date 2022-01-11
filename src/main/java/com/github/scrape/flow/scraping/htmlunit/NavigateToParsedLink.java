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
import com.github.scrape.flow.scraping.LoadingNewPage;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.SiteParser;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class NavigateToParsedLink extends CommonOperationsStepBase<NavigateToParsedLink>
        implements LoadingNewPage {

    private final SiteParser siteParser;

    NavigateToParsedLink(SiteParser siteParser) {
        this.siteParser = siteParser;
    }

    @Override
    protected NavigateToParsedLink copy() {
        return copyFieldValuesTo(new NavigateToParsedLink(siteParser));
    }

    // the URL must come from the parsing context!!
    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        // TODO problem ... this does not track steps for us and also the data ...
        Runnable runnable = () -> {
            if (ctx.getParsedURL() != null) {
                // TODO if this step type has collectors then we need similar logic as in Wrapper ...
                siteParser.parse(ctx.getParsedURL(), ctx, this.getNextSteps(), stepOrder, services);

            } else {
                log.error("{}: Cannot navigate to next site - the previously parsed URL is null!", getName());
            }
        };

        submitForExecution(stepOrder, runnable, services.getTaskService());

        return stepOrder;
    }


    @Override
    public boolean throttlingAllowed() {
        return true;
    }
}
