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

package com.github.web.scraping.lib.dom.data.parsing.steps;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.parallelism.StepOrder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class GetHtmlBody extends CommonOperationsStepBase<GetHtmlBody> {

    public GetHtmlBody(@Nullable List<HtmlUnitParsingStep<?>> nextSteps) {
        super(nextSteps);
    }

    public GetHtmlBody() {
        this(null);
    }

    public static GetHtmlBody instance() {
        return new GetHtmlBody();
    }

    @Override
    public <ModelT, ContainerT> List<StepResult> execute(ParsingContext<ModelT, ContainerT> ctx, ExecutionMode mode) {
        /*
            new design:

            - wrap in a task that will have its step order based on the parents step order ... this step will need to know where it stands relative to other steps ...
                - ... the child step wil generate its ownn step number before execution of step logic starts (sync or async)
            - submit generated StepOrder to "tracking service" so we can query:
                - ... see written on the paper :-)
            - submit task to processing to the queue
            - if step generates models than the models can be published only when all steps below it are finished (tracking service will notify about this -> tracking service should generate events maybe?)
                - the user should be able to register listeners for data freely ... based on the type of models ... the listeners will be notified ...

            - somehow all step implementations will need a class called Services that will be handed over to them as they are instantiated by the then() method ... the only was to have access to global stuff in one place ...
         */


        StepOrder stepOrder = genNextOrderAfter(ctx.getPrevStepOrder());

        Callable<List<StepResult>> callable = () -> {
            logExecutionStart(stepOrder);
            Supplier<List<DomNode>> nodesSearch = () ->  ctx.getNode().getByXPath("/html/body");
            @SuppressWarnings("unchecked")
            HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> wrapper = new HtmlUnitParsingExecutionWrapper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName(), services);
            return wrapper.execute(ctx, nodesSearch, stepOrder, mode);
        };

        return handleExecution(mode, stepOrder, callable);
    }

}
