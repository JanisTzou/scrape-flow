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

import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.SiteParserInternal;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@Log4j2
public class NavigateToNewSite extends CommonOperationsStepBase<NavigateToNewSite> {

    // TODp perhaps provide in the constructor as a mandatory thing ?
    private SiteParserInternal<?> siteParser;

    public NavigateToNewSite(@Nullable List<HtmlUnitParsingStep<?>> nextSteps) {
        super(nextSteps);
    }

    public NavigateToNewSite() {
        this(null);
    }

    // the URL must come from the parsing context!!
    @Override
    public <ModelT, ContainerT> List<StepResult> execute(ParsingContext<ModelT, ContainerT> ctx) {
        logExecutionStart();
        if (ctx.getParsedText() == null) {
            return siteParser.parseInternal(ctx.getParsedURL(), ctx, this.nextSteps);
        } else {
            log.error("{}: Cannot parse next site - the parsed URL is null!", getName());
            return Collections.emptyList();
        }
    }


    public NavigateToNewSite setSiteParser(SiteParserInternal<?> siteParser) {
        this.siteParser = siteParser;
        return this;
    }

    @Override
    public NavigateToNewSite then(HtmlUnitParsingStep<?> nextStep) {
        super.then(nextStep);
        return this;
    }

}
