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
        return filterAndGetDescendants(parentElement, el -> hasAttributeWithValue(el, attributeName, attributeValue, exactMatch));
    }

    public static List<DomNode> getDescendantsByAttribute(DomNode parentElement, String attributeName) {
        return filterAndGetDescendants(parentElement, el -> el.hasAttribute(attributeName));
    }

    public static List<DomNode> getDescendantsByClass(DomNode parentElement, String cssClassName) {
        return filterAndGetDescendants(parentElement, el -> {
            if (el.hasAttribute("class")) {
                return Arrays.stream(el.getAttribute("class").split(" ")).anyMatch(ccls -> ccls.equalsIgnoreCase(cssClassName));
            }
            return false;
        });
    }

    public static List<DomNode> getDescendantsBySccSelector(DomNode parentElement, String selector) {
        DomNodeList<DomNode> domNodes = parentElement.querySelectorAll(selector);
        return domNodes;
    }

    // TODO there is a big problem here that some elements are being searched but only for descendants and in fact we want those same "parents" returned ... somehow express this in the
    //  steps that go for elements ... so that their filtering function is clear ...
    public static List<DomNode> getDescendantsByTagName(DomNode parentElement, String tagName) {
        return filterAndGetDescendants(parentElement, el -> el.getTagName().equalsIgnoreCase(tagName));
    }

    public static List<DomNode> filterAndGetDescendants(DomNode parentElement, Predicate<DomElement> filter) {
        List<DomNode> found = new ArrayList<>();
        for (DomNode childElement : parentElement.getHtmlElementDescendants()) {
            if (childElement instanceof HtmlElement htmlEl) {
                if (filter.test(htmlEl)) {
                    found.add(childElement);
                }
            }
        }
        return found;
    }

    public static boolean hasAttributeWithValue(DomNode domNode, String attribute, String value, boolean exactMatch) {
        if (domNode instanceof DomElement) {
            return hasAttributeWithValue((DomElement) domNode, attribute, value, exactMatch);
        }
        return false;
    }

    public static boolean hasAttributeWithValue(DomElement element, String attribute, String value, boolean exactMatch) {
        if (exactMatch) {
            return element.hasAttribute(attribute) && element.getAttribute(attribute).equals(value);
        } else {
            return element.hasAttribute(attribute) && element.getAttribute(attribute).contains(value);
        }
    }


    public static Optional<DomNode> findNthParent(DomNode domNode, int nth) {
        if (nth < 0) {
            throw new IllegalArgumentException("Cannot return nth child element for n = " + nth + " - nth must be a non-negative integer!");
        }
        return findNthParentHelper(domNode, nth, 0);
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


}
