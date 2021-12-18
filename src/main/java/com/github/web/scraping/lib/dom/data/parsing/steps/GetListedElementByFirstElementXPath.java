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
import com.github.web.scraping.lib.dom.data.parsing.XPathUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GetListedElementByFirstElementXPath extends CommonOperationsStepBase<GetListedElementByFirstElementXPath> {

    // the xPath of the first child
    private final String xPath;
    private Collecting<?, ?> collecting;

    protected GetListedElementByFirstElementXPath(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, String xPath) {
        super(nextSteps);
        this.xPath = xPath;
    }

/*
    example:
    // /html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]
    // /html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[2]
    // /html/body/div[1]/div/div[2]/div[2]/div/div[5]/div/div[1]/div[1]/table/tbody/tr[1]/td[1]/div/div[1]/span[1]
     */

    public static GetListedElementByFirstElementXPath instance(String xPath) {
        return new GetListedElementByFirstElementXPath(null, xPath);
    }

    @Override
    public <ModelT, ContainerT> List<StepResult> execute(ParsingContext<ModelT, ContainerT> ctx) {
        logExecutionStart();
        Supplier<List<DomNode>> nodesSearch = () -> {
            // figure out the diff between this.xPath and the parent element xPath ... then use that
            String parentXPath = ctx.getNode().getCanonicalXPath();
            String parentBaseXPath = XPathUtils.getXPathSubstrHead(parentXPath, 1);
            // the part of the child's xpath that will be the same through all the parents
            Optional<String> xPathDiff = XPathUtils.getXPathDiff(parentBaseXPath, xPath);
            if (xPathDiff.isEmpty()) {
                return Collections.emptyList();
            }
            String childStaticPartXPath = XPathUtils.getXPathSubstrTailFromStart(xPathDiff.get(), 1);
            String childXPath = XPathUtils.concat(parentXPath, childStaticPartXPath);

            return ctx.getNode().getByXPath(childXPath).stream()
                    .filter(o -> o instanceof DomNode)
                    .map(o -> (DomNode) o)
                    .collect(Collectors.toList());
        };

        @SuppressWarnings("unchecked")
        HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> wrapper = new HtmlUnitParsingExecutionWrapper<>(nextSteps, (Collecting<ModelT, ContainerT>) collecting, getName());
        return wrapper.execute(ctx, nodesSearch);
    }

}
