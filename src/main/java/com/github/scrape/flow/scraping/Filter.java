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


public interface Filter<T> {

    // we always need to filter the whole list as we might have operations that
    // need to know the number of items (e.g. whe filtering the first N nodes)
    List<T> filter(List<T> list);

    Type getType();

    enum Type {
        TAG,
        ATTRIBUTE,
        CSS_CLASS,
        ID,
        TEXT_MATCHING,
        POSITION;

        public boolean isPosition() {
            return this.equals(POSITION);
        }
    }



}
