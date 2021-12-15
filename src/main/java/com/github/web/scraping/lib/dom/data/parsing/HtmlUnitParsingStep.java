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

package com.github.web.scraping.lib.dom.data.parsing;

import com.gargoylesoftware.htmlunit.html.DomNode;

import java.util.List;

// TODO maybe rename to static ...
public abstract class HtmlUnitParsingStep {

    // TODO perhaps provide documentation that the DomNode can be either loadedPage or an HtmlElement?
    // predicates, sanitizers atc ...?
    public abstract List<ParsedElement> parse(DomNode loadedPage);   // TODO the input for this will be different for different drivers ...


    public static HtmlUnitParsingStepByFullXPath.Builder byXPath(String xPath) {
        return HtmlUnitParsingStepByFullXPath.builder().setxPath(xPath);
    }

    public static HtmlUnitParsingStepByFullXPath.Builder byXPath(Enum<?> identifier, String xPath) {
        return HtmlUnitParsingStepByFullXPath.builder().setIdentifier(identifier).setxPath(xPath);
    }


    public static HtmlUnitParsingStepIterableByFullXPath.Builder iterableByXPath(String xPath) {
        return HtmlUnitParsingStepIterableByFullXPath.builder().setxPath(xPath);
    }

    public static HtmlUnitParsingStepIterableByFullXPath.Builder iterableByXPath(Enum<?> identifier, String xPath) {
        return HtmlUnitParsingStepIterableByFullXPath.builder().setIdentifier(identifier).setxPath(xPath);
    }


    public static HtmlUnitParsingStepIteratedChildByFullXPath.Builder iteratedChildByXPath(String xPath) {
        return HtmlUnitParsingStepIteratedChildByFullXPath.builder().setxPath(xPath);
    }

    public static HtmlUnitParsingStepIteratedChildByFullXPath.Builder iteratedChildByXPath(Enum<?> identifier, String xPath) {
        return HtmlUnitParsingStepIteratedChildByFullXPath.builder().setIdentifier(identifier).setxPath(xPath);
    }


}
