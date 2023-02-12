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

package com.github.scrape.flow.scraping.selenium.filters;

import javax.annotation.Nullable;

// TODO apply this and also use if in HtmlUnit ...
public interface SeleniumFilterableById<C> extends SeleniumFilterable<C> {

    // TODO revise JavaDoc as they are not reflecting waht is going on ... we do not first find all and then filter ... we apply filters in the search ...
    //  so here the filter is basically an element search criterion
    /**
     * Filter all found <code>WebElement</code>s having the specified id value
     */
    default C byId(@Nullable String id) {
        SeleniumFilterByAttribute filter = new SeleniumFilterByAttribute("id", id, null);
        return addFilter(filter);
    }

    /**
     * Filters all found <code>WebElement</code>s by id matching the provided regex using {@link String#matches(String regex)} -> regex must match whole id value
     */
    default C byIdRegex(String idRegex) {
        SeleniumFilterByAttribute filter = new SeleniumFilterByAttribute("id", null, idRegex);
        return addFilter(filter);
    }

}
