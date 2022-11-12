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
import com.github.scrape.flow.scraping.FilterableSiblings;
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitFilterElementsNatively;
import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.WebElement;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class Selenium {


    public static class Get {

        /**
         * Searches the DOM for <code>WebElement</code>s based on specified <code>xPathExpr</code> expression from the current <code>WebElement</code>.
         * Search is performed as specified in {@link org.openqa.selenium.By#xpath(String) By.xpath(String)}.
         */
        public static SeleniumGetElementsByXPath byXPath(String xPathExpr) {
            return new SeleniumGetElementsByXPath(xPathExpr);
        }

        /**
         * Searches the DOM the parent <code>WebElement</code>s of the current <code>WebElement</code>.
         */
        public static SeleniumGetAncestor parent() {
            return new SeleniumGetAncestor(1);
        }

        /**
         * Searches the DOM upwards for nth ancestor of the current <code>WebElement</code>.
         */
        public static SeleniumGetAncestor ancestor(int nth) {
            return new SeleniumGetAncestor(nth);
        }

        /**
         * Searches the DOM for siblings of the current <code>WebElement</code> while applying
         * the specified filter methods specified by {@link FilterableSiblings}
         */
        public static SeleniumGetSiblings siblings() {
            return new SeleniumGetSiblings();
        }

        /**
         * Searches the DOM for descendants of the current <code>WebElement</code>.
         */
        public static SeleniumGetDescendants descendants() {
            return new SeleniumGetDescendants();
        }

        /**
         * Searches the DOM for descendants of the current <code>WebElement</code> by specified <code>sccSelector</code>.
         * Search for descendants is performed as specified by {@link org.openqa.selenium.By#cssSelector(String) By.cssSelector(String)}.
         */
        public static SeleniumGetDescendantsByCssSelector descendantsBySelector(String sccSelector) {
            return new SeleniumGetDescendantsByCssSelector(sccSelector);
        }

        /**
         * Searches the DOM for children of the current <code>WebElement</code>.
         */
        public static SeleniumGetChildren children() {
            return new SeleniumGetChildren();
        }

        /**
         * Provides direct access to Selenium's API's <code>WebElement</code> in cases when complex custom DOM traversal is needed.
         * @param mapper maps the current <code>WebElement</code> to another <code>WebElement</code>
         */
        public static SeleniumGetElementsNatively natively(Function<WebElement, Optional<WebElement>> mapper) {
            return new SeleniumGetElementsNatively(mapper);
        }

    }


    /**
     * Contains methods for additional filtering of accessed DOM elements
     */
    public static class Filter {

        /**
         * Applies the specified filter on the current <code>WebElement</code>
         */
        public static SeleniumFilterElementsNatively natively(Predicate<WebElement> filter) {
            return new SeleniumFilterElementsNatively(filter);
        }

    }


    public static class Parse {

        public static SeleniumParseElementHRef hRef() {
            return new SeleniumParseElementHRef();
        }

        public static SeleniumParseElementHRef hRef(Function<String, String> parsedValueMapper) {
            return new SeleniumParseElementHRef(parsedValueMapper);
        }

        public static SeleniumParseElementTextContent textContent() {
            return new SeleniumParseElementTextContent();
        }

    }


    public static class Do {

        /**
         * Used as first step in the scraping sequence to navigate to the entry-point page from which all the rest of the scraping takes place
         */
        public static SeleniumNavigateToUrl navigateToUrl(String url) {
            return new SeleniumNavigateToUrl(url);
        }

        public static SeleniumNavigateToParsedLink navigateToParsedLink() {
            return new SeleniumNavigateToParsedLink();
        }

    }


}
