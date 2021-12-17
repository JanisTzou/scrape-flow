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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HtmlUnitParsingExecutionWrapper<R, T> {

    private final List<HtmlUnitParsingStep> nextSteps;
    private final Collecting<R, T> collecting;

    public HtmlUnitParsingExecutionWrapper(@Nullable List<HtmlUnitParsingStep> nextSteps, @Nullable Collecting<R, T> collecting) {
        this.nextSteps = Objects.requireNonNullElse(nextSteps, new ArrayList<>());
        this.collecting = Objects.requireNonNullElse(collecting, new Collecting<>());
    }

    public HtmlUnitParsingExecutionWrapper(List<HtmlUnitParsingStep> nextSteps) {
        this(nextSteps, null);
    }

    public List<StepResult> execute(ParsingContext ctx, Supplier<List<DomNode>> nodesSearch) {
        try {
//        final Set<Object> processed = new HashSet<>(); // temporary solution ... if class implements equals() & hashcode this is a problem ...

            NextParsingContextBasis nextContextBasis = getNextContextBasis(ctx);

            final List<DomNode> foundNodes = nodesSearch.get();

            final List<StepResult> nextStepResults = foundNodes
                    .stream()
                    .flatMap(node -> executeNextSteps(node, nextContextBasis))
                    .collect(Collectors.toList());

            // TODO hmm maybe we do not get the right container here ? The decision making logic below might need to determine this container ...
            //  or maybe not?
            return collectStepResults((R) nextContextBasis.container, nextStepResults);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private Stream<StepResult> executeNextSteps(DomNode node, NextParsingContextBasis nextContextBasis) {
        return nextSteps.stream().flatMap(s -> {
            ParsingContext nextCtx = new ParsingContext(node, nextContextBasis.model, nextContextBasis.container, false);
            return s.execute(nextCtx).stream();
        });
    }


    private NextParsingContextBasis getNextContextBasis(ParsingContext ctx) {
        // TODO should we ignore ctx.getContainer  at this point?
        Optional<R> container = collecting.supplyContainer(); // TODO use cur container
        Optional<T> model = collecting.supplyModel();
        T nextModel;
        R nextContainer;

        if (container.isPresent()) {
            nextContainer = container.get();
            if (model.isPresent()) {
                nextModel = model.get();
                System.out.println("here ... 0");
            } else {
                nextModel = (T) ctx.getModel();
                System.out.println("here ... 1");
            }
        } else {
            if (model.isPresent()) {
                nextModel = model.get();
                nextContainer = (R) ctx.getModel(); // previous model must be the current container ...
                System.out.println("here ... 2");
            } else {
                nextModel = (T) ctx.getModel(); // needs to be propagated
                nextContainer = null; // must not be propagated ...
                System.out.println("here ... 3");
            }
        }

        return new NextParsingContextBasis(nextModel, nextContainer, false);
    }

    private List<StepResult> collectStepResults(R container, List<StepResult> stepResults) {
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

    @Getter
    @Setter
    @ToString
    @RequiredArgsConstructor
    public class NextParsingContextBasis {

        // Curr model?
        @Nullable
        private final Object model;

        @Nullable
        private final Object container;

        private final boolean collectorToParentModel;

    }


}
