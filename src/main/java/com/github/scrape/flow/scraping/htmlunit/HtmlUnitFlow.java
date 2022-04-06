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
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.github.scrape.flow.scraping.CommonOperationsStepBase;
import com.github.scrape.flow.scraping.ScrapingStep;
import com.github.scrape.flow.scraping.htmlunit.filters.HtmlUnitFilterableSiblings;
import org.apache.commons.text.StringEscapeUtils;

import javax.imageio.ImageIO;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnitGetAncestor.Type;

/**
 * Class providing convenient entry point to all operations used to traverse, parse and perform actions on the scraped page.
 * These operations are "concatenated" in a fluent way.
 * <br>
 * They often work with the notion of "current element". This concept just expresses the fact that each individual element
 * found by one operation is passed to the next operation as its "current element" that it can perform further operations upon.
 */
public class HtmlUnitFlow {

    /**
     * Contains methods for DOM traversal. The <code>DomNode</code> or <code>HtmlElement</code> from which the traversal takes place
     * is generally provided by the previous step in the scraping sequence.
     */
    public static class Get {

        /**
         * Searches the DOM for <code>HtmlElement</code>s based on specified <code>xPathExpr</code> expression from the current <code>HtmlElement</code>.
         * Search is performed as specified in {@link DomNode#getByXPath(String)} and only <code>HtmlElement</code>s are filtered.
         */
        public static HtmlUnitGetElementsByXPath byXPath(String xPathExpr) {
            return new HtmlUnitGetElementsByXPath(xPathExpr);
        }

        /**
         * Searches the DOM the parent <code>HtmlElement</code>s of the current <code>HtmlElement</code>. Search is performed as specified in
         * {@link DomNode#getParentNode()}.
         */
        public static HtmlUnitGetAncestor parent() {
            return new HtmlUnitGetAncestor(Type.PARENT);
        }

        /**
         * Searches the DOM upwards for nth ancestor of the current <code>HtmlElement</code>. Search is performed recursively as specified in
         * {@link DomNode#getParentNode()}.
         */
        public static HtmlUnitGetAncestor ancestor(int nth) {
            return new HtmlUnitGetAncestor(Type.NTH_ANCESTOR, nth);
        }

        /**
         * Searches the DOM for siblings of the current <code>HtmlElement</code> based on specified filter methods specified by {@link HtmlUnitFilterableSiblings}
         * Search for sibling candidates is performed as specified by {@link DomNode#getPreviousElementSibling()} and {@link DomNode#getNextElementSibling()}.
         */
        public static HtmlUnitGetSiblings siblings() {
            return new HtmlUnitGetSiblings();
        }

        /**
         * Searches the DOM for descendants of the current <code>HtmlElement</code>.
         * Search for descendants is performed as specified by {@link DomNode#getHtmlElementDescendants()}.
         */
        public static HtmlUnitGetDescendants descendants() {
            return new HtmlUnitGetDescendants();
        }

        /**
         * Searches the DOM for descendants of the current <code>HtmlElement</code> by specified <code>sccSelector</code>
         * Search for descendants is performed as specified by {@link DomNode#querySelectorAll(String)} and only <code>HtmlElement</code>s are filtered.
         */
        public static HtmlUnitGetDescendantsByCssSelector descendantsBySelector(String sccSelector) {
            return new HtmlUnitGetDescendantsByCssSelector(sccSelector);
        }

        /**
         * Searches the DOM for children of the current <code>HtmlElement</code>.
         * Search for children is performed as specified by {@link DomNode#getChildNodes()} and only <code>HtmlElement</code>s are filtered.
         */
        public static HtmlUnitGetChildren children() {
            return new HtmlUnitGetChildren();
        }

        /**
         * Provides direct access to HtmlUnit's API's <code>DomNode</code> in cases when complex custom DOM traversal is needed.
         * @param mapper maps the current <code>DomNode</code> (usually a <code>HtmlElement</code>) to another <code>DomNode</code>
         */
        public static HtmlUnitGetElementsNatively natively(Function<DomNode, Optional<DomNode>> mapper) {
            return new HtmlUnitGetElementsNatively(mapper);
        }

    }


    /**
     * Contains methods for additional filtering of accessed DOM elements
     */
    public static class Filter {

        /**
         * Applies the specified filter on the current <code>DomNode</code> (usually an instance of <code>HtmlElement</code>)
         */
        public static HtmlUnitFilterElementsNatively natively(Predicate<DomNode> filter) {
            return new HtmlUnitFilterElementsNatively(filter);
        }

    }

    /**
     * Contains methods for parsing data from DOM elements
     */
    public static class Parse {

        /**
         * Parses the text content of the current <code>HtmlElement</code> as specified by {@link HtmlElement#getTextContent()} and unescapes
         * the retrieved value using {@link StringEscapeUtils#unescapeHtml4(java.lang.String)}
         */
        public static HtmlUnitParseElementTextContent textContent() {
            return new HtmlUnitParseElementTextContent();
        }

        /**
         * Same as {@link Parse#textContent()} but with the possibility to define a custom mapper of the parsed value
         */
        public static HtmlUnitParseElementTextContent textContent(Function<String, String> parsedValueMapper) {
            return new HtmlUnitParseElementTextContent().setValueMapper(parsedValueMapper);
        }

        /**
         * Parses the href attribute value of the current <code>HtmlElement</code> as specified by {@link HtmlAnchor#getHrefAttribute()}.
         * Note that the current element needs to be of type {@link HtmlAnchor}
         */
        public static HtmlUnitParseElementHRef hRef() {
            return new HtmlUnitParseElementHRef();
        }

        /**
         * Same as {@link Parse#hRef()} but with the possibility to define a custom mapper of the parsed value
         */
        public static HtmlUnitParseElementHRef hRef(Function<String, String> parsedValueMapper) {
            return new HtmlUnitParseElementHRef(parsedValueMapper);
        }

        /**
         * Parses the attribute value of the current <code>HtmlElement</code> as specified by {@link HtmlElement#getAttribute(String)}.
         */
        public static HtmlUnitParseElementAttributeValue attrValue(String attrName) {
            return new HtmlUnitParseElementAttributeValue(attrName);
        }

        /**
         * Same as {@link Parse#attrValue(String)} but with the possibility to define a custom mapper of the parsed value
         */
        public static HtmlUnitParseElementAttributeValue attrValue(String attrName, Function<String, String> parsedValueMapper) {
            return new HtmlUnitParseElementAttributeValue(attrName, parsedValueMapper);
        }

    }


    /**
     * Contains methods for various actions that can be performed with regard to the loaded page or specific DOM elements
     */
    public static class Do {

        /**
         * <p>Replaces the current page with a new one that is loaded after the link is followed.
         * <p><b>IMPORTANT</b>: use this only when there are no other steps working with the page.
         * If not sure, use {@link Do#navigateToParsedLink()}
         */
        public static HtmlUnitFollowLink followLink() {
            return new HtmlUnitFollowLink();
        }

        /**
         * Can be used to load a new page after a link has been parsed e.g. after {@link Parse#hRef()}.
         * <br>
         * Loads the site into a new page instance - this is important e.g. when we are scraping additional data from the same page.
         * If you are sure that parsing is finished at current page you at that point, use {@link Do#followLink()}
         */
        public static HtmlUnitNavigateToParsedLink navigateToParsedLink() {
            return new HtmlUnitNavigateToParsedLink();
        }

        /**
         * Used as first step in the scraping sequence to navigate to the entry-point page from which all the rest of the scraping takes place
         */
        public static HtmlUnitNavigateToUrl navigateToUrl(String url) {
            return new HtmlUnitNavigateToUrl(url);
        }

        /**
         * Used in pagination as the last step of the paginating sequence of steps
         */
        public static HtmlUnitReturnNextPage returnNextPage() {
            return new HtmlUnitReturnNextPage();
        }

        /**
         * Used to define a sequence of steps taking care of the pagination itself (specified in {@link HtmlUnitPaginate#setStepsLoadingNextPage(HtmlUnitScrapingStep)})
         * and also the sequence taking care of the scraping itself (specified using {@link CommonOperationsStepBase#next(ScrapingStep)} or its other specialisations.
         */
        public static HtmlUnitPaginate paginate() {
            return new HtmlUnitPaginate();
        }

        /**
         * Used to download image into a {@code BufferedImage} using {@link ImageIO#read(URL)}.
         * <br>
         * Note that the URL of the image needs to be scraped by one of the prevous steps in the scraping sequence.
         */
        public static HtmlUnitDownloadImage downloadImage() {
            return new HtmlUnitDownloadImage();
        }

    }


    /**
     * Contains methods that enable structuring the flow of the scraping
     */
    public static class Flow {

        /**
         * Used to group multiple same-level steps under a single "artificial" parent step. Can be useful in some cases e.g.
         * when there is a shared condition based on which a number of steps are executed, and we want that condition to be evaluated just once for all the affected steps.
         */
        public static HtmlUnitStepBlock asBlock() {
            return new HtmlUnitStepBlock();
        }

    }

    /**
     * Contains methods representing conditions based on which scraping steps are performed
     */
    public static class Condition {

        // TODO define sets of conditions that we can use in next() ... to avoid dealing with DomNode ...
        //  e.g. HasAnyDescendants of

    }

}
