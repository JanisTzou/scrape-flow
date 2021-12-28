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
import com.gargoylesoftware.htmlunit.html.DomText;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
public class FilterByTextContent implements Filter {

    private final String searchString;
    private final boolean matchWholeTextContent;


    FilterByTextContent(String searchString, boolean matchWholeTextContent) {
        Objects.requireNonNull(searchString);
        this.searchString = searchString;
        this.matchWholeTextContent = matchWholeTextContent;
    }

    // TODO think about how to use this when we have a lot of descendants and some of them might be nested ...
    //  ... also try to use TextNodes ...

    // TODO we need to get to the lowest element with the text ...

    @Override
    public List<DomNode> filter(List<DomNode> list) {
        return list.stream().filter(this::containsSearchStringDirectly).collect(Collectors.toList());

    }

    // recursively visits all child nodes until it finds one that has child DomText nodes containing the search text
    private boolean containsSearchStringDirectly(DomNode domNode) {
        for (DomNode childNode : domNode.getChildNodes()) {
            if (childNode instanceof DomText domText) {
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

    // recursively visits all child nodes until it finds one that has child DomText nodes containing the search text
    private boolean containsSearchStringRecursively(DomNode domNode) {
        for (DomNode childNode : domNode.getChildNodes()) {
            if (childNode instanceof DomText domText) {
                if (isFound(domText)) {
                    log.debug("Found element by textContent: {} - {}", searchString, childNode);
                    return true;
                } else {
                    return containsSearchStringRecursively(childNode);
                }
            }
        }
        return false;
    }

    private boolean isFound(DomText domText) {
        boolean found;
        if (matchWholeTextContent) {
            found = domText.getTextContent().trim().equalsIgnoreCase(searchString);
        } else {
            found = domText.getTextContent().trim().contains(searchString);
        }
        return found;
    }


}
