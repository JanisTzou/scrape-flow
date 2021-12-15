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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class HtmlUnitParsingStrategyIterableByFullXPath implements HtmlUnitParsingStrategy {

    private final Enum<?> identifier;

    // TODO convert these into patterns so we can generate a range of xpaths ...
    private final String xPath;

    // for each iterated element these strategies will be applied to parse data ...
    private final List<HtmlUnitParsingStrategy> strategies;

    // TODO optionally specify pagination strategy ? that will be called at the end ?

    public HtmlUnitParsingStrategyIterableByFullXPath(Enum<?> identifier,
                                                      String xPath,
                                                      List<HtmlUnitParsingStrategy> strategies) {
        this.identifier = identifier;
        this.xPath = xPath;
        this.strategies = strategies;
    }

    // TODO shoud this be returning this?
    @Override
    public List<ParsedElement> parse(DomNode loadedPage) {

        String parentXPath = XPathUtils.getXPathSubstrHead(xPath, 1);
        String xPathTail = XPathUtils.getXPathSubstrTail(xPath, 1).replaceAll("\\d+", "\\\\d");


        // hmm what cna the tail contain ? ... the tag name ...
        //  ... we want to transform it into patterns of sorts ... so we can use it below ...

//        String pattern = XPathUtils.regexEscape(parentXPath) + "\\/div\\[\\d+\\]";// TODO hardcoded tempoarily ...
        String pattern = XPathUtils.regexEscape(XPathUtils.concat(parentXPath, xPathTail));

        System.out.println(loadedPage.asXml());

        // TODO hmm what if the parent is a nested XPath in itself ? ...
        return loadedPage.getByXPath(parentXPath)
                .stream()
                .flatMap(el -> {
                    // child elements ...
                    if (el instanceof HtmlElement htmlEl) {
                        return StreamSupport.stream(htmlEl.getChildElements().spliterator(), false);
                    }
                    return Stream.empty();
                })
                .filter(el -> {
                    if (el instanceof HtmlElement htmlEl) {
                        String xPath = htmlEl.getCanonicalXPath();
                        boolean matches = xPath.matches(pattern);
                        if (matches) {
//                            System.out.println("Matched listing xPath = " + xPath);
                        }
                        return matches;
                    }
                    return false;
                })
                .flatMap(el -> {
                    if (el instanceof HtmlElement htmlEl) {
                        return strategies.stream().flatMap(s -> s.parse(htmlEl).stream());
                    }
                    return Stream.empty();
                })
                .collect(Collectors.toList());



        // here we want to identify all the elements that will then be processed by the strategies above?
        // ... so we can for example apply specific HtmlUnitParsingStrategyByFullXPath on each one of them ... BUT the expaths will need to be dynamic as the root will change for each listed item ....

    }

}
