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
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class SeleniumFilterByTextContent implements Filter<WebElement> {

    private final String searchString;
    private final String regex;

    static SeleniumFilterByTextContent createForSearchString(String searchString) {
        return new SeleniumFilterByTextContent(searchString, null);
    }

    static SeleniumFilterByTextContent createForRegex(String searchStringRegex) {
        return new SeleniumFilterByTextContent(null, searchStringRegex);
    }

    @Override
    public List<WebElement> filter(List<WebElement> list) {
        return list.stream().filter(this::matches).collect(Collectors.toList());

    }

    private boolean matches(WebElement webElement) { // TODO how to handle sub elements?
        // TODO get the children here and check that they do not contain the given text, if they do not then we can return the currently found ...
        if (searchString != null) {
            return webElement.getText().trim().equalsIgnoreCase(searchString);
        } else if (regex != null) {
            return webElement.getText().matches(regex);
        } else {
            throw new IllegalArgumentException("Cannot match text content - no search criteria specified!");
        }
    }


    public Type getType() {
        return Type.TEXT_MATCHING;
    }
}
