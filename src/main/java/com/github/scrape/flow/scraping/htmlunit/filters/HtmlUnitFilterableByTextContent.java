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

package com.github.scrape.flow.scraping.htmlunit.filters;

public interface HtmlUnitFilterableByTextContent<C> extends HtmlUnitFilterable<C> {

    // TODO we should document that we only search the children ... or something like that ...

    /**
     * Filters all found <code>HtmlElement</code>s whose whole text content exactly matches the specified searchString (ignoring case)
     */
    default C byTextContent(String searchString) {
        return addFilter(HtmlUnitFilterByTextContent.createForSearchString(searchString));
    }

    /**
     * Filters all found <code>HtmlElement</code>s whose text content exactly matches the specified regex.
     * Internally {@link java.lang.String#matches(String regex)} is used -> regex must match whole value.
     */
    default C byTextContentRegex(String regex) {
        return addFilter(HtmlUnitFilterByTextContent.createForRegex(regex));
    }

}
