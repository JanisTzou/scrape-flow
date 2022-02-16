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
import com.github.scrape.flow.scraping.htmlunit.filters.HtmlUnitFilterElements;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.scrape.flow.scraping.htmlunit.HtmlUnitGetAncestor.Type;

public class HtmlUnit {

    public static class Get {

        public static HtmlUnitGetElementsByXPath byXPath(String xPath) {
            return new HtmlUnitGetElementsByXPath(xPath);
        }

        public static HtmlUnitGetAncestor parent() {
            return new HtmlUnitGetAncestor(Type.PARENT);
        }

        public static HtmlUnitGetAncestor ancestor(int nth) {
            return new HtmlUnitGetAncestor(Type.NTH_ANCESTOR, nth);
        }

        public static HtmlUnitGetSiblings siblings() {
            return new HtmlUnitGetSiblings();
        }

        public static HtmlUnitGetDescendants descendants() {
            return new HtmlUnitGetDescendants();
        }

        // this needs to be here ... cannot go under the descendants even though it gets the descendants
        public static HtmlUnitGetDescendantsByCssSelector descendantsBySelector(String sccSelector) {
            return new HtmlUnitGetDescendantsByCssSelector(sccSelector);
        }

        public static HtmlUnitGetChildren children() {
            return new HtmlUnitGetChildren();
        }

    }


    /**
     * In case some complex custom filtering logic is needed these filters can be used
     */
    public static class Filter {

        public static HtmlUnitFilterElements apply(Predicate<DomNode> domNodePredicate) {
            return new HtmlUnitFilterElements(domNodePredicate);
        }

    }


    public static class Parse {

        public static HtmlUnitParseElementTextContent textContent() {
            return new HtmlUnitParseElementTextContent();
        }

        public static HtmlUnitParseElementTextContent textContent(Function<String, String> parsedTextConverter) {
            return new HtmlUnitParseElementTextContent().setValueConversion(parsedTextConverter);
        }

        public static HtmlUnitParseElementHRef hRef() {
            return new HtmlUnitParseElementHRef();
        }

        public static HtmlUnitParseElementHRef hRef(Function<String, String> parsedTextConverter) {
            return new HtmlUnitParseElementHRef(parsedTextConverter);
        }

        public static HtmlUnitParseElementAttributeValue attrValue(String attrName, Function<String, String> parsedTextConverter) {
            return new HtmlUnitParseElementAttributeValue(attrName, parsedTextConverter);
        }

        public static HtmlUnitParseElementAttributeValue attrValue(String attrName) {
            return new HtmlUnitParseElementAttributeValue(attrName);
        }

    }


    public static class Do {

        public static HtmlUnitMapElements mapElements(Function<DomNode, Optional<DomNode>> mapper) {
            return new HtmlUnitMapElements(mapper);
        }

        /**
         * <p>Replaces the current page with a new one that is loaded after the link is followed.
         * <p><b>IMPORTANT</b>: use this only when there are no other steps working with the page.
         * If not sure, use {@link Do#navigateToParsedLink(HtmlUnitSiteLoader)}
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
        public static HtmlUnitNavigateToParsedLink navigateToParsedLink(HtmlUnitSiteLoader siteParser) { // TODO it should not be necessary to pass the siteParser
            return new HtmlUnitNavigateToParsedLink(siteParser);
        }

        public static HtmlUnitNavigateToUrl navigateToUrl(String url) {
            return new HtmlUnitNavigateToUrl(url);
        }

        public static HtmlUnitReturnNextPage returnNextPage() {
            return new HtmlUnitReturnNextPage();
        }

        public static HtmlUnitPaginate paginate() {
            return new HtmlUnitPaginate();
        }


        public static HtmlUnitDownloadImage downloadImage() {
            return new HtmlUnitDownloadImage();
            // TODO other types of files?
        }
    }


    public static class Flow {

        public static HtmlUnitStepBlock asBlock() {
            return new HtmlUnitStepBlock();
        }

    }


    // TODO rename to When/If ?
    public static class Conditions {

        // TODO define sets of conditions that we can use in next() ... to avoid dealing with DomNode ...

        // HasAnyDescendants of
    }

}
