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
import com.gargoylesoftware.htmlunit.html.DomText;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Log4j2
public class GetElementsByTextContent extends GetElementsStepBase<GetElementsByTextContent> {

    private final String searchString;
    private final boolean matchWholeTextContent;


    GetElementsByTextContent(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps, String searchString, boolean matchWholeTextContent) {
        super(nextSteps);
        Objects.requireNonNull(searchString);
        this.searchString = searchString;
        this.matchWholeTextContent = matchWholeTextContent;
    }

    GetElementsByTextContent(String searchString, boolean matchWholeTextContent) {
        this(null, searchString, matchWholeTextContent);
    }


    @Override
    public StepExecOrder execute(ParsingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        // TODO hmm .. this only returns one item ... we need to return all ... and apply traverse options filtering ...

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = () -> {
                for (DomNode domNode : ctx.getNode().getDescendants()) {
                    if (domNode instanceof DomText textNode) {
                        boolean found;
                        if (matchWholeTextContent) {
                            found = textNode.getTextContent().trim().equalsIgnoreCase(searchString);
                        } else {
                            found = textNode.getTextContent().trim().contains(searchString);
                        }
                        if (found) {
                            log.debug("Found element by textContent: {}", searchString);
                            return Collections.singletonList(textNode.getParentNode());
                        }
                    }
                }
                return Collections.emptyList();
            };

            HtmlUnitStepHelper helper = new HtmlUnitStepHelper(nextSteps, getName(), services, collectorSetups);
            helper.execute(ctx, nodesSearch, stepExecOrder);
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

}