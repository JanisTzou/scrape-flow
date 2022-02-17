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

package com.github.scrape.flow.scraping;

import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.data.collectors.Collectors;
import com.github.scrape.flow.debugging.DebuggingOptions;
import com.github.scrape.flow.drivers.SeleniumDriversManager;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.execution.TaskBasis;
import com.github.scrape.flow.execution.TaskService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public abstract class ScrapingStepBase<C extends ScrapingStepBase<C>> implements Throttling {

    protected static final Function<String, String> NO_VALUE_CONVERSION = s -> s;

    private final List<ScrapingStepBase<?>> nextSteps;

    protected boolean exclusiveExecution = false;

    /**
     * condition for the execution of this step
     */
    protected StepExecutionCondition executeIf = StepExecutionCondition.NO_CONDITIONS;

    /**
     * relevant for steps that do scrape textual values
     */
    protected Function<String, String> parsedValueConversion = NO_VALUE_CONVERSION; // by default return the string as-is

    /**
     * To be used on logging to give the user an accurate location of a problematic step
     */
    protected StackTraceElement stepDeclarationLine;

    private Collectors collectors = new Collectors();

    protected DebuggingOptions stepDebugging = new DebuggingOptions();

    /**
     * for logging and debugging
     */
    private String name = getClass().getSimpleName() + "-unnamed-step";


    protected ScrapingStepBase(List<ScrapingStepBase<?>> nextSteps) {
        this.nextSteps = new ArrayList<>(Objects.requireNonNullElse(nextSteps, Collections.emptyList()));
    }

    protected ScrapingStepBase() {
        this(null);
    }

    protected abstract StepOrder execute(ScrapingContext ctx, ScrapingServices services);

    protected abstract C copy();

    /**
     * @return copy of this step
     */
    protected C setExclusiveExecution(boolean exclusiveExecution) {
        return copyModifyAndGet(copy -> {
            copy.exclusiveExecution = exclusiveExecution;
            return copy;
        });
    }

    /**
     * @return copy of this step
     */
    protected C setStepDeclarationLine(StackTraceElement stepDeclarationLine) {
        return copyModifyAndGet(copy -> {
            copy.stepDeclarationLine = stepDeclarationLine;
            return copy;
        });
    }

    /**
     * @return copy of this step
     */
    protected C setExecuteIf(StepExecutionCondition executeIf) {
        return copyModifyAndGet(copy -> {
            copy.executeIf = executeIf;
            return copy;
        });
    }

    /**
     * @return copy of this step
     */
    protected C setParsedValueConversion(Function<String, String> conversion) {
        return copyModifyAndGet(copy -> {
            copy.parsedValueConversion = conversion;
            return copy;
        });
    }

    protected Collectors getCollectors() {
        return collectors;
    }

    /**
     * mutating - internal usage only
     */
    protected void setCollectorsMutably(Collectors collectors) {
        this.collectors = collectors;
    }

    /**
     * @return copy of this step
     */
    protected C addCollector(Collector cs) {
        return copyModifyAndGet(copy -> {
            Collectors csCopy = getCollectors().copy();
            csCopy.add(cs);
            copy.setCollectorsMutably(csCopy);
            return copy;
        });
    }

    protected List<ScrapingStepBase<?>> getNextSteps() {
        return nextSteps;
    }

    /**
     * @return copy of this step
     */
    protected C addNextStep(ScrapingStepBase<?> nextStep) {
        ScrapingStepBase<?> nsCopy = nextStep.copy();
        return copyModifyAndGet(copy -> {
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
    protected void addNextStepMutably(ScrapingStepBase<?> nextStep) {
        this.nextSteps.add(nextStep);
    }

    protected boolean isExclusiveExecution() {
        return exclusiveExecution;
    }

    protected StepExecutionCondition getExecuteIf() {
        return executeIf;
    }

    protected StackTraceElement getStepDeclarationLine() {
        return stepDeclarationLine;
    }

    protected DebuggingOptions getStepDebugging() {
        return stepDebugging;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected abstract ScrapingType getScrapingType();

    /**
     * Sets the name of this step to be used in logging. Useful for debugging purposes to identify problematic steps.
     * @return a copy of this step with the given <code>name</code> set
     */
    public C stepName(String name) {
        return copyModifyAndGet(copy -> {
            copy.setName(name != null && !name.toLowerCase().contains("step") ? name + "-step" : name);
            return copy;
        });
    }

    protected String convertParsedText(String text) {
        return text != null ? parsedValueConversion.apply(text) : null;
    }

    protected void submitForExecution(StepOrder stepOrder, Runnable runnable, TaskService taskService, SeleniumDriversManager seleniumDriversManager) {
        TaskBasis stepTask = new TaskBasis(stepOrder, isExclusiveExecution(), getName(), runnable, throttlingAllowed(), this instanceof MakingHttpRequests, getScrapingType(), seleniumDriversManager);
        taskService.submitForExecution(stepTask);
    }

    /**
     * Enables logging the source codes if all the elements found by this step and processed.
     */
    @SuppressWarnings("unchecked")
    public DebuggableStep<C> debugOptions() {
        return new DebuggableStep<C>((C) this);
    }


    protected C copyModifyAndGet(UnaryOperator<C> copyMutation) {
        return copyMutation.apply(this.copy());
    }

    @SuppressWarnings("unchecked")
    protected C copyFieldValuesTo(ScrapingStepBase<?> other) {
        other.executeIf = this.executeIf;
        other.collectors = this.collectors.copy();
        other.parsedValueConversion = this.parsedValueConversion;
        other.name = this.name;
        other.stepDeclarationLine = this.stepDeclarationLine;
        other.nextSteps.addAll(this.nextSteps);
        other.stepDebugging = stepDebugging.copy();
        return (C) other;
    }

}
