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

package com.github.scrape.flow.scraping.htmlunit;

import com.gargoylesoftware.htmlunit.html.DomNode;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class FilterByAttribute implements Filter {

    private final String attributeName;
    private final String attributeValue;
    private final String valueRegex;

    private FilterByAttribute(String attributeName,
                      @Nullable String attributeValue,
                      String valueRegex) {
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.valueRegex = valueRegex;
    }

    FilterByAttribute(String attributeName,
                      @Nullable String attributeValue) {
        this(attributeName, attributeValue, null);
    }

    FilterByAttribute(String attributeName) {
        this(attributeName, null, null);
    }

    @Override
    public List<DomNode> filter(List<DomNode> list) {
        return list.stream()
                .filter(n -> {
                    if (attributeValue != null) {
                        return HtmlUnitUtils.hasAttributeWithValue(n, attributeName, attributeValue, true);
                    } else if (valueRegex != null) {
                        return HtmlUnitUtils.hasAttributeWithValue(n, attributeName, valueRegex);
                    } else {
                        return HtmlUnitUtils.hasAttribute(n, attributeName);
                    }
                })
                .collect(Collectors.toList());
    }

}
