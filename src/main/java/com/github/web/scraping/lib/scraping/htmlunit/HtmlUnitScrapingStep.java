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

import com.github.web.scraping.lib.parallelism.StepExecOrder;
import com.github.web.scraping.lib.parallelism.StepTask;
import com.github.web.scraping.lib.scraping.MakingHttpRequests;
import com.github.web.scraping.lib.scraping.ScrapingServices;
import com.github.web.scraping.lib.scraping.StepThrottling;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@Log4j2
public abstract class HtmlUnitScrapingStep<T> implements StepThrottling {

    protected final List<HtmlUnitScrapingStep<?>> nextSteps;

    @Getter @Setter
    protected boolean exclusiveExecution = false;

    /**
     * condition for the execution of this step
     */
    @Getter @Setter
    protected ExecutionCondition executeIf = null;

    protected CollectorSetups collectorSetups = new CollectorSetups();

    protected static Function<String, String> NO_TEXT_TRANSFORMATION = s -> s;

    /**
     * relevant for steps that do scrape textual values
     */
    protected Function<String, String> parsedTextTransformation = NO_TEXT_TRANSFORMATION; // by default return the string as-is

    protected ScrapingServices services;

    /**
     * for logging and debugging
     */
    private String name = getClass().getSimpleName() + "-unnamed-step";

    // TODO actually use in logging ...
    /**
     * To be used on logging to give the user an accurate location of a problematic step
     */
    @Getter @Setter
    protected StackTraceElement stepDeclarationLine;

    public HtmlUnitScrapingStep(List<HtmlUnitScrapingStep<?>> nextSteps) {
        this.nextSteps = Objects.requireNonNullElse(nextSteps, new ArrayList<>());
    }


    protected abstract StepExecOrder execute(ParsingContext ctx);

    // internal usage only
    protected void setServices(ScrapingServices services) {
        this.services = services;
    }

    // internal usage only for propagating the Service instance down the step call hierarchy
    protected void propagateServicesTo(HtmlUnitScrapingStep<?> step) {
        step.setServices(this.services);
    }

    protected String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public T stepName(String name) {
        this.name = name != null && !name.toLowerCase().contains("step") ? name + "-step" : name;
        return (T) this;
    }

    protected String transformParsedText(String text) {
        return text != null ? parsedTextTransformation.apply(text) : null;
    }

    /**
     * @param runnable      task can be executed immediately or at some later point based on the given mode
     */
    protected void handleExecution(StepExecOrder stepExecOrder, Runnable runnable) {
        StepTask stepTask = new StepTask(stepExecOrder, exclusiveExecution, getName(), runnable, throttlingAllowed(), this instanceof MakingHttpRequests);
        services.getActiveStepsTracker().track(stepExecOrder, getName());
        services.getStepTaskExecutor().submit(
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
        return services.getStepExecOrderGenerator().genNextOrderAfter(stepAtPrevLevel);
    }

    @Getter
    protected static class ExecutionCondition {
        protected final Predicate<Object> predicate;
        protected final Class<?> modelType;

        @SuppressWarnings("unchecked")
        public ExecutionCondition(Predicate<?> predicate, Class<?> modelType) {
            this.predicate = (Predicate<Object>) predicate;
            this.modelType = modelType;
        }
    }

}
