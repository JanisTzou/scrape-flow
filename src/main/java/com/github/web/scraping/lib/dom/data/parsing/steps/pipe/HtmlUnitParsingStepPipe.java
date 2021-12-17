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

package com.github.web.scraping.lib.dom.data.parsing.steps.pipe;

import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.dom.data.parsing.steps.Collecting;
import com.github.web.scraping.lib.dom.data.parsing.steps.HtmlUnitCollectorSetupStep;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

// TODO maybe rename to static ... ??
// TODO make an interface?
public abstract class HtmlUnitParsingStepPipe implements HtmlUnitCollectorSetupStep<HtmlUnitParsingStepPipe> {

    protected HtmlUnitParsingStepPipe prevStep;

    protected HtmlUnitParsingStepPipe(HtmlUnitParsingStepPipe prevStep) {
        this.prevStep = prevStep;
    }

    protected HtmlUnitParsingStepPipe() {
    }

    public abstract List<StepResult> execute(ParsingContext ctx);

    public <R, T> CollectionSetupPiped collector(Supplier<T> modelSupplier, Supplier<R> containerSupplier, BiConsumer<R, T> accumulator) {
        Collecting<R, T> col = new Collecting<>(modelSupplier, containerSupplier, accumulator);
        return new CollectionSetupPiped(this, col);
    }

    public <R, T> CollectionSetupPiped collector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator) {
        Collecting<R, T> col = new Collecting<>(modelSupplier, null, accumulator);
        return new CollectionSetupPiped(this, col);
    }

    public HtmlUnitParsingStepPipe then(HtmlUnitParsingStepPipe nextStep) {
        return new HtmlUnitChainableStepPipe(this, nextStep);
    }



}
