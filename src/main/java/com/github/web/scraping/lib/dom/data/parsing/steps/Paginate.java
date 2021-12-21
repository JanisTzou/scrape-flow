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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.web.scraping.lib.dom.data.parsing.ElementClicked;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Log4j2
public class Paginate extends CommonOperationsStepBase<Paginate> {

    private HtmlUnitParsingStep<?> paginationTrigger;

    public Paginate(@Nullable List<HtmlUnitParsingStep<?>> nextSteps) {
        super(nextSteps);
    }

    public Paginate() {
        this(null);
    }

    public static Paginate instance() {
        return new Paginate();
    }

    @Override
    public <ModelT, ContainerT> List<StepResult> execute(ParsingContext<ModelT, ContainerT> ctx, ExecutionMode mode, OnOrderGenerated onOrderGenerated) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder(), onOrderGenerated);

        Callable<List<StepResult>> callable = () -> {
            if (paginationTrigger == null) {
                throw new IllegalStateException("paginationTrigger must be set for pagination to work!");
            } else {
                StepsUtils.propagateServicesRecursively(paginationTrigger, services, new HashSet<>());
            }
            AtomicReference<HtmlPage> pageRef = new AtomicReference<>(ctx.getNode().getHtmlPageOrNull());
            List<StepResult> all = new ArrayList<>();
            while (true) {
                Supplier<List<DomNode>> nodesSearch = () -> List.of(pageRef.get());
                @SuppressWarnings("unchecked")
                HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> wrapper = new HtmlUnitParsingExecutionWrapper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName(), services);
                List<StepResult> scraping = wrapper.execute(ctx, nodesSearch, stepExecOrder, mode);
                all.addAll(scraping);
                if (paginationTrigger != null) {
                    // as long as we execute the pagination synchronously it is ok not no collect and handle the generated order ...
                    OnOrderGenerated order = so -> {
                    };
                    // TODO think about how to make this ASYNC ...
                    // ALWAYS SYNC! we need to wait for the next page to be loaded immediately ... one bad thing though is that this does not participate in throttling ...
                    List<StepResult> pagination = paginationTrigger.execute(new ParsingContext<>(stepExecOrder, pageRef.get()), ExecutionMode.SYNC, order);
                    Optional<HtmlPage> nextPage = pagination.stream().filter(sr -> sr instanceof ElementClicked).map(sr -> ((ElementClicked) sr).getPageAfterElementClicked()).findFirst();
                    if (nextPage.isPresent()) {
                        pageRef.set(nextPage.get());
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }

            return all;
        };

        return handleExecution(mode, stepExecOrder, callable);
    }

    /**
     * Steps that trigger the pagination - that is loading the next content.
     * In practice this is most often the action finding the "NEXT" button element and clicking it.
     */
    public Paginate setPaginationTrigger(HtmlUnitParsingStep<?> paginatingSequence) {
        this.paginationTrigger = paginatingSequence;
        return this;
    }

    /**
     * Performs the specified step sequence for content loaded in pagination
     * Same as calling {@link com.github.web.scraping.lib.dom.data.parsing.steps.CommonOperationsStepBase#then(HtmlUnitParsingStep)}
     */
    public Paginate thenForEachPage(HtmlUnitParsingStep<?> stepsForPaginatedContent) {
        then(stepsForPaginatedContent);
        return this;
    }

    // TODO before and after pagination started hooks? for preceding / following steps?

}
