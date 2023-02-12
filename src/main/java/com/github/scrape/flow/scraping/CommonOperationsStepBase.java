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

package com.github.scrape.flow.scraping;

import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.data.publishing.ScrapedDataListener;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.scrape.flow.data.collectors.Collector.AccumulatorType;


public abstract class CommonOperationsStepBase<C extends ScrapingStep<C>>
        extends ScrapingStep<C>
        implements ChainedStep<C>, CollectingStep<C> {

    protected CommonOperationsStepBase() {
        this(null);
    }

    protected CommonOperationsStepBase(List<ScrapingStep<?>> nextSteps) {
        super(nextSteps);
    }

    @Override
    public <R, T> C collectValue(BiConsumer<R, T> accumulator, Class<R> containerType, Class<T> modelType) {
        return addCollector(new Collector(accumulator, modelType, containerType, AccumulatorType.ONE));
    }

    @Override
    public <R, T> C collectValues(BiConsumer<R, T> accumulator, Class<R> containerType, Class<T> modelType) {
        return addCollector(new Collector(accumulator, modelType, containerType, AccumulatorType.MANY));
    }

    @Override
    public <T> C addCollector(Supplier<T> modelSupplier, Class<T> modelType) {
        return addCollector(new Collector(modelSupplier, modelType));
    }

    @Override
    public <T> C addCollector(Supplier<T> modelSupplier, Class<T> modelType, ScrapedDataListener<T> scrapedDataListener) {
        return addCollector(new Collector(modelSupplier, modelType, scrapedDataListener));
    }

    public <S extends ScrapingStep<S>> S next(S nextStep) {
        addNextStepAndReturnThis(nextStep);
        if (this.getBranchRoot() == null) { // is root
            nextStep.setBranchRoot(this);
        } else {
            nextStep.setBranchRoot(this.getBranchRoot()); // hand over the branch root
        }
        return nextStep;
    }

    @Override
    public C nextBranch(ScrapingStep<?> nextStep) {
        ScrapingStep<?> branchRoot = getBranchRoot(nextStep);
        return addNextStepAndReturnThis(branchRoot);
    }

    @Override
    public C nextBranchExclusively(ScrapingStep<?> nextStep) {
        Steps.add(nextStep.stepNumber, this.stepNumber);
        ScrapingStep<?> branchRoot = getBranchRoot(nextStep);
        return addNextStepAndReturnThis(branchRoot.setExclusiveExecution(true));
    }

    @Override
    public <T> C nextBranchIf(Predicate<T> modelDataCondition, Class<T> modelType, ScrapingStep<?> nextStep) {
        nextStep.setExecuteIf(new ExecuteStepByModelDataCondition(modelDataCondition, modelType));
        ScrapingStep<?> branchRoot = getBranchRoot(nextStep);
        return addNextStepAndReturnThis(branchRoot);
    }

    @Override
    public <T> C nextBranchIfExclusively(Predicate<T> modelDataCondition, Class<T> modelType, ScrapingStep<?> nextStep) {
        nextStep.setExecuteIf(new ExecuteStepByModelDataCondition(modelDataCondition, modelType))
                .setExclusiveExecution(true);
        ScrapingStep<?> branchRoot = getBranchRoot(nextStep);
        return addNextStepAndReturnThis(branchRoot.setExclusiveExecution(true));
    }

    private ScrapingStep<?> getBranchRoot(ScrapingStep<?> nextStep) {
        return nextStep.getBranchRoot() == null ? nextStep : nextStep.getBranchRoot();
    }

    // TODO think about these ...
//    public C expectFindingNone();
//    public C expectFindingOne();
//    public C expectFindingOneAtLeast();
//    public C expectFindingOneAtMost();
//    public C expectFindingMany();

}
