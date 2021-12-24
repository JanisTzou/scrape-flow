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

package com.github.web.scraping.lib.scraping.selenium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeleniumUtils {

    private static final Logger log = LogManager.getLogger();

    public static boolean hasAttribute(WebElement webElement, String attrName) {
        boolean result = webElement.getAttribute(attrName) != null;
        return result;
    }


    public static boolean hasAttributeWithValue(WebElement element, String attribute, String value) {
        return hasAttribute(element, attribute) && element.getAttribute(attribute).equals(value);
    }


    public static String getAttributeValue(WebElement webElement, String attrName) {
        if (hasAttribute(webElement, attrName)) {
            return webElement.getAttribute(attrName);
        } else {
            throw new NoSuchElementException("The specified element has no attribute named: " + attrName);
        }
    }


    public static Optional<WebElement> getElement(WebElement rootElement, String valueContainingTag, String valueContainingAttribute, String valueContainingAttributesValue) {
        List<WebElement> pElems = rootElement.findElements(By.tagName(valueContainingTag));
        for (WebElement pElem : pElems) {
            if (SeleniumUtils.hasAttributeWithValue(pElem, valueContainingAttribute, valueContainingAttributesValue)) {
                return Optional.ofNullable(pElem);
            }
        }
        return Optional.empty();
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
            log.error("Found {} body elements for {} at {}", inzIdentifier, inzeratUrl);
            return Optional.empty();
        }
    }

    public static List<WebElement> getElements(WebElement rootElement, String valueContainingTag, String valueContainingAttribute, String valueContainingAttributesValue) {
        List<WebElement> pElems = rootElement.findElements(By.tagName(valueContainingTag));
        List<WebElement> result = new ArrayList<>();
        for (WebElement pElem : pElems) {
            if (SeleniumUtils.hasAttributeWithValue(pElem, valueContainingAttribute, valueContainingAttributesValue)) {
                result.add(pElem);
            }
        }
        return result;
    }


    public static void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
