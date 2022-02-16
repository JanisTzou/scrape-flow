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

package com.github.scrape.flow.scraping.filters;

import com.github.scrape.flow.debugging.DebuggingOptions;

import java.util.List;
import java.util.stream.Collectors;

public class FilterUtils {

    public static <T> List<T> filter(List<T> itemsToFilter, List<Filter<T>> filters, DebuggingOptions globalDebugging) {
        List<T> nodes = applyFilters(filters, itemsToFilter);
        if (globalDebugging.isOnlyScrapeFirstElements()) {
            return nodes.stream().findFirst().stream().collect(Collectors.toList());
        } else {
            return nodes;
        }
    }

    private static <T> List<T> applyFilters(List<Filter<T>> filters, List<T> nodes) {
        if (filters.isEmpty()) {
            return nodes;
        }
        List<T> filtered = nodes;
        for (Filter<T> f : filters) {
            filtered = f.filter(filtered);
        }
        return filtered;
    }

}
