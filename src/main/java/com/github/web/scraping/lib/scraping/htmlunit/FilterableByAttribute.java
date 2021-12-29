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

package com.github.web.scraping.lib.scraping.htmlunit;

import javax.annotation.Nullable;

public interface FilterableByAttribute<C extends HtmlUnitScrapingStep<C>> extends Filterable<C> {

    default C byAttr(String name, @Nullable String value) {
        FilterByAttribute filter = new FilterByAttribute(name, value);
        return addFilter(filter);
    }

    /**
     * Will  match the attribute value using {@link java.lang.String#matches(String regex)} -> regex must match whole value
     */
    default C byAttrRegex(String name, String valueRegex) {
        FilterByAttribute filter = new FilterByAttribute(name, valueRegex);
        return addFilter(filter);
    }

    default C byAttr(String name) {
        FilterByAttribute filter = new FilterByAttribute(name);
        return addFilter(filter);
    }

}
