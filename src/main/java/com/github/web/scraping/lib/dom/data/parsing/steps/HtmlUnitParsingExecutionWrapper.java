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
import com.github.web.scraping.lib.dom.data.parsing.*;
import com.github.web.scraping.lib.parallelism.ParsedDataListener;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Log4j2
public class HtmlUnitParsingExecutionWrapper<ModelT, ContainerT> {

    private final List<HtmlUnitParsingStep<?>> nextSteps;
    private final Collecting<ModelT, ContainerT> collecting;
    private final CrawlingServices services;
    // just for debugging
    @Getter
    private String stepName;

    /**
     * @param stepName the delegating step name for debugging purposes
     * @param services
     */
    public HtmlUnitParsingExecutionWrapper(@Nullable List<HtmlUnitParsingStep<?>> nextSteps, @Nullable Collecting<ModelT, ContainerT> collecting, String stepName, CrawlingServices services) {
        this.nextSteps = Objects.requireNonNullElse(nextSteps, new ArrayList<>());
        this.collecting = Objects.requireNonNullElse(collecting, new Collecting<>());
        this.services = services;
        setStepName(stepName);
    }

    public HtmlUnitParsingExecutionWrapper(List<HtmlUnitParsingStep<?>> nextSteps, String stepName, CrawlingServices services) {
        this(nextSteps, null, stepName, services);
    }

    public <M, T> List<StepResult> execute(ParsingContext<ModelT, ContainerT> ctx, Supplier<List<DomNode>> nodesSearch, StepExecOrder currStepExecOrder, ExecutionMode mode) {
        try {
            final List<DomNode> foundNodes = nodesSearch.get();

            final List<StepResult> nextStepResults = foundNodes
                    .stream()
                    .flatMap(node -> {
                        NextParsingContextBasis<M, T> nextContextBasis = getNextContextBasis(ctx);
                        List<StepExecOrder> generatedSteps = new ArrayList<>(); // the generated model was propagated to these steps
                        List<StepResult> stepResults = executeNextSteps(currStepExecOrder, node, nextContextBasis, generatedSteps, mode);

                        // TODO refactor ..
                        if (mode.equals(ExecutionMode.ASYNC)) {
                            if (nextContextBasis.model != null && nextContextBasis.modelSupplied) {
                                M model = nextContextBasis.model.getModel();

                                BiConsumer<ContainerT, ModelT> accumulator = collecting.getAccumulator();

                                final Optional<StepContainer<ContainerT>> stepContainer = getStepContainer(ctx);
                                final ContainerT container = stepContainer.map(sc -> sc.container).orElse(null);

                                if (container != null) {
                                    if (accumulator != null) {
                                        try {
                                            // if collectors are incorrectly set up, here is where we get exps like this: java.lang.ClassCastException: class com.github.web.scraping.lib.demos.TeleskopExpressDeCrawler$Product cannot be cast to class com.github.web.scraping.lib.demos.TeleskopExpressDeCrawler$Products
                                            accumulator.accept(container, (ModelT) model);
                                        } catch (ClassCastException e) {
                                            throwIncorrectDataCollectionSetupEx(e);
                                        }
                                    } else {
                                        log.error("{}: Accumulator is null - cannot collect parsed data", getStepName());
                                    }
                                }

                                // TODO this step is what is missing when we call HtmlUnitSiteParser or NavigateToPage step ... from another step ... if it has a collector set to it ...
                                services.getStepAndDataRelationshipTracker().track(currStepExecOrder, generatedSteps, model, (ParsedDataListener<Object>) collecting.getDataListener());
                            }
                        }
                        return stepResults.stream();
                    })
                    .collect(Collectors.toList());

            return collectStepResults(ctx, nextStepResults);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    private <M, T> List<StepResult> executeNextSteps(StepExecOrder currStepExecOrder, DomNode node, NextParsingContextBasis<M, T> nextContextBasis, List<StepExecOrder> executedSteps, ExecutionMode mode) {
        return nextSteps.stream()
                .flatMap(step -> {
                    ParsingContext<M, T> nextCtx = new ParsingContext<>(
                            currStepExecOrder,
                            node,
                            nextContextBasis.model,
                            nextContextBasis.container,
                            null, // TODO send parsed text as well? Probably not, the parsed text should be possible to access differently ... (through model)
                            nextContextBasis.parsedURL
                    );
                    List<StepResult> stepResults = step.execute(nextCtx, mode, executedSteps::add);
                    return stepResults.stream();
                })
                .collect(Collectors.toList());
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
                    log.error("{} no container provided", stepName); // TODO is this error or not ?
                }
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <ModelT2, ContainerT2> NextParsingContextBasis<ModelT2, ContainerT2> getNextContextBasis(ParsingContext<ModelT, ContainerT> ctx) {
        Optional<ModelProxy<?>> suppliedModelProxy = collecting.supplyModel().map(ModelProxy::new);
        boolean suppliedModel;
        ModelProxy<ModelT2> nextModelProxy;
        ContainerT2 nextContainer;

        if (suppliedModelProxy.isPresent()) {
            suppliedModel = true;
            nextModelProxy = (ModelProxy<ModelT2>) suppliedModelProxy.get();
            nextContainer = (ContainerT2) suppliedModelProxy.get().getModel(); // previous suppliedModelProxy must be the next container ...
            log.trace("{}: next model is supplied", getStepName());
        } else {
            suppliedModel = false;
            ModelProxy<?> ctxModelProxy = ctx.getModelProxy();
            nextModelProxy = (ModelProxy<ModelT2>) ctxModelProxy; // needs to be propagated
            nextContainer = ctxModelProxy != null ? (ContainerT2) ctxModelProxy.getModel() : null;
            log.trace("{}: next model is not supplied", getStepName());
        }

        return new NextParsingContextBasis<>(nextModelProxy, suppliedModel, nextContainer, ctx.getParsedURL());
    }

    // TODO execute this onlt in the SYNC mode ... probably ...
    private List<StepResult> collectStepResults(ParsingContext<ModelT, ContainerT> ctx, List<StepResult> stepResults) {
        final Optional<StepContainer<ContainerT>> stepContainer = getStepContainer(ctx);
        final ContainerT container = stepContainer.map(sc -> sc.container).orElse(null);

        if (container != null) {
            stepResults.stream()
                    .filter(sr -> sr instanceof ParsedElement)
                    .map(sr -> (ParsedElement) sr)
                    .map(ParsedElement::getModelProxy)
                    .filter(Objects::nonNull)
                    .forEach(mp -> {
                        // the proxy prevents duplicates to be accumulated as data is returning upstream
                        @SuppressWarnings("unchecked")
                        ModelProxy<ModelT> modelProxy = (ModelProxy<ModelT>) mp;
                        if (!mp.isAccumulated()) {

                            BiConsumer<ContainerT, ModelT> accumulator = collecting.getAccumulator();
                            if (accumulator != null) {
                                try {
                                    // if collectors are incorrectly set up, here is where we get exps like this: java.lang.ClassCastException: class com.github.web.scraping.lib.demos.TeleskopExpressDeCrawler$Product cannot be cast to class com.github.web.scraping.lib.demos.TeleskopExpressDeCrawler$Products
                                    accumulator.accept(container, modelProxy.getModel());
                                    modelProxy.setAccumulated(true);
                                } catch (ClassCastException e) {
                                    throwIncorrectDataCollectionSetupEx(e);
                                }
                            } else {
                                log.error("{}: Accumulator is null - cannot collect parsed data", getStepName());
                            }

                        } else {
                            log.trace("{}: skipping item - already accumulated", getStepName());
                        }
                    });

            // TODO hmm what to return here? Or better ... what to wrap this in?
            return List.of(new ParsedElements(container));
        } else {
            BiConsumer<?, ?> accumulator = collecting.getAccumulator();
            if (accumulator != null) {
                log.error("{}: No container available while there is an accumulator set. Error in data collection setting.", getStepName());
            }
            return stepResults;
        }
    }

    private void throwIncorrectDataCollectionSetupEx(ClassCastException e) {
        // TODO improve this further by parsing the class names from the original exception to provide a detailed message ...
        throw new IncorrectDataCollectionSetupException("Error while setting parsed data to provided models in step " +
                "'" + getStepName() + "'. The setup of data collection into models must be fixed.", e);
    }

    private void setStepName(String stepName) {
        this.stepName = stepName != null ? stepName + "-wrapper" : null;
    }

    // TODO remove .... probably not needed ... this wrapping ...
    private record StepContainer<R>(R container, boolean isNewInstance) {
    }

    @Getter
    @Setter
    @ToString
    @RequiredArgsConstructor
    private static class NextParsingContextBasis<ModelT, ContainerT> {

        @Nullable
        private final ModelProxy<ModelT> model;

        /**
         * If the model has been just instantiated
         */
        private final boolean modelSupplied;

        @Nullable
        private final ContainerT container;

        // if we are getting a URL from previous scraping we wanna preserve it in the context
        @Nullable
        private final String parsedURL;

    }


}
