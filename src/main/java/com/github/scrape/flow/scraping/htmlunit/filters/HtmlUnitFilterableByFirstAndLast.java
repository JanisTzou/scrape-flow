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

    default C first() {
        return addFilter(new HtmlUnitFilterFirstN(1));
    }

    default C last() {
        return addFilter(new HtmlUnitFilterLastN(1));
    }

    /**
     * @param n non-negative integer
     */
    default C firstN(int n) {
        return addFilter(new HtmlUnitFilterFirstN(n));
    }

    /**
     * @param n non-negative integer
     */
    default C lastN(int n) {
        return addFilter(new HtmlUnitFilterLastN(n));
    }

    /**
     * @param nth positive integer
     */
    default C firstNth(int nth) {
        return addFilter(new HtmlUnitFilterFirstNth(nth));
    }

    /**
     * @param nth positive integer
     */
    default C lastNth(int nth) {
        return addFilter(new HtmlUnitFilterLastNth(nth));
    }

    /**
     * @param n non-negative integer
     */
    default C excludingFirstN(int n) {
        return addFilter(new HtmlUnitFilterExcludeFirstN(n));
    }

    /**
     * @param n non-negative integer
     */
    default C excludingLastN(int n) {
        return addFilter(new HtmlUnitFilterExcludeLastN(n));
    }

}
