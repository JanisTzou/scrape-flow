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

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class HtmlUnitParsingStrategyByFullXPath extends ByFullXPath implements HtmlUnitParsingStrategy {

    private final Enum<?> dataType;
    private final String xpath;


    @Override
    public Optional<ParsingResult> parse(HtmlPage loadedPage) {
        return loadedPage.getByXPath(xpath).stream()
                .map(o -> {
                    String href = null;
                    String textContent = null;
                    if (o instanceof HtmlAnchor) {
                        href = ((HtmlAnchor) o).getHrefAttribute();
                    }
                    if (o instanceof HtmlElement) {
                        textContent = ((HtmlElement) o).getTextContent();
                    }
                    if (href != null || textContent != null) {
                        return new ParsingResult(dataType, href, textContent);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst(); // TODO temporary ... return all ...
    }

}
