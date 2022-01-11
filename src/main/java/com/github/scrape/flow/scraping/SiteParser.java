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

package com.github.scrape.flow.scraping;

import com.github.scrape.flow.parallelism.StepOrder;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitScrapingStep;
import com.github.scrape.flow.scraping.htmlunit.ScrapingContext;

import java.util.List;

public interface SiteParser {

    // TODO this looks pretty HtmlUnit-specific ...

    void parse(String url, HtmlUnitScrapingStep<?> parsingSequence, ScrapingServices services);

    /**
     * For internal lib uses only
     */
    void parse(String url, ScrapingContext ctx, List<HtmlUnitScrapingStep<?>> parsingSequence, StepOrder currStepOrder, ScrapingServices services);

}
