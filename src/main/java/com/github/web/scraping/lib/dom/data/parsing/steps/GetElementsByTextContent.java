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
import com.gargoylesoftware.htmlunit.html.DomText;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Log4j2
public class GetElementsByTextContent extends CommonOperationsStepBase<GetElementsByTextContent> {

    private final String stringToSearch;

    private boolean matchWholeTextContent = true;


    GetElementsByTextContent(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, String stringToSearch) {
        super(nextSteps);
        Objects.requireNonNull(stringToSearch);
        this.stringToSearch = stringToSearch;
    }

    public GetElementsByTextContent(String stringToSearch) {
        this(null, stringToSearch);
    }


    @Override
    public <ModelT, ContainerT> StepExecOrder execute(ParsingContext<ModelT, ContainerT> ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = () -> {
                for (DomNode domNode : ctx.getNode().getDescendants()) {
                    if (domNode instanceof DomText textNode) {
                        boolean found;
                        if (matchWholeTextContent) {
                            found = textNode.getTextContent().trim().equalsIgnoreCase(stringToSearch);
                        } else {
                            found = textNode.getTextContent().trim().contains(stringToSearch);
                        }
                        if (found) {
                            log.debug("Found element by textContent: {}", stringToSearch);
                            return Collections.singletonList(textNode.getParentNode());
                        }
                    }
                }
                return Collections.emptyList();
            };

            @SuppressWarnings("unchecked")
            HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> wrapper = new HtmlUnitParsingExecutionWrapper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName(), services);
            wrapper.execute(ctx, nodesSearch, stepExecOrder);
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    /**
     * set to true by default
     * @return
     */
    public GetElementsByTextContent setMatchWholeTextContent(boolean matchWholeTextContent) {
        this.matchWholeTextContent = matchWholeTextContent;
        return this;
    }
}
