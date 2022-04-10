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

package com.github.scrape.flow.scraping.htmlunit;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ScrapingContext;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class HtmlUnitGetAncestorRunnable implements Runnable {

    private final int param;
    private final ScrapingContext ctx;
    private final StepOrder stepOrder;
    private final HtmlUnitNodeSearchBasedStepHelper helper;

    @Override
    public void run() {
        DomNode node = ctx.getNode();
        Supplier<List<DomNode>> nodesSearch = () -> HtmlUnitUtils.findNthAncestor(node, param).stream().collect(Collectors.toList());
        helper.execute(nodesSearch, ctx, stepOrder);
    }

}
