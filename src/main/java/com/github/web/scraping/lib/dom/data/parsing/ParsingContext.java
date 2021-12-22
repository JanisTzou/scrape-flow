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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.web.scraping.lib.dom.data.parsing.steps.ModelWrapper;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

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
    private ModelWrapper<ModelT> modelWrapper;

    @Nullable
    private ContainerT container;

    private String parsedText;

    // TODO make sure this contains the full URL so that it can be further navigated to ...
    private String parsedURL;

    // used in special cases when we have recursive step execution and we always want to set the prev step to some initial value ...
    private StepExecOrder recursiveRootStepExecOrder;



    public ParsingContext(StepExecOrder prevStepExecOrder, DomNode node) {
        this(prevStepExecOrder, node, null, null);
    }

    public ParsingContext(StepExecOrder prevStepExecOrder, DomNode node, @Nullable ModelWrapper<ModelT> modelWrapper, @Nullable ContainerT container) {
        this(prevStepExecOrder, node, modelWrapper, container, null, null, null);
    }

    public ParsingContext(@Nonnull StepExecOrder prevStepExecOrder,
                          DomNode node,
                          @Nullable ModelWrapper<ModelT> modelWrapper,
                          @Nullable ContainerT container,
                          String parsedText,
                          String parsedURL,
                          StepExecOrder recursiveRootStepExecOrder) {
        this.prevStepExecOrder = Objects.requireNonNull(prevStepExecOrder);
        this.node = node;
        this.modelWrapper = modelWrapper;
        this.container = container;
        this.parsedText = parsedText;
        this.parsedURL = parsedURL;
        this.recursiveRootStepExecOrder = recursiveRootStepExecOrder;
    }

    public Optional<HtmlPage> getNodeAsHtmlPage() {
        HtmlPage page = node instanceof HtmlPage ? (HtmlPage) node : node.getHtmlPageOrNull();
        return Optional.ofNullable(page);
    }

    public Builder<ModelT, ContainerT> toBuilder() {
        return new Builder<>(this);
    }

    public static class Builder<ModelT, ContainerT> {

        private StepExecOrder prevStepExecOrder;
        private DomNode node;
        private ModelWrapper<ModelT> modelWrapper;
        private ContainerT container;
        private String parsedText;
        private String parsedURL;
        private StepExecOrder recursiveRootStepExecOrder;

        public Builder(StepExecOrder prevStepExecOrder, DomNode node, ModelWrapper<ModelT> modelWrapper, ContainerT container, String parsedText, String parsedURL,
                       StepExecOrder recursiveRootStepExecOrder) {
            this.prevStepExecOrder = prevStepExecOrder;
            this.node = node;
            this.modelWrapper = modelWrapper;
            this.container = container;
            this.parsedText = parsedText;
            this.parsedURL = parsedURL;
            this.recursiveRootStepExecOrder = recursiveRootStepExecOrder;
        }

        public Builder(ParsingContext<ModelT, ContainerT> ctx) {
            this(ctx.prevStepExecOrder, ctx.node, ctx.modelWrapper, ctx.container, ctx.parsedText, ctx.parsedURL, ctx.recursiveRootStepExecOrder);
        }

        public Builder<ModelT, ContainerT> setPrevStepOrder(StepExecOrder stepExecOrder) {
            this.prevStepExecOrder = stepExecOrder;
            return this;
        }

        public Builder<ModelT, ContainerT> setNode(DomNode node) {
            this.node = node;
            return this;
        }

        public Builder<ModelT, ContainerT> setModelProxy(ModelWrapper<ModelT> modelWrapper) {
            this.modelWrapper = modelWrapper;
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

        public Builder<ModelT, ContainerT> setRecursiveRootStepExecOrder(StepExecOrder stepExecOrder) {
            this.recursiveRootStepExecOrder = stepExecOrder;
            return this;
        }

        public ParsingContext<ModelT, ContainerT> build() {
            return new ParsingContext<>(prevStepExecOrder, node, modelWrapper, container, parsedText, parsedURL, recursiveRootStepExecOrder);
        }
    }
}
