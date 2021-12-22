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
    implements HtmlUnitChainableStep<C>, HtmlUnitCollectorSetupStep<C> {

    public CommonOperationsStepBase(List<HtmlUnitParsingStep<?>> nextSteps) {
        super(nextSteps);
    }

    @Deprecated // TODO do not expose the container to the outside, work with it only internally ... instead of supplying a container with an accumulator here we should expect a listener ...
                // TODO or actually ... this might be still relevant for SYNC execution ... or not if we return a plain list of stuff in SYNC execution ...
    @SuppressWarnings("unchecked")
    @Override
    public <R, T> C setCollector(Supplier<T> modelSupplier, Supplier<R> containerSupplier, BiConsumer<R, T> accumulator) {
        mustBeNull(this.collecting);
        this.collecting = new Collecting<>(modelSupplier, containerSupplier, accumulator, null);
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, T> C setCollector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator) {
        mustBeNull(this.collecting);
        this.collecting = new Collecting<>(modelSupplier, null, accumulator, null);
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, T> C setCollector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator, ParsedDataListener<T> parsedDataListener) {
        mustBeNull(this.collecting);
        this.collecting = new Collecting<>(modelSupplier, null, accumulator, parsedDataListener);
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, T> C setCollector(Supplier<T> modelSupplier,  Supplier<R> containerSupplier, BiConsumer<R, T> accumulator, ParsedDataListener<T> parsedDataListener) {
        mustBeNull(this.collecting);
        this.collecting = new Collecting<>(modelSupplier, containerSupplier, accumulator, parsedDataListener);
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public C then(HtmlUnitParsingStep<?> nextStep) {
        this.nextSteps.add(nextStep);
        return (C) this;
    }

}
