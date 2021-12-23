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

import com.github.web.scraping.lib.parallelism.ParsedDataListener;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;


public abstract class CommonOperationsStepBase<C> extends HtmlUnitParsingStep<C>
        implements HtmlUnitSupportingNextStep<C>, HtmlUnitStepSupportingCollection<C> {

    public CommonOperationsStepBase(List<HtmlUnitParsingStep<?>> nextSteps) {
        super(nextSteps);
    }

    @Override
    public <R, T> C collect(BiConsumer<R, T> accumulator, Class<R> containerType, Class<T> modelType) {
        this.collectorSetups.add(new CollectorSetup(accumulator, modelType, containerType));
        return (C) this;
    }

    @Override
    public <T> C setCollector(Supplier<T> modelSupplier, Class<T> modelType) {
        this.collectorSetups.add(new CollectorSetup(modelSupplier, modelType));
        return (C) this;
    }

    @Override
    public <T> C setCollector(Supplier<T> modelSupplier, Class<T> modelType, ParsedDataListener<T> parsedDataListener) {
        this.collectorSetups.add(new CollectorSetup(modelSupplier, modelType, parsedDataListener));
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public C next(HtmlUnitParsingStep<?> nextStep) {
        this.nextSteps.add(nextStep);
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public C nextExclusively(HtmlUnitParsingStep<?> nextStep) {
        nextStep.setExclusiveExecution(true);
        this.nextSteps.add(nextStep);
        return (C) this;
    }

    // TODO create nextAsGroup(StepGroup)

    // TODO think about these ...
//    public C expectFindingNone();
//    public C expectFindingOne();
//    public C expectFindingOneAtLeast();
//    public C expectFindingOneAtMost();
//    public C expectFindingMany();

}
