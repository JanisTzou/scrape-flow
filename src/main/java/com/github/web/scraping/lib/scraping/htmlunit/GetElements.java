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

import static com.github.web.scraping.lib.scraping.htmlunit.GetElementsByDomTraversal.*;

public class GetElements {

    public static class Descendants {

        public static class ByTag {

            public static GetElementsByTag tagName(String tagName) {
                return new GetElementsByTag(tagName);
            }

            public static GetElementsByTag body() {
                return new GetElementsByTag("body");
            }

            public static GetElementsByTag anchor() {
                return new GetElementsByTag("a");
            }

            public static GetElementsByTag div() {
                return new GetElementsByTag("div");
            }

            public static GetElementsByTag li() {
                return new GetElementsByTag("li");
            }

            public static GetElementsByTag span() {
                return new GetElementsByTag("span");
            }

            public static GetElementsByTag img() {
                return new GetElementsByTag("img");
            }

            public static GetElementsByTag article() {
                return new GetElementsByTag("article");
            }

        }


        public static class ByTextContent {

            public static GetElementsByTextContent search(String searchString, boolean matchWholeTextContent) {
                return new GetElementsByTextContent(searchString, matchWholeTextContent);
            }

        }


        public static class ByCss {

            public static GetElementsByCssClass byClassName(String className) {
                return new GetElementsByCssClass(className);
            }

            public static GetElementsByCssSelector bySelector(String selector) {
                return new GetElementsByCssSelector(selector);
            }

        }


        public static class ByAttribute {

            public static GetElementsByAttribute id(String idValue) {
                return new GetElementsByAttribute("id", idValue);
            }

            public static GetElementsByAttribute nameAndValue(String attrName, String attrValue) {
                return new GetElementsByAttribute(attrName, attrValue);
            }

            public static GetElementsByAttribute name(String attrName) {
                return new GetElementsByAttribute(attrName);
            }

        }


        public static class ByXPath {

            public static GetElementsByXPath xPath(String xPath) {
                return new GetElementsByXPath(xPath);
            }

        }
    }


    // TODO rename class to Traversal and the method prefix with getXXX ?
    public static class ByDomTraversal {

        public static GetElementsByDomTraversal body() {
            return new GetElementsByDomTraversal(Type.BODY);
        }

        public static GetElementsByDomTraversal parent() {
            return new GetElementsByDomTraversal(Type.PARENT);
        }

        public static GetElementsByDomTraversal nthParent(int nth) {
            return new GetElementsByDomTraversal(Type.NTH_PARENT, nth);
        }

        public static GetElementsByDomTraversal nextSiblingElem() {
            return new GetElementsByDomTraversal(Type.NEXT_SIBLING_ELEMENT);
        }

        public static GetElementsByDomTraversal prevSiblingElem() {
            return new GetElementsByDomTraversal(Type.PREV_SIBLING_ELEMENT);
        }

        public static GetElementsByDomTraversal firstChildElem() {
            return new GetElementsByDomTraversal(Type.FIRST_CHILD_ELEMENT);
        }

        public static GetElementsByDomTraversal firstNChildElems(int n) {
            return new GetElementsByDomTraversal(Type.FIRST_N_CHILD_ELEMENTS, n);
        }

        public static GetElementsByDomTraversal lastChildElem() {
            return new GetElementsByDomTraversal(Type.LAST_CHILD_ELEMENT);
        }

        public static GetElementsByDomTraversal lastNChildElems(int n) {
            return new GetElementsByDomTraversal(Type.LAST_N_CHILD_ELEMENTS, n);
        }

        public static GetElementsByDomTraversal nthChildElem(int nth) {
            return new GetElementsByDomTraversal(Type.NTH_CHILD_ELEMENT, nth);
        }

        public static GetElementsByDomTraversal childElems() {
            return new GetElementsByDomTraversal(Type.CHILD_ELEMENTS);
        }

    }

}
