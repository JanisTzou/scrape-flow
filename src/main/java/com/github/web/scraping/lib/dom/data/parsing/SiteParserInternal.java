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

package com.github.web.scraping.lib.dom.data.parsing;

import com.github.web.scraping.lib.dom.data.parsing.steps.ScrapingServices;
import com.github.web.scraping.lib.dom.data.parsing.steps.HtmlUnitParsingStep;
import com.github.web.scraping.lib.parallelism.StepExecOrder;

import java.util.List;

/**
 * Exposes some internally used methods - for internal use only
 */
public interface SiteParserInternal<T> extends SiteParser<T> {

    /**
     * For internal lib uses only
     */
    void parseInternal(String url, ParsingContext<?, ?> ctx, List<HtmlUnitParsingStep<?>> parsingSequence, StepExecOrder currStepExecOrder);

    /**
     * For internal lib uses only
     */
    void setServicesInternal(ScrapingServices services);

}
