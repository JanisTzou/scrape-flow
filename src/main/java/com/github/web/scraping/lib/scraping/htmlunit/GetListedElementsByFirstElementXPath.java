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
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Log4j2
public class GetListedElementsByFirstElementXPath extends GetElementsStepBase<GetListedElementsByFirstElementXPath> {

    private final String xPath;

    protected GetListedElementsByFirstElementXPath(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps, String xPath) {
        super(nextSteps);
        this.xPath = xPath;
    }

    @Deprecated
    public static GetListedElementsByFirstElementXPath instance(String xPath) {
        return new GetListedElementsByFirstElementXPath(null, xPath);
    }

    @Override
    public StepExecOrder execute(ParsingContext ctx) {
        StepExecOrder stepExecOrder = genNextOrderAfter(ctx.getPrevStepExecOrder());

        Runnable runnable = () -> {

            // here we want to identify all the elements that will then be processed by the next steps??
            // ... so we can for example apply specific HtmlUnitParsingStrategyByFullXPath on each one of them ... BUT the expaths will need to be dynamic as the root will change for each listed item ....

            Supplier<List<DomNode>> nodesSearch = () -> {
                // TODO improve working with XPath ...
                String parentXPath = XPathUtils.getXPathSubstrHead(xPath, 1);
                String xPathTail = XPathUtils.getXPathSubstrTail(xPath, 1).replaceAll("\\d+", "\\\\d+");
                String pattern = XPathUtils.regexEscape(XPathUtils.concat(parentXPath, xPathTail));

                List<Object> nodes = ctx.getNode().getByXPath(parentXPath);
                log.debug("{} - {} Found {} nodes in {}", stepExecOrder, getName(), nodes.size(), ctx.getNode());
                List<DomNode> domElements = nodes
                        .stream()
                        .flatMap(el -> {
                            // child elements ...
                            if (el instanceof HtmlElement htmlEl) {
                                return StreamSupport.stream(htmlEl.getChildElements().spliterator(), false);
                            }
                            return Stream.empty();
                        })
                        .filter(el -> {
                            if (el instanceof HtmlElement htmlEl) {
                                String xPath = htmlEl.getCanonicalXPath();
                                boolean matches = xPath.matches(pattern);
//                        logMatching(xPath, matches);
                                return matches;
                            }
                            return false;
                        })
                        .collect(Collectors.toList());

                return filterByTraverseOption(domElements);
            };

            HtmlUnitStepHelper helper = new HtmlUnitStepHelper(nextSteps, getName(), services, collectorSetups);
            helper.execute(ctx, nodesSearch, stepExecOrder);
        };

        handleExecution(stepExecOrder, runnable);

        return stepExecOrder;
    }

    private void logMatching(String xPath, boolean matches) {
        if (matches) {
            log.debug("{} Matched listing xPath = {}", getName(), xPath);
        } else {
            log.debug("{} Unmatched listing xPath = {}", getName(), xPath);
        }
    }

}
