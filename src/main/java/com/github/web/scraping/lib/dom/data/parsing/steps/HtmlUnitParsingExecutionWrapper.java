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
import com.github.web.scraping.lib.dom.data.parsing.ParsedElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsedElements;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.scraping.utils.HtmlUnitUtils;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HtmlUnitParsingExecutionWrapper<R, T> {

    private final List<HtmlUnitParsingStep> nextSteps;
    private final Collecting<R, T> collecting;

    public HtmlUnitParsingExecutionWrapper(List<HtmlUnitParsingStep> nextSteps, Collecting<R, T> collecting) {
        this.nextSteps = nextSteps;
        this.collecting = Objects.requireNonNullElse(collecting, new Collecting<>());
    }

    public List<StepResult> execute(ParsingContext ctx, Supplier<List<DomNode>> nodesSearch) {
//        final Set<Object> processed = new HashSet<>(); // temporary solution ... if class implements equals() & hashcode this is a problem ...
        final Optional<R> container = collecting.supplyContainer();

        final List<DomNode> foundNodes = nodesSearch.get();

        final List<StepResult> stepResults = foundNodes
                .stream()
                .flatMap(node -> executeNextSteps(ctx, node, container.orElse(null)))
                .collect(Collectors.toList());

        return getStepResults(container.orElse(null), stepResults);
    }

    private Stream<StepResult> executeNextSteps(ParsingContext ctx, DomNode node, R container) {
        T m = collecting.supplyModel().orElse((T) ctx.getModel());
        return nextSteps.stream().flatMap(s -> {
            ParsingContext nextCtx = new ParsingContext(node, m, container);
            return s.execute(nextCtx).stream();
        });
    }

    private List<StepResult> getStepResults(R container, List<StepResult> stepResults) {
        if (container != null) {
            final List<ParsedElement> hrefs = stepResults.stream().filter(sr -> sr instanceof ParsedElement pe && pe.isHasHRef()).map(sr -> (ParsedElement) sr).collect(Collectors.toList());

            // TODO handle duplicates ...
            stepResults.stream()
                    .filter(sr -> sr instanceof ParsedElement)
                    .map(sr -> (ParsedElement) sr)
                    .map(ParsedElement::getModel)
                    .forEach(model -> collecting.getAccumulator().accept(container, (T) model));

            return List.of(new ParsedElements(container, hrefs));
        } else {
            return stepResults;
        }
    }

}
