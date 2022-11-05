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

import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.data.collectors.Collectors;
import com.github.scrape.flow.debugging.DebuggingOptions;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.execution.TaskDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public abstract class ScrapingStep<C extends ScrapingStep<C>> implements Throttling {

    protected static final Function<String, String> NO_MAPPING = s -> s;

    private final List<ScrapingStep<?>> nextSteps;

    protected boolean exclusiveExecution = false;

    /**
     * condition for the execution of this step
     */
    protected StepExecutionCondition executeIf = StepExecutionCondition.NO_CONDITIONS;

    /**
     * relevant for steps that do scrape textual values
     */
    protected Function<String, String> parsedValueMapper = NO_MAPPING; // by default return the string as-is

    private Collectors collectors = new Collectors();

    protected DebuggingOptions stepDebugging = new DebuggingOptions();

    /**
     * for logging and debugging
     */
    private String name = getClass().getSimpleName() + "-unnamed-step";


    protected ScrapingStep(List<ScrapingStep<?>> nextSteps) {
        this.nextSteps = new ArrayList<>(Objects.requireNonNullElse(nextSteps, Collections.emptyList()));
    }

    protected ScrapingStep() {
        this(null);
    }

    protected abstract StepOrder execute(ScrapingContext ctx, ScrapingServices services);

    protected abstract C copy();

    /**
     * @return copy of this step
     */
    @SuppressWarnings("SameParameterValue")
    protected C setExclusiveExecution(boolean exclusiveExecution) {
        return copyModifyAndGet(copy -> {
            copy.exclusiveExecution = exclusiveExecution;
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
    protected C setParsedValueMapper(Function<String, String> conversion) {
        return copyModifyAndGet(copy -> {
            copy.parsedValueMapper = conversion;
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

    protected List<ScrapingStep<?>> getNextSteps() {
        return nextSteps;
    }

    /**
     * @return copy of this step
     */
    protected C addNextStep(ScrapingStep<?> nextStep) {
        ScrapingStep<?> nsCopy = nextStep.copy();
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
    protected void addNextStepMutably(ScrapingStep<?> nextStep) {
        this.nextSteps.add(nextStep);
    }

    protected boolean isExclusiveExecution() {
        return exclusiveExecution;
    }

    protected StepExecutionCondition getExecuteIf() {
        return executeIf;
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

    protected abstract ClientType getClientType();

    protected abstract ClientReservationType getClientReservationType();

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

    protected String mapParsedValue(String value) {
        return value != null ? parsedValueMapper.apply(value) : null;
    }

    protected void submitForExecution(StepOrder stepOrder, Runnable runnable, ScrapingServices services) {
        StepOrder stepHierarchyOrder = services.getStepHierarchyRepository().getMetadataFor(this).getStepHierarchyOrder();
        TaskDefinition taskDefinition = new TaskDefinition(stepHierarchyOrder, stepOrder, isExclusiveExecution(), getName(), runnable, throttlingAllowed(), this instanceof MakingHttpRequests, getClientType(), getClientReservationType());
        services.getTaskService().submitForExecution(taskDefinition);
    }

    /**
     * Enables logging the source codes if all the elements found by this step and processed.
     */
    @SuppressWarnings("unchecked")
    public DebuggableStep<C> debugOptions() {
        return new DebuggableStep<>((C) this);
    }


    protected C copyModifyAndGet(UnaryOperator<C> copyMutation) {
        return copyMutation.apply(this.copy());
    }

    @SuppressWarnings("unchecked")
    protected C copyFieldValuesTo(ScrapingStep<?> other) {
        other.executeIf = this.executeIf;
        other.collectors = this.collectors.copy();
        other.parsedValueMapper = this.parsedValueMapper;
        other.name = this.name;
        other.nextSteps.addAll(this.nextSteps);
        other.stepDebugging = stepDebugging.copy();
        return (C) other;
    }

}
