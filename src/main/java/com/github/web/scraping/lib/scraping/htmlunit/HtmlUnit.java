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

import com.gargoylesoftware.htmlunit.html.DomNode;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.web.scraping.lib.scraping.htmlunit.GetElementsByDomTraversal.*;

public class HtmlUnit {

    public static class Get {

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


        public static class Descendants {

            public static class ByTag {

                public static GetElementsByTag tagName(String tagName) {
                    return new GetElementsByTag(tagName);
                }

                public static GetElementsByTag article() {
                    return new GetElementsByTag("article");
                }

                public static GetElementsByTag anchor() {
                    return new GetElementsByTag("a");
                }

                public static GetElementsByTag body() {
                    return new GetElementsByTag("body");
                }

                public static GetElementsByTag div() {
                    return new GetElementsByTag("div");
                }

                public static GetElementsByTag h1() {
                    return new GetElementsByTag("h1");
                }

                public static GetElementsByTag h2() {
                    return new GetElementsByTag("h2");
                }

                public static GetElementsByTag h3() {
                    return new GetElementsByTag("h3");
                }

                public static GetElementsByTag h4() {
                    return new GetElementsByTag("h4");
                }

                public static GetElementsByTag h5() {
                    return new GetElementsByTag("h5");
                }

                public static GetElementsByTag img() {
                    return new GetElementsByTag("img");
                }

                public static GetElementsByTag li() {
                    return new GetElementsByTag("li");
                }

                public static GetElementsByTag p() {
                    return new GetElementsByTag("p");
                }

                public static GetElementsByTag span() {
                    return new GetElementsByTag("span");
                }

                public static GetElementsByTag thead() {
                    return new GetElementsByTag("thead");
                }

                public static GetElementsByTag tbody() {
                    return new GetElementsByTag("tbody");
                }

                public static GetElementsByTag tr() {
                    return new GetElementsByTag("tr");
                }

                public static GetElementsByTag td() {
                    return new GetElementsByTag("td");
                }

                public static GetElementsByTag ul() {
                    return new GetElementsByTag("ul");
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
    }

    public static class Parse {

        public static ParseElementTextContent textContent() {
            return new ParseElementTextContent();
        }

        public static ParseElementTextContent textContent(Function<String, String> parsedTextTransformation) {
            return new ParseElementTextContent().setTransformation(parsedTextTransformation);
        }

        public static ParseElementHRef hRef() {
            return new ParseElementHRef();
        }

        public static ParseElementHRef hRef(Function<String, String> parsedTextTransformation) {
            return new ParseElementHRef(parsedTextTransformation);
        }

        // TODO attribute value etc ...

    }

    public static class Do {

        public static FilterElements filterElements(Predicate<DomNode> domNodePredicate) {
            return new FilterElements(domNodePredicate);
        }

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

    }

    public static class Flow {

        public static StepGroup asStepGroup() {
            return new StepGroup();
        }

    }

    // TODO rename to When/If ?
    public static class Conditions {

        // TODO define sets of conditions that we can use in next() ... to avoid dealing with DomNode ...

        // HasAnyDescendats of

    }
}
