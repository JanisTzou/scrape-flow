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

import com.github.scrape.flow.parallelism.StepExecOrder;
import com.github.scrape.flow.scraping.LoadingNewPage;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.SiteParser;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class NavigateToParsedLink extends CommonOperationsStepBase<NavigateToParsedLink>
        implements LoadingNewPage {

    // TODp perhaps provide in the constructor as a mandatory thing ?
    private SiteParser siteParser;


    NavigateToParsedLink(List<HtmlUnitScrapingStep<?>> nextSteps, SiteParser siteParser) {
        super(nextSteps);
        this.siteParser = siteParser;
    }

    NavigateToParsedLink(SiteParser siteParser) {
        this(null, siteParser);
    }

    @Override
    protected NavigateToParsedLink copy() {
        return copyFieldValuesTo(new NavigateToParsedLink(siteParser));
    }

    // the URL must come from the parsing context!!
    @Override
    protected StepExecOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepExecOrder stepExecOrder = services.getStepExecOrderGenerator().genNextOrderAfter(ctx.getPrevStepExecOrder());

        // TODO problem ... this does not track steps for us and also the data ...
        Runnable runnable = () -> {
            if (ctx.getParsedURL() != null) {
                // TODO if this step type has collectors then we need similar logic as in Wrapper ...
                siteParser.parse(ctx.getParsedURL(), ctx, this.getNextSteps(), stepExecOrder, services);

            } else {
                log.error("{}: Cannot navigate to next site - the previously parsed URL is null!", getName());
            }
        };

        submitForExecution(stepExecOrder, runnable, services.getTaskService());

        return stepExecOrder;
    }


    @Override
    public boolean throttlingAllowed() {
        return true;
    }
}
