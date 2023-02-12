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
import com.gargoylesoftware.htmlunit.html.DomText;
import com.github.scrape.flow.scraping.Filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class HtmlUnitFilterByTextContent implements Filter<DomNode> {

    private final String searchString;
    private final String regex;

    static HtmlUnitFilterByTextContent createForSearchString(String searchString) {
        return new HtmlUnitFilterByTextContent(searchString, null);
    }

    static HtmlUnitFilterByTextContent createForRegex(String searchStringRegex) {
        return new HtmlUnitFilterByTextContent(null, searchStringRegex);
    }

    @Override
    public List<DomNode> filter(List<DomNode> list) {
        return list.stream().filter(this::childNodesContainText).collect(Collectors.toList());

    }

    // Important the child nodes need to contain the text - not the descendants
    private boolean childNodesContainText(DomNode domNode) {
        for (DomNode childNode : domNode.getChildNodes()) {
            if (childNode instanceof DomText) {
                DomText domText = (DomText) childNode;
                if (isFound(domText)) {
                    log.debug("Found element by textContent: {} - {}", searchString, childNode);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isFound(DomText domText) {
        if (searchString != null) {
            return domText.getTextContent().trim().equalsIgnoreCase(searchString);
        } else if (regex != null) {
            return domText.getTextContent().matches(regex);
        } else {
            throw new IllegalArgumentException("Cannot match text content - no search criteria specified!");
        }
    }


    public Type getType() {
        return Type.TEXT_MATCHING;
    }
}
