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


import com.github.scrape.flow.scraping.Filter;
import com.github.scrape.flow.scraping.Filterable;
import org.openqa.selenium.WebElement;

// TODO support basic logic operators in filtering ...
public interface SeleniumFilterable<C> extends Filterable<C, WebElement> {

    /**
     * @param filter a filter that will be applied on found elements by this step before
     *               next steps are executed for each of the resulting elements.
     *               The filters are applied in the order in which they were added.
     * @return a copy of this instance with the filter added to it
     */
    C addFilter(Filter<WebElement> filter);

}
