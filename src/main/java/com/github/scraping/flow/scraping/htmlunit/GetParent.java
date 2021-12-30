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

package com.github.scraping.flow.scraping.htmlunit;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.scraping.flow.parallelism.StepExecOrder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class GetParent extends CommonOperationsStepBase<GetParent>
    // TODO these filters do not ake sense for the parent as they are .... if we want to provide them, the impementation must go throguh all parents until it fineds the one specificed by the filters ...
    //  if ued as they are the filters might just filter away the one parent found ...
//        implements
//        FilterableByAttribute<GetParent>,
//        FilterableByTag<GetParent>,
//        FilterableByTextContent<GetParent>,
//        FilterableByCssClass<GetParent>
{

    private final Type type;
    private final Integer param;

    GetParent(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps, Type type, @Nullable Integer param) {
        super(nextSteps);
        this.type = type;
        this.param = param;
    }

    GetParent(Type type, Integer param) {
        this(null, type, param);
    }

    GetParent(Type type) {
        this(null, type, null);
    }

    @Override
    protected GetParent copy() {
        return copyFieldValuesTo(new GetParent(type, param));
    }

    @Override
    protected StepExecOrder execute(ScrapingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            DomNode node = ctx.getNode();
            Supplier<List<DomNode>> nodesSearch = () ->
                    switch (type) {
                        case PARENT -> Stream.of(node.getParentNode()).toList();
                        case NTH_PARENT -> HtmlUnitUtils.findNthParent(node, param).stream().toList();
                    };
            getHelper().execute(ctx, nodesSearch, stepExecOrder, getExecuteIf());
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    public enum Type {
        PARENT,
        NTH_PARENT,
    }

    // TODO see comment above ... about filters ...
//    @Override
//    public GetParent addFilter(Filter filter) {
//        return super.addFilter(filter);
//    }
}
