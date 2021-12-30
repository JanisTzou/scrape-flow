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

package com.github.scraping.flow.scraping.htmlunit;

import com.gargoylesoftware.htmlunit.html.DomNode;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.scraping.flow.scraping.htmlunit.GetParent.Type;

public class HtmlUnit {

    public static class Get {

        public static GetElementsByXPath byXPath(String xPath) {
            return new GetElementsByXPath(xPath);
        }

        public static GetParent parent() {
            return new GetParent(Type.PARENT);
        }

        public static GetParent nthParent(int nth) {
            return new GetParent(Type.NTH_PARENT, nth);
        }

        public static GetSiblings siblings() {
            return new GetSiblings();
        }

        public static GetDescendants descendants() {
            return new GetDescendants();
        }

        // this needs to be here ... cannot go under the descendants even thouth it gets the descendants
        public static GetDescendantsByCssSelector descendantsBySelector(String sccSelector) {
            return new GetDescendantsByCssSelector(sccSelector);
        }

        public static GetChildren children() {
            return new GetChildren();
        }

    }


    /**
     * In case some complex custom filtering logic is needed these filters can be used
     */
    public static class Filter {

        public static FilterElements apply(Predicate<DomNode> domNodePredicate) {
            return new FilterElements(domNodePredicate);
        }

    }


    public static class Parse {

        public static ParseElementTextContent textContent() {
            return new ParseElementTextContent();
        }

        public static ParseElementTextContent textContent(Function<String, String> parsedTextConverter) {
            return new ParseElementTextContent().setValueConversion(parsedTextConverter);
        }

        public static ParseElementHRef hRef() {
            return new ParseElementHRef();
        }

        public static ParseElementHRef hRef(Function<String, String> parsedTextConverter) {
            return new ParseElementHRef(parsedTextConverter);
        }

        public static ParseElementAttributeValue attrValue(String attrName, Function<String, String> parsedTextConverter) {
            return new ParseElementAttributeValue(attrName, parsedTextConverter);
        }

        public static ParseElementAttributeValue attrValue(String attrName) {
            return new ParseElementAttributeValue(attrName);
        }

    }


    public static class Do {

        public static MapElements mapElements(Function<DomNode, Optional<DomNode>> mapper) {
            return new MapElements(mapper);
        }

        /**
         * <p>Replaces the current page with a new one that is loaded after the link is followed.
         * <p><b>IMPORTANT</b>: use this only when there are no other steps working with the page.
         * If not sure, use {@link Do#navigateToParsedLink(HtmlUnitSiteParser)}
         */
        public static FollowLink followLink() {
            return new FollowLink();
        }

        /**
         * Can be used to load a new page after a link has been parsed e.g. after {@link Parse#hRef()}.
         * <br>
         * Loads the site into a new page instance - this is important e.g. when we are scraping additional data from the same page.
         * If you are sure that parsing is finished at current page you at that point, use {@link Do#followLink()}
         */
        public static NavigateToParsedLink navigateToParsedLink(HtmlUnitSiteParser siteParser) {
            return new NavigateToParsedLink(siteParser);
        }

        public static ReturnNextPage returnNextPage() {
            return new ReturnNextPage();
        }

        public static Paginate paginate() {
            return new Paginate();
        }


        public static DownloadImage downloadImage() {
            return new DownloadImage();
            // TODO other types of files?
        }
    }


    public static class Flow {

        public static StepBlock asBlock() {
            return new StepBlock();
        }

    }


    // TODO rename to When/If ?
    public static class Conditions {

        // TODO define sets of conditions that we can use in next() ... to avoid dealing with DomNode ...

        // HasAnyDescendats of
    }

}