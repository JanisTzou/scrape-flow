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

import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class HtmlUnitParsingStep<C> {

    // just for debugging
    private String name;
    protected final List<HtmlUnitParsingStep<?>> nextSteps;
    protected Collecting<?, ?> collecting;


    public HtmlUnitParsingStep(List<HtmlUnitParsingStep<?>> nextSteps) {
        this.nextSteps = Objects.requireNonNullElse(nextSteps, new ArrayList<>());
    }

    public abstract List<StepResult> execute(ParsingContext ctx);

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public C setName(String name) {
        this.name = name;
        return (C) this;
    }

}
