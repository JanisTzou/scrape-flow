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

    @Override
    public C next(ScrapingStep<?> nextStep) {
        return addNextStep(getNextStepCopy(nextStep));
    }

    @Override
    public C nextExclusively(ScrapingStep<?> nextStep) {
        ScrapingStep<?> nextCopy = getNextExclusivelyStepCopy(nextStep);
        return addNextStep(nextCopy);
    }

    @Override
    public <T> C nextIf(Predicate<T> modelDataCondition, Class<T> modelType, ScrapingStep<?> nextStep) {
        ScrapingStep<?> nextCopy = getNextIfStepCopy(modelDataCondition, modelType, nextStep);
        return addNextStep(nextCopy);
    }

    @Override
    public <T> C nextIfExclusively(Predicate<T> modelDataCondition, Class<T> modelType, ScrapingStep<?> nextStep) {
        ScrapingStep<?> nextCopy = getNextIfExclusivelyStepCopy(modelDataCondition, modelType, nextStep);
        return addNextStep(nextCopy);
    }

    protected ScrapingStep<?> getNextStepCopy(ScrapingStep<?> nextStep) {
        return nextStep.copy();
    }

    protected ScrapingStep<?> getNextExclusivelyStepCopy(ScrapingStep<?> nextStep) {
        return nextStep.copy()
                .setExclusiveExecution(true);
    }

    protected <T> ScrapingStep<?> getNextIfStepCopy(Predicate<T> modelDataCondition, Class<T> modelType, ScrapingStep<?> nextStep) {
        return nextStep.copy()
                .setExecuteIf(new ExecuteStepByModelDataCondition(modelDataCondition, modelType));
    }

    protected <T> ScrapingStep<?> getNextIfExclusivelyStepCopy(Predicate<T> modelDataCondition, Class<T> modelType, ScrapingStep<?> nextStep) {
        return nextStep.copy()
                .setExecuteIf(new ExecuteStepByModelDataCondition(modelDataCondition, modelType))
                .setExclusiveExecution(true);
    }

    // TODO think about these ...
//    public C expectFindingNone();
//    public C expectFindingOne();
//    public C expectFindingOneAtLeast();
//    public C expectFindingOneAtMost();
//    public C expectFindingMany();

}
