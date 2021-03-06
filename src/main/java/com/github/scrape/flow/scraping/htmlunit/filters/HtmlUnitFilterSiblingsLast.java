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

package com.github.scrape.flow.scraping.htmlunit.filters;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.scrape.flow.scraping.Filter;

import java.util.Collections;
import java.util.List;

public class HtmlUnitFilterSiblingsLast implements Filter<DomNode> {

    @Override
    public List<DomNode> filter(List<DomNode> allNextSiblings) {
        if (allNextSiblings.isEmpty()) {
            return Collections.emptyList();
        } else {
            return List.of(allNextSiblings.get(allNextSiblings.size() - 1));
        }
    }

}
