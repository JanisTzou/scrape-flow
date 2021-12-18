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
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HtmlUnitParsingExecutionWrapper<R, T> {

    // just for debugging
    @Getter
    private String name;

    private final List<HtmlUnitParsingStep<?>> nextSteps;
    private final Collecting<R, T> collecting;

    /**
     * @param name for debugging purposes
     */
    public HtmlUnitParsingExecutionWrapper(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, @Nullable Collecting<R, T> collecting, String name) {
        this.nextSteps = Objects.requireNonNullElse(nextSteps, new ArrayList<>());
        this.collecting = Objects.requireNonNullElse(collecting, new Collecting<>());
        setName(name);
    }

    public HtmlUnitParsingExecutionWrapper(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, @Nullable Collecting<R, T> collecting) {
        this(nextSteps, collecting, null);
    }

    public HtmlUnitParsingExecutionWrapper(List<HtmlUnitParsingStep<?>> nextSteps) {
        this(nextSteps, null);
    }

    public List<StepResult> execute(ParsingContext ctx, Supplier<List<DomNode>> nodesSearch) {
        try {
            final List<DomNode> foundNodes = nodesSearch.get();

            final List<StepResult> nextStepResults = foundNodes
                    .stream()
                    .flatMap(node -> {
                        NextParsingContextBasis<?> nextContextBasis = getNextContextBasis(ctx);
                        return executeNextSteps(node, nextContextBasis);
                    })
                    .collect(Collectors.toList());

            // TODO hmm maybe we do not get the right stepContainer here ? The decision making logic below might need to determine this stepContainer ...
            //  or maybe not?
            // TODO maybe these two steps can go into collectStepResults() ?
            final Optional<StepContainer<R>> stepContainer = getStepContainer(ctx);
            final R container = stepContainer.map(sc -> sc.container).orElse(null);

            // TODO for some reason it seems that we are not getting container where we should ...
            return collectStepResults(container, nextStepResults);

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

    // contained for this step
    private Optional<StepContainer<R>> getStepContainer(ParsingContext ctx) {
        if (collecting.getAccumulator() != null) { // we will be collecting stuff only if there is an accumulator as well ...
            Optional<R> container = collecting.supplyContainer();
            if (container.isPresent()) {
                return Optional.of(new StepContainer<>(container.get(), true));
            } else {
                R ctxContainer = (R) ctx.getContainer();
                if (ctxContainer != null) {
                    return Optional.of(new StepContainer<>(ctxContainer, false));
                } else {
                    System.out.println("Context container null ... in " + getName());
                }
            }
        }
        return Optional.empty();
    }

    // TODO remove .... probably not needed ... this wrapping ...
    private record StepContainer<R>(R container, boolean isNewInstance) {
    }

    private NextParsingContextBasis getNextContextBasis(ParsingContext ctx) {
        Optional<ModelProxy<T>> suppliedModelProxy = collecting.supplyModel().map(ModelProxy::new);
        ModelProxy<T> nextModelProxy;
        R nextContainer;
//
//        if (ctxContainer != null) {
//            nextContainer = (R) ctxContainer;
//            if (suppliedModelProxy.isPresent()) {
//                nextModelProxy = suppliedModelProxy.get();
//                System.out.println("here ... 0");
//            } else {
//                nextModelProxy = (AccumulatedModelProxy<T>) ctx.getModelProxy();
//                System.out.println("here ... 1");
//            }
//        } else {
//            if (suppliedModelProxy.isPresent()) {
//                nextModelProxy = suppliedModelProxy.get();
//                nextContainer = (R) ctx.getModelProxy().getModel(); // previous suppliedModelProxy must be the current container ...
//                System.out.println("here ... 2");
//            } else {
//                nextModelProxy = (AccumulatedModelProxy<T>) ctx.getModelProxy(); // needs to be propagated
//                nextContainer = null; // must not be propagated ...
//                System.out.println("here ... 3");
//            }
//        }

        if (suppliedModelProxy.isPresent()) {
            nextModelProxy = suppliedModelProxy.get();
            nextContainer = (R) suppliedModelProxy.get().getModel(); // previous suppliedModelProxy must be the current container ...
            System.out.println("here ... 4");
        } else {
            ModelProxy<T> ctxModelProxy = (ModelProxy<T>) ctx.getModelProxy();
            nextModelProxy = ctxModelProxy; // needs to be propagated
            nextContainer = ctxModelProxy != null ? (R) ctxModelProxy.getModel() : null; // must not be propagated ...
            System.out.println("here ... 5");
        }

        return new NextParsingContextBasis<>(nextModelProxy, nextContainer);
    }

    private List<StepResult> collectStepResults(R container, List<StepResult> stepResults) {
        if (container != null) { // TODO maybe check for accumulator here ...
            final List<ParsedElement> hrefs = stepResults.stream().filter(sr -> sr instanceof ParsedElement pe && pe.isHasHRef()).map(sr -> (ParsedElement) sr).collect(Collectors.toList());

            // TODO handle duplicates ...
            stepResults.stream()
                    .filter(sr -> sr instanceof ParsedElement)
                    .map(sr -> (ParsedElement) sr)
                    .map(ParsedElement::getModelProxy)
                    .filter(Objects::nonNull)
                    .forEach(mp -> {
                        // the proxy prevents duplicates to be accumulated as data is returning upstream
                        ModelProxy<T> modelProxy = (ModelProxy<T>) mp;
//                        System.out.println(modelProxy);
                        if (!modelProxy.isAccumulated()) {
                            BiConsumer<R, T> accumulator = collecting.getAccumulator();
                            if (accumulator != null) {
                                accumulator.accept(container, modelProxy.getModel());
                                modelProxy.setAccumulated(true);
                                System.out.println("Accumulator exists ...");
                            } else {
                                System.out.println("Accumulator is null ...");
                            }
                        } else {
                            System.out.println("Skipping ... already accumulated");
                        }
                    });

            return List.of(new ParsedElements(container, hrefs));
        } else {
            BiConsumer<R, T> accumulator = collecting.getAccumulator();
            if (accumulator != null) {
                System.out.println("Wrong existing accumulator ... in " + getName());
            }
            return stepResults;
        }
    }

    private HtmlUnitParsingExecutionWrapper<R, T> setName(String name) {
        this.name = name != null ? name + "-wrapper" : null;
        return this;
    }

    @Getter
    @Setter
    @ToString
    @RequiredArgsConstructor
    private static class NextParsingContextBasis<T> {

        @Nullable
        private final ModelProxy<T> model;

        @Nullable
        private final Object container;

    }


}
