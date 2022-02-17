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

public interface HtmlUnitFilterableByFirstAndLast<C> extends HtmlUnitFilterable<C> {

    /**
     * Filters the first found <code>HtmlElement</code>
     */
    default C first() {
        return addFilter(new HtmlUnitFilterFirstN(1));
    }

    /**
     * Filters the last found <code>HtmlElement</code>
     */
    default C last() {
        return addFilter(new HtmlUnitFilterLastN(1));
    }

    /**
     * Filters the first n found <code>HtmlElement</code>s
     * @param n non-negative integer
     */
    default C firstN(int n) {
        return addFilter(new HtmlUnitFilterFirstN(n));
    }

    /**
     * Filters the last n found <code>HtmlElement</code>s
     * @param n non-negative integer
     */
    default C lastN(int n) {
        return addFilter(new HtmlUnitFilterLastN(n));
    }

    /**
     * Filters the first nth found <code>HtmlElement</code>
     * @param nth positive integer
     */
    default C firstNth(int nth) {
        return addFilter(new HtmlUnitFilterFirstNth(nth));
    }

    /**
     * Filters the last nth found <code>HtmlElement</code> (nth counted in reverse order)
     * @param nth positive integer
     */
    default C lastNth(int nth) {
        return addFilter(new HtmlUnitFilterLastNth(nth));
    }

    /**
     * Filters away the first n <code>HtmlElement</code>s
     * @param n non-negative integer
     */
    default C excludingFirstN(int n) {
        return addFilter(new HtmlUnitFilterExcludeFirstN(n));
    }

    /**
     * Filters away the last n <code>HtmlElement</code>s
     * @param n non-negative integer
     */
    default C excludingLastN(int n) {
        return addFilter(new HtmlUnitFilterExcludeLastN(n));
    }

}
