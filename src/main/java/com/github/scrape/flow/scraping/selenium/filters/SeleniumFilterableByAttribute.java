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

public interface SeleniumFilterableByAttribute<C> extends SeleniumFilterable<C> {

    /**
     * Filters all found <code>WebElement</code>s having the specified attr name and value
     */
    default C byAttr(String name, @Nullable String value) {
        SeleniumFilterByAttribute filter = new SeleniumFilterByAttribute(name, value, null);
        return addFilter(filter);
    }

    /**
     * Filters all found <code>WebElement</code>s by their attr name and value regex using {@link String#matches(String regex)} -> regex must match whole value
     */
    default C byAttrRegex(String name, String valueRegex) {
        SeleniumFilterByAttribute filter = new SeleniumFilterByAttribute(name, null, valueRegex);
        return addFilter(filter);
    }

    /**
     * Filters all found <code>WebElement</code>s having the specified attr name
     */
    default C byAttr(String name) {
        SeleniumFilterByAttribute filter = new SeleniumFilterByAttribute(name);
        return addFilter(filter);
    }

}
