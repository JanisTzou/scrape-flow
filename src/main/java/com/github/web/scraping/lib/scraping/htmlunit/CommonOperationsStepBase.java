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

import com.github.web.scraping.lib.parallelism.ParsedDataListener;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;


public abstract class CommonOperationsStepBase<C extends HtmlUnitScrapingStep<C>> extends HtmlUnitScrapingStep<C>
        implements HtmlUnitStepSupportingNext<C>, HtmlUnitStepSupportingCollection<C> {

    public CommonOperationsStepBase(List<HtmlUnitScrapingStep<?>> nextSteps) {
        super(nextSteps);
    }

    @Override
    public <R, T> C collect(BiConsumer<R, T> accumulator, Class<R> containerType, Class<T> modelType) {
        return addCollectorSetup(new CollectorSetup(accumulator, modelType, containerType));
    }

    @Override
    public <T> C setCollector(Supplier<T> modelSupplier, Class<T> modelType) {
        return addCollectorSetup(new CollectorSetup(modelSupplier, modelType));
    }

    @Override
    public <T> C setCollector(Supplier<T> modelSupplier, Class<T> modelType, ParsedDataListener<T> parsedDataListener) {
        return addCollectorSetup(new CollectorSetup(modelSupplier, modelType, parsedDataListener));
    }

    @Override
    public C next(HtmlUnitScrapingStep<?> nextStep) {
        HtmlUnitScrapingStep<?> nextCopy = nextStep.copy()
                .setStepDeclarationLine(getStepDeclarationStackTraceEl());
        return addNextStep(nextCopy);
    }

    @Override
    public C nextExclusively(HtmlUnitScrapingStep<?> nextStep) {
        HtmlUnitScrapingStep<?> nextCopy = nextStep.copy()
                .setStepDeclarationLine(getStepDeclarationStackTraceEl())
                .setExclusiveExecution(true);
        return addNextStep(nextCopy);
    }

    @Override
    public <T> C nextIf(Predicate<T> condition, Class<T> modelType, HtmlUnitScrapingStep<?> nextStep) {
        HtmlUnitScrapingStep<?> nextCopy = nextStep.copy()
                .setStepDeclarationLine(getStepDeclarationStackTraceEl())
                .setExecuteIf(new ExecutionCondition(condition, modelType));
        return addNextStep(nextCopy);
    }

    @Override
    public <T> C nextIfExclusively(Predicate<T> condition, Class<T> modelType, HtmlUnitScrapingStep<?> nextStep) {
        HtmlUnitScrapingStep<?> nextCopy = nextStep.copy()
                .setStepDeclarationLine(getStepDeclarationStackTraceEl())
                .setExecuteIf(new ExecutionCondition(condition, modelType))
                .setExclusiveExecution(true);
        return addNextStep(nextCopy);

    }

    private StackTraceElement getStepDeclarationStackTraceEl() {
        return Thread.currentThread().getStackTrace()[2];
    }

    // TODO create method nextSequentially() useful when we want to visit different urls one by one and many other ... possibly?


    // TODO think about these ...
//    public C expectFindingNone();
//    public C expectFindingOne();
//    public C expectFindingOneAtLeast();
//    public C expectFindingOneAtMost();
//    public C expectFindingMany();

}
