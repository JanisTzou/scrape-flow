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

import com.github.scrape.flow.scraping.FilterableSiblings;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;
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
         * Searches the DOM of the whole the page for elements.
         */
        public static SeleniumGetElements elements() {
            return new SeleniumGetElements();
        }

        /**
         * Searches the DOM for descendants of the current <code>WebElement</code> by specified <code>sccSelector</code>.
         * Search for descendants is performed as specified by {@link org.openqa.selenium.By#cssSelector(String) By.cssSelector(String)}.
         */
        @Deprecated
        // TODO actually create a filter and use that ...
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

        public static SeleniumParseTextContent textContent() {
            return new SeleniumParseTextContent();
        }

    }


    public static class Do {

        // TODO followLink()

        public static SeleniumNavigateToParsedLink navigateToParsedLink() {
            return new SeleniumNavigateToParsedLink();
        }

        /**
         * Used as first step in the scraping sequence to navigate to the entry-point page from which all the rest of the scraping takes place
         */
        public static SeleniumNavigateToUrl navigateTo(String url) {
            return new SeleniumNavigateToUrl(url);
        }

        /**
         * Used to download image into a {@code BufferedImage} using {@link ImageIO#read(URL)}.
         * <br>
         * Note that the URL of the image needs to be scraped by one of the previous steps in the scraping sequence.
         */
        public static SeleniumDownloadImage downloadImage() {
            return new SeleniumDownloadImage();
        }

        public static SeleniumSendKeys sendKeys(String text) {
            return new SeleniumSendKeys(text);
        }

        public static SeleniumSubmit submit() {
            return new SeleniumSubmit();
        }

        public static SeleniumClick click() {
            return new SeleniumClick();
        }

        public static SeleniumPause pause(long millis) {
            return new SeleniumPause(millis);
        }

        public static SeleniumReloadPage reloadPage() {
            return new SeleniumReloadPage();
        }

        public static SeleniumPeek peek(Consumer<WebElement> consumer) {
            return new SeleniumPeek(consumer);
        }

        // TODO expose more of Seleniums possibilities ... such as execute script

    }

    /**
     * Contains methods that enable structuring the flow of the scraping
     */
    public static class Flow {

        /**
         * Used to group multiple same-level steps under a single "artificial" parent step. Can be useful in some cases e.g.
         * when there is a shared condition based on which a number of steps are executed, and we want that condition to be evaluated just once for all the affected steps.
         */
        public static SeleniumStepBlock asBlock() {
            return new SeleniumStepBlock();
        }

        // TODO returnNextPage()
        // TODO withPagination()

    }


    }
