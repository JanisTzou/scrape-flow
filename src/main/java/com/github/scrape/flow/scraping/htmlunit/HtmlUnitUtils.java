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
import com.gargoylesoftware.htmlunit.html.HtmlElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HtmlUnitUtils {

    public static boolean hasCssClass(DomNode domNode, String cssClassName) {
        if (domNode instanceof HtmlElement) {
            HtmlElement htmlEl = (HtmlElement) domNode;
            if (htmlEl.hasAttribute("class")) {
                return Arrays.stream(htmlEl.getAttribute("class").split(" ")).anyMatch(cls -> cls.equalsIgnoreCase(cssClassName));
            }
        }
        return false;
    }

    public static boolean hasTagName(DomNode domNode, String tagName) {
        if (domNode instanceof HtmlElement) {
            HtmlElement htmlEl = (HtmlElement) domNode;
            return htmlEl.getTagName().equalsIgnoreCase(tagName);
        }
        return false;
    }

    public static List<DomNode> getDescendantsBySccSelector(DomNode domNode, String selector) {
        return domNode.querySelectorAll(selector).stream().filter(n -> n instanceof HtmlElement).collect(Collectors.toList());
    }

    public static List<DomNode> getHtmlElementDescendants(DomNode parentElement, Predicate<DomNode> filter) {
        List<DomNode> found = new ArrayList<>();
        for (DomNode desc : parentElement.getHtmlElementDescendants()) {
            if (desc instanceof HtmlElement) {
                HtmlElement htmlEl = (HtmlElement) desc;
                if (filter.test(htmlEl)) {
                    found.add(desc);
                }
            }
        }
        return found;
    }

    public static boolean hasAttributeWithExactValue(DomNode domNode, String attribute, String value) {
        if (domNode instanceof HtmlElement) {
            HtmlElement element = (HtmlElement) domNode;
            return element.hasAttribute(attribute) && element.getAttribute(attribute).equalsIgnoreCase(value);
        } else {
            return false;
        }
    }

    public static boolean hasAttributeWithValue(DomNode domNode, String attribute, String valueRegex) {
        if (domNode instanceof HtmlElement) {
            HtmlElement element = (HtmlElement) domNode;
            if (element.hasAttribute(attribute)) {
                return element.getAttribute(attribute).matches(valueRegex);
            }
        }
        return false;
    }

    public static boolean hasAttribute(DomNode domNode, String attribute) {
        if (domNode instanceof HtmlElement) {
            HtmlElement element = (HtmlElement) domNode;
            return element.hasAttribute(attribute);
        } else {
            return false;
        }
    }

    public static Optional<DomNode> findNthAncestor(DomNode domNode, Integer nth) {
        if (nth != null && nth < 0) {
            throw new IllegalArgumentException("Cannot return nth child element for n = " + nth + " - nth must be a non-null and non-negative integer!");
        } else {
            return findNthAncestorHelper(domNode, nth, 0);
        }
    }

    private static Optional<DomNode> findNthAncestorHelper(DomNode domNode, int nth, int count) {
        if (count == nth) {
            return Optional.of(domNode);
        } else {
            if (domNode.getParentNode() != null && domNode instanceof HtmlElement) {
                return findNthAncestorHelper(domNode.getParentNode(), nth, ++count);
            } else {
                return Optional.empty();
            }
        }
    }

    public static List<DomNode> findAllSiblingElements(DomNode domNode) {
        return Stream.concat(
                        findPrevSiblingElements(domNode).stream(),
                        findNextSiblingElements(domNode).stream()
                )
                .collect(Collectors.toList());
    }

    public static List<DomNode> findPrevSiblingElements(DomNode domNode) {
        List<DomNode> prevSiblings = new ArrayList<>();
        DomNode prev = domNode.getPreviousElementSibling();
        while (prev != null) {
            prevSiblings.add(prev);
            prev = prev.getPreviousElementSibling();
        }
        return prevSiblings;
    }

    public static List<DomNode> findNextSiblingElements(DomNode domNode) {
        List<DomNode> nextSiblings = new ArrayList<>();
        DomNode next = domNode.getNextElementSibling();
        while (next != null) {
            nextSiblings.add(next);
            next = next.getNextElementSibling();
        }
        return nextSiblings;
    }


    public static Optional<DomNode> toDomNode(Object obj) {
        if (obj instanceof DomNode) {
            DomNode node = (DomNode) obj;
            return Optional.of(node);
        }
        return Optional.empty();
    }

    public static Optional<HtmlElement> toHtmlElement(Object obj) {
        if (obj instanceof HtmlElement) {
            HtmlElement elem = (HtmlElement) obj;
            return Optional.of(elem);
        }
        return Optional.empty();
    }

}
