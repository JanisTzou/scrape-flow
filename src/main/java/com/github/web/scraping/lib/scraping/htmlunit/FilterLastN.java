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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FilterLastN implements Filter {

    private final int n;

    public FilterLastN(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be > 0");
        }
        this.n = n;
    }

    @Override
    public List<DomNode> filter(List<DomNode> list) {
        ArrayList<DomNode> copy = new ArrayList<>(list);
        Collections.reverse(copy);
        return  copy.stream()
                .limit(n)
                .collect(Collectors.toList());
    }

}
