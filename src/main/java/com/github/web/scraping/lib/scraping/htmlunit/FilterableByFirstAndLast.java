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

public interface FilterableByFirstAndLast<C extends HtmlUnitScrapingStep<C>> extends Filterable<C> {

    default C first() {
        return addFilter(new FilterFirstN(1));
    }

    default C last() {
        return addFilter(new FilterLastN(1));
    }

    /**
     * @param n non-negative integer
     */
    default C firstN(int n) {
        return addFilter(new FilterFirstN(n));
    }

    /**
     * @param n non-negative integer
     */
    default C lastN(int n) {
        return addFilter(new FilterLastN(n));
    }

    /**
     * @param nth positive integer
     */
    default C firstNth(int nth) {
        return addFilter(new FilterFirstNth(nth));
    }

    /**
     * @param nth positive integer
     */
    default C lastNth(int nth) {
        return addFilter(new FilterLastNth(nth));
    }

    /**
     * @param n non-negative integer
     */
    default C excludingFirstN(int n) {
        return addFilter(new FilterExcludeFirstN(n));
    }

    /**
     * @param n non-negative integer
     */
    default C excludingLastN(int n) {
        return addFilter(new FilterExcludeLastN(n));
    }

}
