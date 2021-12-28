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

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.web.scraping.lib.parallelism.StepExecOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

/**
 * Allows sharing information between parsing steps
 */
@Getter
@Setter
@ToString
@Log4j2
public class ScrapingContext {

    /**
     * StepOrder value at the previous level of step hierarchy (so the step calling this step)
     */
    @Nonnull
    private final StepExecOrder prevStepExecOrder;

    private DomNode node;

    // TODO very careful how this is shared and populated (needs to be copied always when going from one step to next ...)
    @Nonnull
    private ContextModels contextModels;

    private String parsedText;

    // should contain th efull URL so that it can be navigated to ...
    private String parsedURL;

    // used in special cases when we have recursive step execution and we always want to set the prev step to some initial value ...
    private StepExecOrder recursiveRootStepExecOrder;



    public ScrapingContext(StepExecOrder prevStepExecOrder, DomNode node) {
        this(prevStepExecOrder, node, new ContextModels());
    }

    public ScrapingContext(StepExecOrder prevStepExecOrder, DomNode node, ContextModels contextModels) {
        this(prevStepExecOrder, node, contextModels, null, null, null);
    }

    public ScrapingContext(@Nonnull StepExecOrder prevStepExecOrder,
                           DomNode node,
                           @Nonnull ContextModels contextModels,
                           String parsedText,
                           String parsedURL,
                           StepExecOrder recursiveRootStepExecOrder) {
        this.prevStepExecOrder = Objects.requireNonNull(prevStepExecOrder);
        this.node = node;
        this.contextModels = Objects.requireNonNull(contextModels);
        this.parsedText = parsedText;
        this.parsedURL = parsedURL;
        this.recursiveRootStepExecOrder = recursiveRootStepExecOrder;
    }

    public Optional<HtmlPage> getNodeAsHtmlPage() {
        HtmlPage page = node instanceof HtmlPage ? (HtmlPage) node : node.getHtmlPageOrNull();
        return Optional.ofNullable(page);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {

        private StepExecOrder prevStepExecOrder;
        private DomNode node;
        private final ContextModels contextModelsCopy;
        private String parsedText;
        private String parsedURL;
        private StepExecOrder recursiveRootStepExecOrder;

        public Builder(StepExecOrder prevStepExecOrder, DomNode node, ContextModels contextModelsCopy, String parsedText, String parsedURL,
                       StepExecOrder recursiveRootStepExecOrder) {
            this.prevStepExecOrder = prevStepExecOrder;
            this.node = node;
            this.contextModelsCopy = contextModelsCopy;
            this.parsedText = parsedText;
            this.parsedURL = parsedURL;
            this.recursiveRootStepExecOrder = recursiveRootStepExecOrder;
        }

        public Builder(ScrapingContext ctx) {
            this(ctx.prevStepExecOrder, ctx.node, ctx.contextModels.copy(), ctx.parsedText, ctx.parsedURL, ctx.recursiveRootStepExecOrder);
        }

        public Builder setPrevStepOrder(StepExecOrder stepExecOrder) {
            this.prevStepExecOrder = stepExecOrder;
            return this;
        }

        public Builder setNode(DomNode node) {
            this.node = node;
            return this;
        }

        public Builder addModel(Object model, Class<?> modelClass) {
            this.contextModelsCopy.push(model, modelClass);
            return this;
        }

        public Builder setParsedText(String parsedText) {
            this.parsedText = parsedText;
            return this;
        }

        public Builder setParsedURL(String parsedURL) {
            this.parsedURL = parsedURL;
            return this;
        }

        public Builder setRecursiveRootStepExecOrder(StepExecOrder stepExecOrder) {
            this.recursiveRootStepExecOrder = stepExecOrder;
            return this;
        }

        public ScrapingContext build() {
            return new ScrapingContext(prevStepExecOrder, node, contextModelsCopy, parsedText, parsedURL, recursiveRootStepExecOrder);
        }
    }
}
