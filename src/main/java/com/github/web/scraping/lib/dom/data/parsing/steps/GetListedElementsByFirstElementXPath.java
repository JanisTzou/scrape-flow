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
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.dom.data.parsing.XPathUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GetListedElementsByFirstElementXPath extends HtmlUnitChainableStep<GetListedElementsByFirstElementXPath>
    implements HtmlUnitCollectorSetupStep<GetListedElementsByFirstElementXPath> {

    private final String xPath;
    private Collecting<?, ?> collecting;

    protected GetListedElementsByFirstElementXPath(@Nullable List<HtmlUnitParsingStep> nextSteps, String xPath) {
        super(nextSteps);
        this.xPath = xPath;
    }

    public static GetListedElementsByFirstElementXPath instance(String xPath) {
        return new GetListedElementsByFirstElementXPath(null, xPath);
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {

        // here we want to identify all the elements that will then be processed by the next steps??
        // ... so we can for example apply specific HtmlUnitParsingStrategyByFullXPath on each one of them ... BUT the expaths will need to be dynamic as the root will change for each listed item ....

        Supplier<List<DomNode>> nodesSearch = () -> {
            // TODO improve working with XPath ...
            String parentXPath = XPathUtils.getXPathSubstrHead(xPath, 1);
            String xPathTail = XPathUtils.getXPathSubstrTail(xPath, 1).replaceAll("\\d+", "\\\\d+");
            String pattern = XPathUtils.regexEscape(XPathUtils.concat(parentXPath, xPathTail));

            return ctx.getNode().getByXPath(parentXPath)
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
        };

        return new HtmlUnitParsingExecutionWrapper<>(nextSteps, collecting).execute(ctx, nodesSearch);
    }

    private void logMatching(String xPath, boolean matches) {
        if (matches) {
            System.out.println("Matched listing xPath = " + xPath);
        } else {
            System.out.println("Unmatched listing xPath = " + xPath);
        }
    }

    @Override
    public <R, T> GetListedElementsByFirstElementXPath collector(Supplier<T> modelSupplier, Supplier<R> containerSupplier, BiConsumer<R, T> accumulator) {
        this.collecting = new Collecting<>(modelSupplier, containerSupplier, accumulator);
        return this;
    }

    @Override
    public <R, T> GetListedElementsByFirstElementXPath collector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator) {
        this.collecting = new Collecting<>(modelSupplier, null, accumulator);
        return this;
    }


}
