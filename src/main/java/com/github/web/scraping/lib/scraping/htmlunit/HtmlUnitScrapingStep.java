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

import com.github.web.scraping.lib.debugging.Debugging;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import com.github.web.scraping.lib.parallelism.StepTask;
import com.github.web.scraping.lib.scraping.MakingHttpRequests;
import com.github.web.scraping.lib.scraping.ScrapingServices;
import com.github.web.scraping.lib.scraping.StepThrottling;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Log4j2
public abstract class HtmlUnitScrapingStep<C extends HtmlUnitScrapingStep<C>> implements StepThrottling {

    protected static Function<String, String> NO_TEXT_TRANSFORMATION = s -> s;

    private final List<HtmlUnitScrapingStep<?>> nextSteps;

    protected boolean exclusiveExecution = false;

    /**
     * condition for the execution of this step
     */
    protected ExecutionCondition executeIf = null;

    /**
     * relevant for steps that do scrape textual values
     */
    protected Function<String, String> parsedTextTransformation = NO_TEXT_TRANSFORMATION; // by default return the string as-is

    protected ScrapingServices services;

    /**
     * To be used on logging to give the user an accurate location of a problematic step
     */
    protected StackTraceElement stepDeclarationLine;
    private CollectorSetups collectorSetups = new CollectorSetups();

    protected Debugging stepDebugging = new Debugging();

    // TODO actually use in logging ...
    /**
     * for logging and debugging
     */
    private String name = getClass().getSimpleName() + "-unnamed-step";


    protected HtmlUnitScrapingStep(List<HtmlUnitScrapingStep<?>> nextSteps) {
        this.nextSteps = new ArrayList<>(Objects.requireNonNullElse(nextSteps, Collections.emptyList()));
    }


    protected abstract StepExecOrder execute(ScrapingContext ctx);

    protected HtmlUnitStepHelper getHelper() {
        return new HtmlUnitStepHelper(this);
    }

    /**
     * @return copy of this step
     */
    protected C setExclusiveExecution(boolean exclusiveExecution) {
        return copyThisMutateAndGet(copy -> {
            copy.exclusiveExecution = exclusiveExecution;
            return copy;
        });
    }

    // TODO having this public is a problem ... we will need a builder to be able to have clean interfaces ...
    /**
     * @return copy of this step
     */
    protected C setStepDeclarationLine(StackTraceElement stepDeclarationLine) {
        return copyThisMutateAndGet(copy -> {
            copy.stepDeclarationLine = stepDeclarationLine;
            return copy;
        });
    }

    /**
     * @return copy of this step
     */
    protected C setExecuteIf(ExecutionCondition executeIf) {
        return copyThisMutateAndGet(copy -> {
            copy.executeIf = executeIf;
            return copy;
        });
    }

    /**
     * @return copy of this step
     */
    protected C setParsedTextTransformation(Function<String, String> transformation) {
        return copyThisMutateAndGet(copy -> {
            copy.parsedTextTransformation = transformation;
            return copy;
        });
    }

    protected CollectorSetups getCollectorSetups() {
        return collectorSetups;
    }

    /**
     * mutating - internal usage only
     */
    protected void setCollectorSetupsMutably(CollectorSetups collectorSetups) {
        this.collectorSetups = collectorSetups;
    }

    /**
     * @return copy of this step
     */
    protected C addCollectorSetup(CollectorSetup cs) {
        return copyThisMutateAndGet(copy -> {
            CollectorSetups csCopy = getCollectorSetups().copy();
            csCopy.add(cs);
            copy.setCollectorSetupsMutably(csCopy);
            return copy;
        });
    }

    protected List<HtmlUnitScrapingStep<?>> getNextSteps() {
        return nextSteps;
    }

    /**
     * @return copy of this step
     */
    protected C addNextStep(HtmlUnitScrapingStep<?> nextStep) {
        HtmlUnitScrapingStep<?> nsCopy = nextStep.copy();
        return copyThisMutateAndGet(copy -> {
            copy.addNextStepMutably(nsCopy);
            return copy;
        });
    }

    /**
     * Internal usage only
     * Mutates this instance by adding the specific <code>nextStep</code>.
     * Does not create a copy of either <code>this</code> step or <code>nextStep</code>.
     * Should only be used at runtime (not at Assembly time)
     */
    protected void addNextStepMutably(HtmlUnitScrapingStep<?> nextStep) {
        this.nextSteps.add(nextStep);
    }

    /**
     * Internal usage only
     * Mutates this instance by adding the specific <code>services</code>.
     * Does not create a copy of either <code>this</code> step or <code>nextStep</code>.
     * Should only be used at runtime (not at Assembly time)
     */
    protected void setServicesMutably(ScrapingServices services) {
        this.services = services;
    }

    protected ScrapingServices getServices() {
        return services;
    }

    protected boolean isExclusiveExecution() {
        return exclusiveExecution;
    }

    protected ExecutionCondition getExecuteIf() {
        return executeIf;
    }

    protected StackTraceElement getStepDeclarationLine() {
        return stepDeclarationLine;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    /**
     * @return a copy of this step with the given <code>name</code> set
     */
    public C stepName(String name) {
        return copyThisMutateAndGet(copy -> {
            copy.setName(name != null && !name.toLowerCase().contains("step") ? name + "-step" : name);
            return copy;
        });
    }

    protected String transformParsedText(String text) {
        return text != null ? parsedTextTransformation.apply(text) : null;
    }

    /**
     * @param runnable task can be executed immediately or at some later point based on the given mode
     */
    protected void handleExecution(StepExecOrder stepExecOrder, Runnable runnable) {
        StepTask stepTask = new StepTask(stepExecOrder, isExclusiveExecution(), getName(), runnable, throttlingAllowed(), this instanceof MakingHttpRequests);
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

    /**
     * Enables logging the source codes if all the elements found by this step and processed.
     */
    @SuppressWarnings("unchecked")
    public DebuggableStep<C> debug() {
        return new DebuggableStep<C>((C) this);
    }

    // TODO having this public is a problem ... we will need a builder to be able to have clean interfaces ...
    protected abstract C copy();

    protected C copyThisMutateAndGet(UnaryOperator<C> copyMutation) {
        return copyMutation.apply(this.copy());
    }

    @SuppressWarnings("unchecked")
    protected C copyFieldValuesTo(HtmlUnitScrapingStep<?> other) {
        other.executeIf = this.executeIf;
        other.collectorSetups = this.collectorSetups.copy();
        other.parsedTextTransformation = this.parsedTextTransformation;
        other.name = this.name;
        other.stepDeclarationLine = this.stepDeclarationLine;
        other.nextSteps.addAll(this.nextSteps);
        other.services = this.services;
        other.stepDebugging = stepDebugging.copy();
        return (C) other;
    }

    // TODO make possible to print the element's code for debugging ... on a targeted step basis ...


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
