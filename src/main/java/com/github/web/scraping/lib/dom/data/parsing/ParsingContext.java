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

package com.github.web.scraping.lib.dom.data.parsing;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.web.scraping.lib.dom.data.parsing.steps.ModelProxy;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Allows sharing information between parsing steps
 */
@Getter
@Setter
@ToString
public class ParsingContext<ModelT, ContainerT> {

    /**
     * StepOrder value at the previous level of step hierarchy (so the step calling this step)
     */
    @Nonnull
    private final StepExecOrder prevStepExecOrder;

    private DomNode node;

    @Nullable
    private ModelProxy<ModelT> modelProxy;

    @Nullable
    private ContainerT container;

    private String parsedText;

    // TODO make sure this contains the full URL so that it can be further navigated to ...
    private String parsedURL;



    public ParsingContext(StepExecOrder prevStepExecOrder, DomNode node) {
        this(prevStepExecOrder, node, null, null);
    }

    public ParsingContext(StepExecOrder prevStepExecOrder, DomNode node, @Nullable ModelProxy<ModelT> modelProxy, @Nullable ContainerT container) {
        this(prevStepExecOrder, node, modelProxy, container, null, null);
    }

    public ParsingContext(@Nonnull StepExecOrder prevStepExecOrder, DomNode node, @Nullable ModelProxy<ModelT> modelProxy, @Nullable ContainerT container, String parsedText, String parsedURL) {
        this.prevStepExecOrder = Objects.requireNonNull(prevStepExecOrder);
        this.node = node;
        this.modelProxy = modelProxy;
        this.container = container;
        this.parsedText = parsedText;
        this.parsedURL = parsedURL;
    }

    public Builder<ModelT, ContainerT> toBuilder() {
        return new Builder<>(this);
    }

    public static class Builder<ModelT, ContainerT> {

        private StepExecOrder prevStepExecOrder;
        private DomNode node;
        private ModelProxy<ModelT> modelProxy;
        private ContainerT container;
        private String parsedText;
        private String parsedURL;

        public Builder(StepExecOrder prevStepExecOrder, DomNode node, ModelProxy<ModelT> modelProxy, ContainerT container, String parsedText, String parsedURL) {
            this.prevStepExecOrder = prevStepExecOrder;
            this.node = node;
            this.modelProxy = modelProxy;
            this.container = container;
            this.parsedText = parsedText;
            this.parsedURL = parsedURL;
        }

        public Builder(ParsingContext<ModelT, ContainerT> ctx) {
            this(ctx.prevStepExecOrder, ctx.node, ctx.modelProxy, ctx.container, ctx.parsedText, ctx.parsedURL);
        }

        public Builder<ModelT, ContainerT> setPrevStepOrder(StepExecOrder prevStepExecOrder) {
            this.prevStepExecOrder = prevStepExecOrder;
            return this;
        }

        public Builder<ModelT, ContainerT> setNode(DomNode node) {
            this.node = node;
            return this;
        }

        public Builder<ModelT, ContainerT> setModelProxy(ModelProxy<ModelT> modelProxy) {
            this.modelProxy = modelProxy;
            return this;
        }

        public Builder<ModelT, ContainerT> setContainer(ContainerT container) {
            this.container = container;
            return this;
        }

        public Builder<ModelT, ContainerT> setParsedText(String parsedText) {
            this.parsedText = parsedText;
            return this;
        }

        public Builder<ModelT, ContainerT> setParsedURL(String parsedURL) {
            this.parsedURL = parsedURL;
            return this;
        }

        public ParsingContext<ModelT, ContainerT> build() {
            return new ParsingContext<>(prevStepExecOrder, node, modelProxy, container, parsedText, parsedURL);
        }
    }
}
