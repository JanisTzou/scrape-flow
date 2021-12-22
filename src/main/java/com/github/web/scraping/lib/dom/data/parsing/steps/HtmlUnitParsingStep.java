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
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import com.github.web.scraping.lib.parallelism.StepTask;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Log4j2
public abstract class HtmlUnitParsingStep<T> implements StepThrottling {

    protected final List<HtmlUnitParsingStep<?>> nextSteps;
    protected Collecting<?, ?> collecting;
    // renlevant for steps that do scrape textual values
    protected Function<String, String> parsedTextTransformation = s -> s; // by default return the string as-is
    protected CrawlingServices services;
    // for logging and debugging
    private String name = getClass().getSimpleName() + "-unnamed-step";


    public HtmlUnitParsingStep(List<HtmlUnitParsingStep<?>> nextSteps) {
        this.nextSteps = Objects.requireNonNullElse(nextSteps, new ArrayList<>());
    }

    // TODO instead of step result return generated order ...
    public abstract <ModelT, ContainerT> StepExecOrder execute(ParsingContext<ModelT, ContainerT> ctx);

    // internal usage only
    protected void setServices(CrawlingServices services) {
        this.services = services;
    }

    // internal usage only for propagating the Service instance down the step call hierarchy
    protected void propagateServicesTo(HtmlUnitParsingStep<?> step) {
        step.setServices(this.services);
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public T setName(String name) {
        this.name = name != null && !name.toLowerCase().contains("step") ? name + "-step" : name;
        return (T) this;
    }

    protected String transformParsedText(String text) {
        return text != null ? parsedTextTransformation.apply(text) : null;
    }

    /**
     * @param stepExecOrder
     * @param runnable  task can be executed immediately or at some later point based on the given mode
     */
    protected void handleExecution(StepExecOrder stepExecOrder, Runnable runnable) {
            StepTask stepTask = new StepTask(stepExecOrder, getName(), runnable, throttlingAllowed());
            services.getActiveStepsTracker().track(stepExecOrder, getName());
            services.getTaskQueue().submit(
                    stepTask,
                    r -> handleFinishedStep(stepExecOrder),
                    e -> handleFinishedStep(stepExecOrder) // even when we finish in error there might be successfully parsed other data that might be waiting to get published outside
            );
    }

    private void handleFinishedStep(StepExecOrder stepExecOrder) {
        services.getActiveStepsTracker().untrack(stepExecOrder);
        services.getNotificationService().notifyAfterStepFinished(stepExecOrder);
    }

    protected StepExecOrder genNextOrderAfter(StepExecOrder stepAtPrevLevel) {
        StepExecOrder stepExecOrder = services.getStepExecOrderGenerator().genNextOrderAfter(stepAtPrevLevel);
        return stepExecOrder;
    }

    // TODO methid failSilently() ???

}
