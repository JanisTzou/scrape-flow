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

package com.github.web.scraping.lib.dom.data.parsing.steps;

import com.github.web.scraping.lib.dom.data.parsing.SiteParserInternal;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class NavigateToParsedLink extends CommonOperationsStepBase<NavigateToParsedLink>
        implements HtmlUnitStepChangingUsedParser<NavigateToParsedLink>, LoadingNewPage {

    // TODp perhaps provide in the constructor as a mandatory thing ?
    private SiteParserInternal<?> siteParser;


    private NavigateToParsedLink(List<HtmlUnitParsingStep<?>> nextSteps, SiteParserInternal<?> siteParser) {
        super(nextSteps);
        this.siteParser = siteParser;
    }

    NavigateToParsedLink(SiteParserInternal<?> siteParser) {
        this(null, siteParser);
    }


    // the URL must come from the parsing context!!
    @Override
    public StepExecOrder execute(ParsingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        // TODO problem ... this does not track steps for us and also the data ...
        Runnable runnable = () -> {
            if (ctx.getParsedURL() != null) {
                // TODO if this step type has collectors then we need similar logic as in Wrapper ...
                siteParser.parseInternal(ctx.getParsedURL(), ctx, this.nextSteps, stepExecOrder);

            } else {
                log.error("{}: Cannot parse next site - the parsed URL is null!", getName());
            }
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }


    @Override
    public NavigateToParsedLink setParser(SiteParserInternal<?> siteParser) {
        this.siteParser = siteParser;
        this.siteParser.setServicesInternal(services);
        return this;
    }

    @Override
    public boolean throttlingAllowed() {
        return true;
    }
}
