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
import com.github.web.scraping.lib.parallelism.StepExecOrder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class GetElementsByDomTraversal extends CommonOperationsStepBase<GetElementsByDomTraversal> {

    private final Type type;
    private final Integer param;

    GetElementsByDomTraversal(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps, Type type, @Nullable Integer param) {
        super(nextSteps);
        this.type = type;
        this.param = param;
    }

    GetElementsByDomTraversal(Type type, Integer param) {
        this(null, type, param);
    }

    GetElementsByDomTraversal(Type type) {
        this(null, type, null);
    }

    @Override
    protected GetElementsByDomTraversal copy() {
        return copyFieldValuesTo(new GetElementsByDomTraversal(type, param));
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = () -> {
                DomNode node = ctx.getNode();
                return switch (type) {
                    case BODY -> node.querySelectorAll("html body");
                    case PARENT -> List.of(node.getParentNode());
                    case NTH_PARENT -> HtmlUnitUtils.findNthParent(node, param).stream().toList();
                    case NEXT_SIBLING_ELEMENT -> HtmlUnitUtils.findNextSiblingElement(node).stream().toList();
                    case PREV_SIBLING_ELEMENT -> HtmlUnitUtils.findPrevSiblingElement(node).stream().toList();
                    case FIRST_CHILD_ELEMENT -> HtmlUnitUtils.findFirstChildElement(node).stream().toList();
                    case LAST_CHILD_ELEMENT -> HtmlUnitUtils.findLastChildElement(node).stream().toList();
                    case NTH_CHILD_ELEMENT -> HtmlUnitUtils.findNthChildElement(node, param).stream().toList();
                    case FIRST_N_CHILD_ELEMENTS -> HtmlUnitUtils.findFirstNChildElements(node, param).stream().toList();
                    case LAST_N_CHILD_ELEMENTS -> HtmlUnitUtils.findLastNChildElements(node, param).stream().toList();
                    case CHILD_ELEMENTS -> HtmlUnitUtils.findChildElements(node).stream().toList();
                };
            };
            getHelper().execute(ctx, nodesSearch, stepExecOrder, getExecuteIf());
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    public enum Type {

        // https://www.digitalocean.com/community/tutorials/how-to-traverse-the-dom

        BODY,

        PARENT,
        NTH_PARENT,

        NEXT_SIBLING_ELEMENT,
        PREV_SIBLING_ELEMENT,

        FIRST_CHILD_ELEMENT,
        LAST_CHILD_ELEMENT,

        NTH_CHILD_ELEMENT,

        FIRST_N_CHILD_ELEMENTS,
        LAST_N_CHILD_ELEMENTS,

        CHILD_ELEMENTS
    }

}
