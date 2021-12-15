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
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.List;
import java.util.Optional;

// TODO maybe rename to static ...
public abstract class HtmlUnitParsingStrategy {

    // TODO perhaps provide documentation that the DomNode can be either loadedPage or an HtmlElement?
    // predicates, sanitizers atc ...?
    public abstract List<ParsedElement> parse(DomNode loadedPage);   // TODO the input for this will be different for different drivers ...


    public static HtmlUnitParsingStrategyByFullXPath.Builder byXPath(String xPath) {
        return HtmlUnitParsingStrategyByFullXPath.builder().setxPath(xPath);
    }

    public static HtmlUnitParsingStrategyIterableByFullXPath.Builder iterableByXPath(String xPath) {
        return HtmlUnitParsingStrategyIterableByFullXPath.builder().setxPath(xPath);
    }

    public static HtmlUnitParsingStrategyIteratedChildByFullXPath.Builder iteratedChildByXPath(String xPath) {
        return HtmlUnitParsingStrategyIteratedChildByFullXPath.builder().setxPath(xPath);
    }


}
