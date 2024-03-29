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

public interface FilterableByFirstAndLast<C, E> extends Filterable<C, E> {

    /**
     * Filters the first found <code>HtmlElement</code>
     */
    default C first() {
        return addFilter(new FilterFirstNth<>(1));
    }

    /**
     * Filters the last found <code>HtmlElement</code>
     */
    default C last() {
        return addFilter(new FilterLast<>());
    }

    /**
     * Filters the first n found <code>HtmlElement</code>s
     * @param n non-negative integer
     */
    default C firstN(int n) {
        return addFilter(new FilterFirstN<>(n));
    }

    /**
     * Filters the last n found <code>HtmlElement</code>s
     * @param n non-negative integer
     */
    default C lastN(int n) {
        return addFilter(new FilterLastN<>(n));
    }

    /**
     * Filters the first nth found <code>HtmlElement</code>
     * @param nth positive integer
     */
    default C firstNth(int nth) {
        return addFilter(new FilterFirstNth<>(nth));
    }

    /**
     * Filters the last nth found <code>HtmlElement</code> (nth counted in reverse order)
     * @param nth positive integer
     */
    default C lastNth(int nth) {
        return addFilter(new FilterLastNth<>(nth));
    }

    /**
     * Filters away the first n <code>HtmlElement</code>s
     * @param n non-negative integer
     */
    default C excludingFirstN(int n) {
        return addFilter(new FilterExcludeFirstN<>(n));
    }

    /**
     * Filters away the last n <code>HtmlElement</code>s
     * @param n non-negative integer
     */
    default C excludingLastN(int n) {
        return addFilter(new FilterExcludeLastN<>(n));
    }

}
