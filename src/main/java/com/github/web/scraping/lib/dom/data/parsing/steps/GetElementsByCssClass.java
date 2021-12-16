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

import com.github.web.scraping.lib.dom.data.parsing.ParsedElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsedElements;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.scraping.utils.HtmlUnitUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GetElementsByCssClass<R, T> extends HtmlUnitParsingStep {

    private final String cssClassName;

    // for each iterated element these strategies will be applied to execute data ...
    private final List<HtmlUnitParsingStep> nextSteps;
    private final Collecting<R, T> collecting;

    public GetElementsByCssClass(String cssClassName, List<HtmlUnitParsingStep> nextSteps, Collecting<R, T> collecting) {
        this.cssClassName = cssClassName;
        this.nextSteps = nextSteps;
        this.collecting = Objects.requireNonNullElse(collecting, new Collecting<>());
    }

    public static Builder builder(String cssClassName) {
        return new Builder(cssClassName);
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        final Optional<R> container = collecting.supplyContainer();

        final List<StepResult> stepResults = HtmlUnitUtils.getAllChildElementsByClass(ctx.getNode(), cssClassName)
                .stream()
                .flatMap(node -> {
                            T m = collecting.supplyModel().orElse((T) ctx.getModel());
                            return nextSteps.stream().flatMap(s -> {
                                ParsingContext nextCtx = new ParsingContext(node, m, container.orElse(null));
                                return s.execute(nextCtx).stream();
                            });
                        }
                )
                .collect(Collectors.toList());

        // TODO hmm what to return here ? if accumulator was set vs if not ...
        if (container.isPresent()) {
            final List<ParsedElement> hrefs = stepResults.stream().filter(sr -> sr instanceof ParsedElement pe && pe.isHasHRef()).map(sr -> (ParsedElement) sr).collect(Collectors.toList());
            // TODO encapsulate this kind of logic somewhere ... maybe in Collecting class

            // TODO somehow collect this into the container ... if given ...
            stepResults.stream()
                    .filter(sr -> sr instanceof ParsedElement)
                    .map(sr -> (ParsedElement) sr)
                    .map(ParsedElement::getModel)
                    .forEach(model -> collecting.getAccumulator().accept(container.get(), (T) model));

            return List.of(new ParsedElements(container, hrefs));
        } else {
            return stepResults;
        }
    }


    public static class Builder {

        private String cssClassName;
        private List<HtmlUnitParsingStep> nextSteps = new ArrayList<>();
        private Collecting<?, ?> collecting;

        Builder(String cssClassName) {
            this.cssClassName = cssClassName;
        }

        public Builder then(HtmlUnitParsingStep nextStep) {
            this.nextSteps.add(nextStep);
            return this;
        }

        public Builder collector(Supplier<?> modelSupplier) {
            this.collecting = new Collecting<>(modelSupplier, null, null);
            return this;
        }

        // TODO make an overloaded version that will create a list ? if used does not want to supply a container?
        // here we need to generics ...
        public <R, T> Builder collector(Supplier<R> containerSupplier, Supplier<T> modelSupplier, BiConsumer<R, T> accumulator) {
            this.collecting = new Collecting<>(modelSupplier, containerSupplier, accumulator);
            return this;
        }

        // if a collector exists ...and it is found in the scraping context ...
        public <R, T> Builder collector(Supplier<T> modelSupplier, BiConsumer<R, T> accumulator) {
            this.collecting = new Collecting<>(modelSupplier, null, accumulator);
            return this;
        }

        public GetElementsByCssClass build() {
            return new GetElementsByCssClass<>(cssClassName, nextSteps, collecting);
        }
    }

}
