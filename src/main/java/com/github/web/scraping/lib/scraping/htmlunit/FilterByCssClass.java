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

import com.gargoylesoftware.htmlunit.html.DomNode;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FilterByCssClass implements Filter {

    private final String className;

    /**
     * @throws NullPointerException if cssClass is null
     */
    FilterByCssClass(String className) {
        Objects.requireNonNull(className);
        this.className = className;
    }

    @Override
    public List<DomNode> filter(List<DomNode> list) {
        return list.stream().filter(dn -> HtmlUnitUtils.hasCssClass(dn, className)).collect(Collectors.toList());
    }

}
