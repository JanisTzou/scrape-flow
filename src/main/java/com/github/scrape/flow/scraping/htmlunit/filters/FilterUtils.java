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

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.scrape.flow.debugging.DebuggingOptions;

import java.util.List;
import java.util.stream.Collectors;

public class FilterUtils {

    public static List<DomNode> filter(List<DomNode> nodesToFilter, List<Filter> filters, DebuggingOptions globalDebugging) {
        List<DomNode> nodes = applyFilters(filters, nodesToFilter);
        if (globalDebugging.isOnlyScrapeFirstElements()) {
            return nodes.stream().findFirst().stream().collect(Collectors.toList());
        } else {
            return nodes;
        }
    }

    private static List<DomNode> applyFilters(List<Filter> filters, List<DomNode> nodes) {
        if (filters.isEmpty()) {
            return nodes;
        }
        List<DomNode> filtered = nodes;
        for (Filter f : filters) {
            filtered = f.filter(filtered);
        }
        return filtered;
    }

}
