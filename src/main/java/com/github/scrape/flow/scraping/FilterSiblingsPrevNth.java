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

import java.util.List;

public class FilterSiblingsPrevNth<C> implements Filter<C> {

    private final int nth;

    /**
     * @param nth positive integer
     */
    public FilterSiblingsPrevNth(int nth) {
        if (nth <= 0) {
            throw new IllegalArgumentException("n must be > 0");
        }
        this.nth = nth;
    }

    /**
     * @param allPrevSiblings sorted from the perspective of the element whose siblings these are
     *                        - the closest being first
     */
    @Override
    public List<C> filter(List<C> allPrevSiblings) {
        // TODO document that this is from the beginning of the
        //  maybe it would be best to have two directions / prespectives ... from beginning and from the element we found this from ...
        return new FilterFirstNth<C>(nth).filter(allPrevSiblings);
    }

}
