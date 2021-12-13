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

package aaanew.utils;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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


    public static boolean hasAttributeWithValue(DomElement element, String attribute, String value, boolean exactMatch) {
        if (exactMatch) {
            return element.hasAttribute(attribute) && element.getAttribute(attribute).equals(value);
        } else {
            return element.hasAttribute(attribute) && element.getAttribute(attribute).contains(value);
        }
    }
}
