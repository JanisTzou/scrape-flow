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

package com.github.scrape.flow.scraping.selenium.filters;

import com.github.scrape.flow.scraping.Filter;
import com.github.scrape.flow.scraping.selenium.SeleniumUtils;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SeleniumFilterByCssClass implements Filter<WebElement> {

    private final String className;

    /**
     * @throws NullPointerException if cssClass is null
     */
    SeleniumFilterByCssClass(String className) {
        Objects.requireNonNull(className);
        if (className.contains(" ")) {
            throw new IllegalArgumentException("className contains a space character. If filtering by multiple class names is required provide them separately");
        }
        this.className = className;
    }

    @Override
    public List<WebElement> filter(List<WebElement> list) {
        return list.stream().filter(dn -> SeleniumUtils.hasCssClass(dn, className)).collect(Collectors.toList());
    }

    public String getClassName() {
        return className;
    }

    public Type getType() {
        return Type.CSS_CLASS;
    }

    // TODO add regex?

}
