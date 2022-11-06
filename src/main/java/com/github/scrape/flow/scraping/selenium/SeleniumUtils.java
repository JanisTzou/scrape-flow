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

package com.github.scrape.flow.scraping.selenium;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SeleniumUtils {

    private static final Logger log = LogManager.getLogger();

    public static boolean hasTagName(WebElement webElement, String tagName) {
        return webElement.getTagName().equalsIgnoreCase(tagName);
    }

    public static boolean hasAttribute(WebElement webElement, String attrName) {
        return webElement.getAttribute(attrName) != null;
    }

    public static boolean hasAttributeWithExactValue(WebElement element, String attribute, String value) {
        return hasAttribute(element, attribute) && element.getAttribute(attribute).equals(value);
    }

    public static boolean hasAttributeWithValueMatchingRegex(WebElement element, String attribute, String valueRegex) {
        if (hasAttribute(element, attribute)) {
            return element.getAttribute(attribute).matches(valueRegex);
        }
        return false;
    }

    public static boolean hasCssClass(WebElement element, String value) {
        if (hasAttribute(element, "class")) {
            return Arrays.stream(element.getAttribute("class").split(" "))
                    .map(String::trim)
                    .anyMatch(cls -> cls.equalsIgnoreCase(value));
        }
        return false;
    }


    public static String getAttributeValue(WebElement webElement, String attrName) {
        if (hasAttribute(webElement, attrName)) {
            return webElement.getAttribute(attrName);
        } else {
            throw new NoSuchElementException("The specified element has no attribute named: " + attrName);
        }
    }

    public static Optional<WebElement> getElement(WebElement rootElement, String valueContainingTag, String valueContainingAttribute) {
        List<WebElement> pElems = rootElement.findElements(By.tagName(valueContainingTag));
        for (WebElement pElem : pElems) {
            if (SeleniumUtils.hasAttribute(pElem, valueContainingAttribute)) {
                return Optional.ofNullable(pElem);
            }
        }
        return Optional.empty();
    }

    public static Optional<WebElement> getBodyElement(WebDriver webDriver, String inzIdentifier, String inzeratUrl) {
        List<WebElement> bodyElems = webDriver.findElements(By.tagName("body"));
        if (bodyElems.size() == 1) {
            return Optional.of(bodyElems.get(0));
        } else {
            log.error("Found {} body elements for {} at {}", bodyElems.size(), inzIdentifier, inzeratUrl);
            return Optional.empty();
        }
    }

    public static List<WebElement> getElements(WebElement rootElement, String valueContainingTag, String valueContainingAttribute, String valueContainingAttributesValue) {
        List<WebElement> pElems = rootElement.findElements(By.tagName(valueContainingTag));
        List<WebElement> result = new ArrayList<>();
        for (WebElement pElem : pElems) {
            if (SeleniumUtils.hasAttributeWithExactValue(pElem, valueContainingAttribute, valueContainingAttributesValue)) {
                result.add(pElem);
            }
        }
        return result;
    }

    public static List<WebElement> getDescendantsBySccSelector(WebElement webElement, String sccSelector) {
        return webElement.findElements(By.cssSelector(sccSelector));
    }

    public static Optional<WebElement> findParent(WebElement weElement) {
        try {
            return Optional.of(weElement.findElement(By.xpath("./..")));
        } catch (NoSuchElementException nsee) {
            return Optional.empty();
        }
    }

    public static Optional<WebElement> findNthAncestor(WebElement webElement, Integer nth) {
        if (nth != null && nth < 0) {
            throw new IllegalArgumentException("Cannot return nth ancestor element for n = " + nth + " - nth must be a non-null and non-negative integer!");
        } else {
            return findNthAncestorHelper(webElement, nth, 0);
        }
    }

    private static Optional<WebElement> findNthAncestorHelper(WebElement webElement, int nth, int count) {
        if (count == nth) {
            return Optional.of(webElement);
        } else {
            Optional<WebElement> parent = findParent(webElement);
            if (parent.isPresent()) {
                return findNthAncestorHelper(parent.get(), nth, ++count);
            } else {
                return Optional.empty();
            }
        }
    }

    public static List<WebElement> findByXPath(WebElement webElement, String xPathExpr) {
        return webElement.findElements(By.xpath(xPathExpr));
    }

    public static void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
