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
import com.github.scrape.flow.scraping.Filter;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class HtmlUnitFilterByCssClass implements Filter<DomNode> {

    private final String className;
    private final String classNameRegex;

    @Override
    public List<DomNode> filter(List<DomNode> list) {
        if (className != null) {
            return list.stream().filter(dn -> HtmlUnitUtils.hasCssClass(dn, className)).collect(Collectors.toList());
        } else if (classNameRegex != null) {
            return list.stream().filter(dn -> HtmlUnitUtils.hasCssClassMatchingRegex(dn, classNameRegex)).collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Cannot match class name - no search criteria specified!");
        }
    }

}
