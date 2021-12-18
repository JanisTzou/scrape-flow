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

public class HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> {

    // just for debugging
    @Getter
    private String name;

    private final List<HtmlUnitParsingStep<?>> nextSteps;
    private final Collecting<ModelT, ContainerT> collecting;

    /**
     * @param name for debugging purposes
     */
    public HtmlUnitParsingExecutionWrapper(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, @Nullable Collecting<ModelT, ContainerT> collecting, String name) {
        this.nextSteps = Objects.requireNonNullElse(nextSteps, new ArrayList<>());
        this.collecting = Objects.requireNonNullElse(collecting, new Collecting<>());
        setName(name);
    }

    public HtmlUnitParsingExecutionWrapper(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, @Nullable Collecting<ModelT, ContainerT> collecting) {
        this(nextSteps, collecting, null);
    }

    public HtmlUnitParsingExecutionWrapper(List<HtmlUnitParsingStep<?>> nextSteps) {
        this(nextSteps, null);
    }

    public <M, T> List<StepResult> execute(ParsingContext<ModelT, ContainerT> ctx, Supplier<List<DomNode>> nodesSearch) {
        try {
            final List<DomNode> foundNodes = nodesSearch.get();

            final List<StepResult> nextStepResults = foundNodes
                    .stream()
                    .flatMap(node -> {
                        NextParsingContextBasis<M, T> nextContextBasis = getNextContextBasis(ctx);
                        return executeNextSteps(node, nextContextBasis);
                    })
                    .collect(Collectors.toList());

            return collectStepResults(ctx, nextStepResults);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }



    private <M, T> Stream<StepResult> executeNextSteps(DomNode node, NextParsingContextBasis<M, T> nextContextBasis) {
        return nextSteps.stream().flatMap(step -> {
            ParsingContext<M, T> nextCtx = new ParsingContext<>(node, nextContextBasis.model, nextContextBasis.container, false);
            return step.execute(nextCtx).stream();
        });
    }

    // contained for this step
    private Optional<StepContainer<ContainerT>> getStepContainer(ParsingContext<ModelT, ContainerT> ctx) {
        if (collecting.getAccumulator() != null) { // we will be collecting stuff only if there is an accumulator as well ...
            Optional<ContainerT> container = collecting.supplyContainer();
            if (container.isPresent()) {
                return Optional.of(new StepContainer<>(container.get(), true));
            } else {
                ContainerT ctxContainer = ctx.getContainer();
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

    @SuppressWarnings("unchecked")
    private <ModelT2, ContainerT2> NextParsingContextBasis<ModelT2, ContainerT2> getNextContextBasis(ParsingContext<ModelT, ContainerT> ctx) {
        Optional<ModelProxy<?>> suppliedModelProxy = collecting.supplyModel().map(ModelProxy::new);
        ModelProxy<ModelT2> nextModelProxy;
        ContainerT2 nextContainer;

        if (suppliedModelProxy.isPresent()) {
            nextModelProxy = (ModelProxy<ModelT2>) suppliedModelProxy.get();
            nextContainer = (ContainerT2) suppliedModelProxy.get().getModel(); // previous suppliedModelProxy must be the next container ...
            System.out.println("here ... 4");
        } else {
            ModelProxy<?> ctxModelProxy = ctx.getModelProxy();
            nextModelProxy = (ModelProxy<ModelT2>) ctxModelProxy; // needs to be propagated
            nextContainer = ctxModelProxy != null ? (ContainerT2) ctxModelProxy.getModel() : null;
            System.out.println("here ... 5");
        }

        return new NextParsingContextBasis<>(nextModelProxy, nextContainer);
    }

    private List<StepResult> collectStepResults(ParsingContext<ModelT, ContainerT> ctx, List<StepResult> stepResults) {
        final Optional<StepContainer<ContainerT>> stepContainer = getStepContainer(ctx);
        final ContainerT container = stepContainer.map(sc -> sc.container).orElse(null);

        if (container != null) {
            final List<ParsedElement> hrefs = stepResults.stream().filter(sr -> sr instanceof ParsedElement pe && pe.isHasHRef()).map(sr -> (ParsedElement) sr).collect(Collectors.toList());

            // TODO handle duplicates ...
            stepResults.stream()
                    .filter(sr -> sr instanceof ParsedElement)
                    .map(sr -> (ParsedElement) sr)
                    .map(ParsedElement::getModelProxy)
                    .filter(Objects::nonNull)
                    .forEach(mp -> {
                        // the proxy prevents duplicates to be accumulated as data is returning upstream
                        @SuppressWarnings("unchecked")
                        ModelProxy<ModelT> modelProxy = (ModelProxy<ModelT>) mp;
//                        System.out.println(modelProxy);
                        if (!mp.isAccumulated()) {
                            BiConsumer<ContainerT, ModelT> accumulator = collecting.getAccumulator();
                            if (accumulator != null) {
                                accumulator.accept(container, modelProxy.getModel()); // if collectors are incorrectly set up, here is where we get exps like this: java.lang.ClassCastException: class com.github.web.scraping.lib.demos.TeleskopExpressDeCrawler$Product cannot be cast to class com.github.web.scraping.lib.demos.TeleskopExpressDeCrawler$Products
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
            BiConsumer<?, ?> accumulator = collecting.getAccumulator();
            if (accumulator != null) {
                System.out.println("Wrong existing accumulator ... in " + getName());
            }
            return stepResults;
        }
    }

    private void setName(String name) {
        this.name = name != null ? name + "-wrapper" : null;
    }

    @Getter
    @Setter
    @ToString
    @RequiredArgsConstructor
    private static class NextParsingContextBasis<ModelT, ContainerT> {

        @Nullable
        private final ModelProxy<ModelT> model;

        @Nullable
        private final ContainerT container;

    }


}
