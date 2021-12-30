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

package com.github.scrape.flow.scraping.htmlunit;

import com.github.scrape.flow.data.publishing.ScrapedDataListener;
import com.github.scrape.flow.data.collectors.Collector;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.scrape.flow.data.collectors.Collector.AccumulatorType;


public abstract class CommonOperationsStepBase<C extends HtmlUnitScrapingStep<C>> extends HtmlUnitScrapingStep<C>
        implements HtmlUnitStepSupportingNext<C>, HtmlUnitStepSupportingCollection<C> {

    public CommonOperationsStepBase(List<HtmlUnitScrapingStep<?>> nextSteps) {
        super(nextSteps);
    }

    @Override
    public <R, T> C collectOne(BiConsumer<R, T> accumulator, Class<R> containerType, Class<T> modelType) {
        return addCollector(new Collector(accumulator, modelType, containerType, AccumulatorType.ONE));
    }

    @Override
    public <R, T> C collectMany(BiConsumer<R, T> accumulator, Class<R> containerType, Class<T> modelType) {
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
    public C next(HtmlUnitScrapingStep<?> nextStep) {
        HtmlUnitScrapingStep<?> nextCopy = nextStep.copy()
                .setStepDeclarationLine(StepsUtils.getStackTraceElementAt(3));
        return addNextStep(nextCopy);
    }

    @Override
    public C nextExclusively(HtmlUnitScrapingStep<?> nextStep) {
        HtmlUnitScrapingStep<?> nextCopy = nextStep.copy()
                .setStepDeclarationLine(StepsUtils.getStackTraceElementAt(3))
                .setExclusiveExecution(true);
        return addNextStep(nextCopy);
    }

    @Override
    public <T> C nextIf(Predicate<T> condition, Class<T> modelType, HtmlUnitScrapingStep<?> nextStep) {
        HtmlUnitScrapingStep<?> nextCopy = nextStep.copy()
                .setStepDeclarationLine(StepsUtils.getStackTraceElementAt(3))
                .setExecuteIf(new ExecutionCondition(condition, modelType));
        return addNextStep(nextCopy);
    }

    @Override
    public <T> C nextIfExclusively(Predicate<T> condition, Class<T> modelType, HtmlUnitScrapingStep<?> nextStep) {
        HtmlUnitScrapingStep<?> nextCopy = nextStep.copy()
                .setStepDeclarationLine(StepsUtils.getStackTraceElementAt(3))
                .setExecuteIf(new ExecutionCondition(condition, modelType))
                .setExclusiveExecution(true);
        return addNextStep(nextCopy);

    }

    // TODO create method nextSequentially() useful when we want to visit different urls one by one and many other ... possibly?


    // TODO think about these ...
//    public C expectFindingNone();
//    public C expectFindingOne();
//    public C expectFindingOneAtLeast();
//    public C expectFindingOneAtMost();
//    public C expectFindingMany();

}
