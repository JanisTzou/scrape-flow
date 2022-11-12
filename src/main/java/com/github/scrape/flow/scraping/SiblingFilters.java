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

public class SiblingFilters {

    public static final List<Class<?>> PREV_SIBLINGS_FILTER_CLASSES = List.of(
            FilterSiblingsPrevN.class,
            FilterSiblingsPrevNth.class,
            FilterSiblingsPrevEveryNth.class,
            FilterSiblingsFirst.class
    );
    public static final List<Class<?>> NEXT_SIBLINGS_FILTER_CLASSES = List.of(
            FilterSiblingsNextN.class,
            FilterSiblingsNextNth.class,
            FilterSiblingsNextEveryNth.class,
            FilterSiblingsLast.class
    );
    public static final List<Class<?>> ALL_SIBLINGS_FILTER_CLASSES = List.of(
            FilterSiblingsAll.class
    );
}
