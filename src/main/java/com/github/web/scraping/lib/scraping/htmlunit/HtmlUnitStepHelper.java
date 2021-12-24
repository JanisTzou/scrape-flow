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
import com.github.web.scraping.lib.parallelism.ParsedDataListener;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import com.github.web.scraping.lib.scraping.ScrapingServices;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Log4j2
public class HtmlUnitStepHelper {

    private final List<HtmlUnitScrapingStep<?>> nextSteps;
    private final CollectorSetups collectorSetups;
    private final ScrapingServices services;

    // just for debugging
    @Getter
    private String stepName;

    /**
     * @param stepName the delegating step name for debugging purposes
     */
    public HtmlUnitStepHelper(@Nullable List<HtmlUnitScrapingStep<?>> nextSteps,
                              String stepName,
                              ScrapingServices services,
                              CollectorSetups collectorSetups) {
        this.nextSteps = Objects.requireNonNullElse(nextSteps, new ArrayList<>());
        this.services = services;
        this.collectorSetups = collectorSetups;
        setStepName(stepName);
    }

    public void execute(ParsingContext ctx, Supplier<List<DomNode>> nodesSearch, StepExecOrder currStepExecOrder) {
        try {
            final List<DomNode> foundNodes = nodesSearch.get();
            log.debug("{} - {}: found {} nodes", currStepExecOrder, getStepName(), foundNodes.size());

            foundNodes.forEach(node -> {

                ContextModels nextContextModels = ctx.getContextModels().copy();

                List<ModelToPublish> modelToPublishList = new ArrayList<>();

                // generate models
                collectorSetups.getModelSuppliers().stream()
                        .forEach(co -> {
                            Object model = co.getModelSupplier().get();
                            Class<?> modelClass = co.getModelClass();
                            ParsedDataListener<Object> parsedDataListener = co.getParsedDataListener();
                            if (parsedDataListener != null) {
                                modelToPublishList.add(new ModelToPublish(model, modelClass, parsedDataListener));
                            }
                            nextContextModels.push(model, modelClass);
                        });

                // populate containers ...
                collectorSetups.getAccumulators().stream()
                        // TODO only custom clases allowed here ...
                        .forEach(op -> {
                            BiConsumer<Object, Object> accumulator = op.getAccumulator();

                            Class<?> containerClass = op.getContainerClass();
                            Class<?> modelClass = op.getModelClass();

                            // TODO if we have multiple collections will this not get mixed up ?
                            Optional<ModelWrapper> container = nextContextModels.getModelFor(containerClass);
                            Optional<ModelWrapper> accumulatedModel = nextContextModels.getModelFor(modelClass);

                            if (container.isPresent() && accumulatedModel.isPresent()) {
                                accumulator.accept(container.get().getModel(), accumulatedModel.get().getModel());
                            } else {
                                log.warn("{} - {}: Failed to find modelWrappers for containerClass and/or modelClass!", currStepExecOrder, stepName);
                            }

                        });


                List<StepExecOrder> generatedSteps = executeNextSteps(currStepExecOrder, node, ctx, nextContextModels);

                // TODO this step is what is missing when we call HtmlUnitSiteParser or NavigateToPage step ... from another step ... if it has a collector set to it ...
                if (!modelToPublishList.isEmpty()) { // important
                    services.getStepAndDataRelationshipTracker().track(currStepExecOrder, generatedSteps, modelToPublishList);
                    services.getNotificationService().track(generatedSteps);
                }
            });

        } catch (Exception e) {
            log.error("{} - {}: Error executing step", currStepExecOrder, getStepName(), e);
        }
    }


    private <M, T> List<StepExecOrder> executeNextSteps(StepExecOrder currStepExecOrder, DomNode node, ParsingContext ctx, ContextModels nextContextModels) {
        return nextSteps.stream()
                .map(step -> {
                    ParsingContext nextCtx = new ParsingContext(
                            currStepExecOrder,
                            node,
                            nextContextModels.copy(),
                            null, // TODO send parsed text as well? Probably not, the parsed text should be possible to access differently ... (through model)
                            ctx.getParsedURL(),
                            ctx.getRecursiveRootStepExecOrder()
                    );
                    return step.execute(nextCtx);
                })
                .collect(Collectors.toList());
    }


    private void setStepName(String stepName) {
        this.stepName = stepName != null ? stepName + "-wrapper" : null;
    }


}
