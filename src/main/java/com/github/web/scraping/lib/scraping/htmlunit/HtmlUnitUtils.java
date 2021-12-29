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

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HtmlUnitUtils {

    private static final Logger log = LogManager.getLogger();

    public static Optional<String> getChildElementTextContent(DomElement rootElement, String childElemTag, String childElemTagAttribute, String tagAttributeContent, boolean exactMatch) {
        Optional<DomElement> elementOp = getChildElement(rootElement, childElemTag, childElemTagAttribute, tagAttributeContent, exactMatch);
        return elementOp.map(domElement -> domElement.getTextContent().trim().replace("\n", "").replace("\t", ""));
    }

    public static List<String> getChildElementsTextContent(DomElement rootElement, String childElemTag) {
        List<HtmlElement> domElements = rootElement.getElementsByTagName(childElemTag);
        return domElements.stream()
                .map(domElement -> domElement.getTextContent().trim().replace("\n", "").replace("\t", ""))
                .collect(Collectors.toList());
    }

    public static Optional<String> getAttributeValueByItsExpectedPart(DomElement rootElement, String childElemTag, String childElemTagAttribute, String expectedPartOfAttrContent) {
        Optional<DomElement> elementOp = getChildElement(rootElement, childElemTag, childElemTagAttribute, expectedPartOfAttrContent, false);
        return elementOp.map(domElement -> domElement.getAttribute(childElemTagAttribute));
    }


    public static Optional<DomElement> getChildElement(DomElement parentElement, String childElemTag, String childElemTagAttribute, String tagAttributeContent, boolean exactMatch) {
        DomNodeList<HtmlElement> allElemsForTag = parentElement.getElementsByTagName(childElemTag);
        for (HtmlElement elem : allElemsForTag) {
            boolean foundElem = hasAttributeWithValue(elem, childElemTagAttribute, tagAttributeContent, exactMatch);
            if (foundElem) {
                return Optional.ofNullable(elem);
            }
        }
        return Optional.empty();
    }


    public static List<String> getAllAttributeValuesFromChildElements(DomElement parentElement, String childElemTag, String childElemTagAttribute) {
        List<DomElement> elements = getAllElementsByAttribute(parentElement, childElemTag, childElemTagAttribute);
        List<String> result = new ArrayList<>();
        for (DomElement element : elements) {
            String attribute = element.getAttribute(childElemTagAttribute);
            if (attribute != null || attribute.trim().isEmpty()) {
                result.add(attribute);
            } else {
                log.warn("Retrieved null or empty attribute value for childElemTag = {} and childElemTagAttribute = {}", childElemTag, childElemTagAttribute);
            }
        }
        return result;
    }

    public static List<DomElement> getAllElementsByAttribute(DomElement rootElement, String tag, String tagAttribute) {
        DomNodeList<HtmlElement> allElemsForTag = rootElement.getElementsByTagName(tag);
        List<DomElement> result = new ArrayList<>();
        for (HtmlElement element : allElemsForTag) {
            if (element.hasAttribute(tagAttribute)) {
                result.add(element);
            }
        }
        return result;
    }


    public static List<DomElement> getAllChildElements(DomElement parentElement, String childElemTag, String childElemTagAttribute, String tagAttributeContent, boolean exactMatch) {
        DomNodeList<HtmlElement> allElemsForTag = parentElement.getElementsByTagName(childElemTag);
        List<DomElement> result = new ArrayList<>();
        for (HtmlElement elem : allElemsForTag) {
            boolean found = hasAttributeWithValue(elem, childElemTagAttribute, tagAttributeContent, exactMatch);
            if (found) {
                result.add(elem);
            }
        }
        return result;
    }

    public static List<DomNode> getDescendantsByAttributeValue(DomNode parentElement, String attributeName, String attributeValue, boolean exactMatch) {
        return getHtmlElementDescendants(parentElement, el -> hasAttributeWithValue(el, attributeName, attributeValue, exactMatch));
    }

    public static List<DomNode> getDescendantsByAttribute(DomNode parentElement, String attributeName) {
        return getHtmlElementDescendants(parentElement, el -> el instanceof HtmlElement && ((HtmlElement) el).hasAttribute(attributeName));
    }

    public static List<DomNode> getDescendantsByClass(DomNode parentElement, String cssClassName) {
        return getHtmlElementDescendants(parentElement, el -> hasCssClass(el, cssClassName));
    }

    public static boolean hasCssClass(DomNode domNode, String cssClassName) {
        if (domNode instanceof HtmlElement el) {
            if (el.hasAttribute("class")) {
                return Arrays.stream(el.getAttribute("class").split(" ")).anyMatch(ccls -> ccls.equalsIgnoreCase(cssClassName));
            }
        }
        return false;
    }

    public static boolean hasTagName(DomNode domNode, String tagName) {
        if (domNode instanceof HtmlElement el) {
            return el.getTagName().equalsIgnoreCase(tagName);
        }
        return false;
    }

    public static List<DomNode> getDescendantsBySccSelector(DomNode domNode, String selector) {
        return domNode.querySelectorAll(selector);
    }


    public static List<DomNode> getDescendants(DomNode parentElement, Predicate<DomNode> filter) {
        List<DomNode> found = new ArrayList<>();
        for (DomNode desc : parentElement.getDescendants()) {
            if (filter.test(desc)) {
                found.add(desc);
            }
        }
        return found;
    }

    public static List<DomNode> getHtmlElementDescendants(DomNode parentElement, Predicate<DomNode> filter) {
        List<DomNode> found = new ArrayList<>();
        for (DomNode desc : parentElement.getHtmlElementDescendants()) {
            if (desc instanceof HtmlElement htmlEl) {
                if (filter.test(htmlEl)) {
                    found.add(desc);
                }
            }
        }
        return found;
    }

    public static boolean hasAttributeWithValue(DomNode domNode, String attribute, String value, boolean exactMatch) {
        if (domNode instanceof HtmlElement element) {
            if (exactMatch) {
                return element.hasAttribute(attribute) && element.getAttribute(attribute).equals(value);
            } else {
                return element.hasAttribute(attribute) && element.getAttribute(attribute).contains(value);
            }
        } else {
            return false;
        }
    }

    public static boolean hasAttributeWithValue(DomNode domNode, String attribute, String valueRegex) {
        if (domNode instanceof HtmlElement element) {
            if (element.hasAttribute(attribute)) {
                return element.getAttribute(attribute).matches(valueRegex);
            }
        }
        return false;
    }

    public static boolean hasAttribute(DomNode domNode, String attribute) {
        if (domNode instanceof HtmlElement element) {
            return element.hasAttribute(attribute);
        } else {
            return false;
        }
    }

    public static Optional<DomNode> findNthParent(DomNode domNode, Integer nth) {
        if (nth != null && nth < 0) {
            throw new IllegalArgumentException("Cannot return nth child element for n = " + nth + " - nth must be a non-null and non-negative integer!");
        } else {
            return findNthParentHelper(domNode, nth, 0);
        }
    }

    private static Optional<DomNode> findNthParentHelper(DomNode domNode, int nth, int count) {
        if (count == nth) {
            return Optional.of(domNode);
        } else {
            if (domNode.getParentNode() != null) {
                return findNthParentHelper(domNode.getParentNode(), nth, ++count);
            } else {
                return Optional.empty();
            }
        }
    }

    public static Optional<DomNode> findNextSiblingElement(DomNode domNode) {
        return Optional.ofNullable(domNode.getNextElementSibling());
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

    public static Optional<DomNode> findPrevSiblingElement(DomNode domNode) {
        return Optional.ofNullable(domNode.getPreviousElementSibling());
    }

    public static Optional<DomNode> findFirstChildElement(DomNode domNode) {
        return findNthChildElement(domNode, 1);
    }

    public static Optional<DomNode> findLastChildElement(DomNode domNode) {
        return findLastNChildElements(domNode, 1).stream().findFirst();
    }

    public static Optional<DomNode> findNthChildElement(DomNode domNode, int nth) {
        if (nth <= 0) {
            throw new IllegalArgumentException("Cannot return nth child element for nth = " + nth + " - nth must be a positive integer!");
        }
        return domNode.getChildNodes().stream().filter(node -> node instanceof HtmlElement)
                .skip(nth - 1)
                .limit(1)
                .findFirst();
    }

    public static List<DomNode> findFirstNChildElements(DomNode domNode, int n) {
        return domNode.getChildNodes().stream().filter(node -> node instanceof HtmlElement).limit(n).collect(Collectors.toList());
    }

    public static List<DomNode> findLastNChildElements(DomNode domNode, int n) {
        List<HtmlElement> elements = domNode.getChildNodes().stream().filter(node -> node instanceof HtmlElement).map(node -> (HtmlElement) node).collect(Collectors.toList());
        List<HtmlElement> elementsMutCopy = new ArrayList<>(elements);
        Collections.reverse(elementsMutCopy);
        return elementsMutCopy.stream().limit(n).collect(Collectors.toList());
    }

    public static List<DomNode> findChildElements(DomNode domNode) {
        return domNode.getChildNodes().stream().filter(node -> node instanceof HtmlElement).map(node -> (HtmlElement) node).collect(Collectors.toList());
    }

    public static Optional<DomNode> toDomNode(Object obj) {
        if (obj instanceof DomNode node) {
            return Optional.of(node);
        }
        return Optional.empty();
    }

    public static Optional<HtmlElement> toHtmlElement(Object obj) {
        if (obj instanceof HtmlElement elem) {
            return Optional.of(elem);
        }
        return Optional.empty();
    }

}
