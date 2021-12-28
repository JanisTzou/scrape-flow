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

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class FilterByAttribute implements Filter {

    private static final boolean MATCH_ENTIRE_VALUE_DEFAULT = true;
    private final String attributeName;
    private final String attributeValue;
    private boolean matchEntireValue;

    FilterByAttribute(String attributeName,
                      @Nullable String attributeValue,
                      boolean matchEntireValue) {
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.matchEntireValue = matchEntireValue;
    }

    FilterByAttribute(String attributeName, @Nullable String attributeValue) {
        this(attributeName, attributeValue, MATCH_ENTIRE_VALUE_DEFAULT);
    }

    FilterByAttribute(String attributeName) {
        this(attributeName, null, MATCH_ENTIRE_VALUE_DEFAULT);
    }

    @Override
    public List<DomNode> filter(List<DomNode> list) {
        return list.stream()
                .filter(n -> {
                    if (attributeValue != null) {
                        return HtmlUnitUtils.hasAttributeWithValue(n, attributeName, attributeValue, this.matchEntireValue);
                    } else {
                        return HtmlUnitUtils.hasAttribute(n, attributeName);
                    }
                    // TODO support for patterns ...
                })
                .collect(Collectors.toList());
    }

}
