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

import com.gargoylesoftware.htmlunit.html.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class HtmlUnitParsingStrategyByFullXPath implements HtmlUnitParsingStrategy {

    private final Enum<?> dataType;
    private final String xpath;

    @Override
    public List<ParsedElement> parse(DomNode loadedPage) {
        return loadedPage.getByXPath(xpath).stream()
                .map(el -> {
                    String href = null;
                    String tc = null;
                    if (el instanceof HtmlAnchor anch) {
                        href = anch.getHrefAttribute();
                    }
                    if (el instanceof HtmlElement htmlEl) {
                        tc = htmlEl.getTextContent();
                        if (tc != null) {
                            // this should be optional ... used in cases when child elements' content filthies the parent element's content ...
                            tc = removeNestedElementsTextContent(tc, htmlEl);
                            tc = tc.trim();
                        }
                    }
                    if (href != null || tc != null) {
                        return new ParsedElement(dataType, href, tc, el);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String removeNestedElementsTextContent(String textContent, HtmlElement el) {
        for (DomElement childElement : el.getChildElements()) {
            textContent = textContent.replace(childElement.getTextContent(), "");
        }
        return textContent;
    }

}
