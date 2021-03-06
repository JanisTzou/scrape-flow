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

import com.github.scrape.flow.scraping.htmlunit.HtmlUnitScrapingStep;

public interface HtmlUnitFilterableSiblings<C extends HtmlUnitScrapingStep<C>> extends HtmlUnitFilterable<C> {

    /**
     * Filters the immediately preceding sibling
     */
    default C prev() {
        return addFilter(new HtmlUnitFilterSiblingsPrevNth(1));
    }

    /**
     * Filters the immediately following sibling
     */
    default C next() {
        return addFilter(new HtmlUnitFilterSiblingsNextNth(1));
    }

    /**
     * Filters n siblings out of multiple prev siblings
     * @param n positive integer
     */
    default C prevN(int n) {
        return addFilter(new HtmlUnitFilterSiblingsPrevN(n));
    }

    /**
     * Filters n siblings out of multiple next siblings
     * @param n positive integer
     */
    default C nextN(int n) {
        return addFilter(new HtmlUnitFilterSiblingsNextN(n));
    }

    /**
     * Filters the nth sibling out of multiple previous siblings
     * @param nth positive integer
     */
    default C prevNth(int nth) {
        return addFilter(new HtmlUnitFilterSiblingsPrevNth(nth));
    }

    /**
     * Filters the nth sibling out of multiple next siblings
     * @param nth positive integer
     */
    default C nextNth(int nth) {
        return addFilter(new HtmlUnitFilterSiblingsNextNth(nth));
    }

    /**
     * Filters the every nth sibling out of multiple previous siblings
     * @param nth positive integer
     */
    default C prevEveryNth(int nth) {
        return addFilter(new HtmlUnitFilterSiblingsPrevEveryNth(nth));
    }

    /**
     * Filters every nth sibling out of multiple next siblings
     * @param nth positive integer
     */
    default C nextEveryNth(int nth) {
        return addFilter(new HtmlUnitFilterSiblingsNextEveryNth(nth));
    }

    /**
     * Filters the first sibling out of multiple previous siblings
     */
    default C first() {
        return addFilter(new HtmlUnitFilterSiblingsFirst());
    }

    /**
     * Filters the last sibling out of multiple next siblings
     */
    default C last() {
        return addFilter(new HtmlUnitFilterSiblingsLast());
    }


    /**
     * Returns all siblings of the current element. This is the default behaviour if no other filter is specified
     */
    default C all() {
        return addFilter(new HtmlUnitFilterSiblingsAll());
    }

}
