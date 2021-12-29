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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.web.scraping.lib.parallelism.ParsedDataListener;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Log4j2
public class HtmlUnitStepHelper {

    private final HtmlUnitScrapingStep<?> step;


    public HtmlUnitStepHelper(HtmlUnitScrapingStep<?> step) {
        this.step = step;
    }

    public void execute(ScrapingContext ctx,
                        Supplier<List<DomNode>> nodesSearch,
                        StepExecOrder currStepExecOrder,
                        HtmlUnitScrapingStep.ExecutionCondition executeIf) {
        try {
            if (!canExecute(ctx, executeIf)) {
                return;
            }

            final List<DomNode> foundNodes = nodesSearch.get();
            final List<DomNode> filteredNodes = filter(foundNodes);

            logFoundCount(currStepExecOrder, filteredNodes.size());

            for (DomNode node : filteredNodes) {

                logNodeSourceCode(node);

                ContextModels nextContextModels = ctx.getContextModels().copy();
                List<ModelToPublish> modelToPublishList = new ArrayList<>();

                createAndAccumulateModels(currStepExecOrder, nextContextModels, modelToPublishList);


                List<StepExecOrder> generatedSteps = executeNextSteps(currStepExecOrder, node, ctx, nextContextModels);

                // TODO this step is what is missing when we call HtmlUnitSiteParser or NavigateToPage step ... from another step ... if it has a collector set to it ...
                //  decide which category of steps absolutely must use this and make it somehow nicely available ...
                if (!modelToPublishList.isEmpty()) { // important
                    step.getServices().getStepAndDataRelationshipTracker().track(currStepExecOrder, generatedSteps, modelToPublishList);
                    step.getServices().getNotificationService().track(generatedSteps);
                }
            }

        } catch (Exception e) {
            log.error("{} - {}: Error executing step", currStepExecOrder, step.getName(), e);
        }
    }

    private void createAndAccumulateModels(StepExecOrder currStepExecOrder, ContextModels nextContextModels, List<ModelToPublish> modelToPublishList) {
        // generate models
        for (Collector co : step.getCollectors().getModelSuppliers()) {
            Object model = co.getModelSupplier().get();
            Class<?> modelClass = co.getModelClass();
            ParsedDataListener<Object> parsedDataListener = co.getParsedDataListener();
            if (parsedDataListener != null) {
                modelToPublishList.add(new ModelToPublish(model, modelClass, parsedDataListener));
            }
            nextContextModels.push(model, modelClass);
        }

        // populate containers with generated models ...
        for (Collector op : step.getCollectors().getAccumulators()) {
            BiConsumer<Object, Object> accumulator = op.getAccumulator();

            Class<?> containerClass = op.getContainerClass();
            Class<?> modelClass = op.getModelClass();

            Optional<ModelWrapper> container = nextContextModels.getModelFor(containerClass);
            Optional<ModelWrapper> accumulatedModel = nextContextModels.getModelFor(modelClass);

            if (container.isPresent() && accumulatedModel.isPresent()) {
                accumulator.accept(container.get().getModel(), accumulatedModel.get().getModel());
            } else if (container.isPresent()) {
                if (step instanceof HtmlUnitStepCollectingParsedValueToModel) {
                    // has its own handling ...
                } else {
                    log.warn("{} - {}: Failed to find modelWrappers for containerClass and/or modelClass!", currStepExecOrder, step.getName());
                }
            }
        }
    }


    private void logFoundCount(StepExecOrder currStepExecOrder, int count) {
        if ((step.services.getGlobalDebugging().isLogFoundElementsCount() || step.stepDebugging.isLogFoundElementsCount())
        ) {
            log.info("{} - {}: found {} nodes", currStepExecOrder, step.getName(), count);
        }
    }


    private void logNodeSourceCode(DomNode node) {
        if (!(node instanceof Page)
                && (step.services.getGlobalDebugging().isLogFoundElementsSource() || step.stepDebugging.isLogFoundElementsSource())
        ) {
            log.info("Source for step {} defined at line {} \n{}", step.getName(), step.getStepDeclarationLine(), node.asXml());
        }
    }

    private boolean canExecute(ScrapingContext ctx, HtmlUnitScrapingStep.ExecutionCondition executeIf) {
        try {
            if (executeIf != null) {
                Optional<ModelWrapper> model = ctx.getContextModels().getModelFor(executeIf.getModelType());
                if (model.isPresent()) {
                    log.trace("{}: Found model and will execute condition", step.getName());
                    boolean canExecute = executeIf.getPredicate().test(model.get().getModel());
                    if (canExecute) {
                        return true;
                    }
                } else {
                    log.error("No model is set up for parsed value in step {}! Cannot execute step conditionally based on it!", step.getName());
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Error evaluating execution condition for step: {} - step will not run", step.getName(), e);
            return false;
        }
    }


    private List<StepExecOrder> executeNextSteps(StepExecOrder currStepExecOrder, DomNode node, ScrapingContext ctx, ContextModels nextContextModels) {
        return step.getNextSteps().stream()
                .map(step -> {
                    ScrapingContext nextCtx = new ScrapingContext(
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

    private List<DomNode> filter(List<DomNode> nodesToFilter) {
        List<DomNode> nodes = applyFilters(step.filters, nodesToFilter);
        if (step.getServices().getGlobalDebugging().isOnlyScrapeFirstElements()) {
            return nodes.stream().findFirst().stream().toList();
        } else {
            return nodes;
        }
    }

    private List<DomNode> applyFilters(List<Filter> filters, List<DomNode> nodes) {
        if (filters.isEmpty()) {
            return nodes;
        }
        List<DomNode> filtered = nodes;
        for (Filter filter : filters) {
            filtered = filter.filter(filtered);
        }
        return filtered;
    }


}
