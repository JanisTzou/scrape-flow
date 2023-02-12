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

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.selenium.SeleniumUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

/**
 * Allows sharing information between parsing steps
 * Very careful how this is shared and populated (needs to be copied always when going from one step to next ...)
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
    private final StepOrder prevStepOrder;

    // for HtmlUnit
    private DomNode node;

    // for Selenium
    private WebElement webElement;
    private String webElementXPath;

    @Nonnull
    private ContextModels contextModels;

    // should contain the full URL so that it can be navigated to ...
    private String parsedURL;

    // used in special cases when we have looped step execution and we always want to set the prev step to some initial value ...
    private StepOrder rootLoopedStepOrder;

    public ScrapingContext(StepOrder prevStepOrder) {
        this(prevStepOrder, null, null, new ContextModels(), null, null);
    }

    public ScrapingContext(StepOrder prevStepOrder, DomNode node) {
        this(prevStepOrder, node, new ContextModels());
    }

    public ScrapingContext(StepOrder prevStepOrder, DomNode node, ContextModels contextModels) {
        this(prevStepOrder, node, null, contextModels, null, null);
    }

    public ScrapingContext(@Nonnull StepOrder prevStepOrder,
                           DomNode node,
                           WebElement webElement,
                           @Nonnull ContextModels contextModels,
                           String parsedURL,
                           StepOrder rootLoopedStepOrder) {
        this.prevStepOrder = Objects.requireNonNull(prevStepOrder);
        this.node = node;
        this.webElement = webElement;
//        if (webElement != null) {
//            long start = System.currentTimeMillis();
//            this.webElementXPath = SeleniumUtils. generateXPath(webElement, "");
//            long end = System.currentTimeMillis();
//            System.out.println("duration: " + (end - start));
//        }
        this.contextModels = Objects.requireNonNull(contextModels);
        this.parsedURL = parsedURL;
        this.rootLoopedStepOrder = rootLoopedStepOrder;
    }

    public Optional<HtmlPage> getNodeAsHtmlPage() {
        HtmlPage page = node instanceof HtmlPage ? (HtmlPage) node : node.getHtmlPageOrNull();
        return Optional.ofNullable(page);
    }

//    public WebElement getWebElement() {
//        try {
//            System.out.println("call");
//            return webElement.findElement(By.xpath(webElementXPath));
//        } catch (Exception e) {
//            return webElement;
//        }
//    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {

        private StepOrder prevStepOrder;
        private DomNode node;
        private WebElement webElement;
        private final ContextModels contextModelsCopy;
        private String parsedURL;
        private StepOrder recursiveRootStepOrder;

        private Builder(StepOrder prevStepOrder,
                        DomNode node,
                        WebElement webElement,
                        ContextModels contextModelsCopy,
                        String parsedURL,
                        StepOrder recursiveRootStepOrder) {
            this.prevStepOrder = prevStepOrder;
            this.node = node;
            this.webElement = webElement;
            this.contextModelsCopy = contextModelsCopy;
            this.parsedURL = parsedURL;
            this.recursiveRootStepOrder = recursiveRootStepOrder;
        }

        private Builder(ScrapingContext ctx) {
            this(ctx.prevStepOrder, ctx.node, ctx.webElement, ctx.contextModels.copy(), ctx.parsedURL, ctx.rootLoopedStepOrder);
        }

        public Builder setPrevStepOrder(StepOrder stepOrder) {
            this.prevStepOrder = stepOrder;
            return this;
        }

        public Builder setNode(DomNode node) {
            this.node = node;
            return this;
        }

        public Builder setWebElement(WebElement webElement) {
            this.webElement = webElement;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder addModel(Object model, Class<?> modelClass) {
            this.contextModelsCopy.add(model, modelClass);
            return this;
        }

        public Builder setParsedURL(String parsedURL) {
            this.parsedURL = parsedURL;
            return this;
        }

        public Builder setRecursiveRootStepOrder(StepOrder stepOrder) {
            this.recursiveRootStepOrder = stepOrder;
            return this;
        }

        public ScrapingContext build() {
            return new ScrapingContext(prevStepOrder, node, webElement, contextModelsCopy, parsedURL, recursiveRootStepOrder);
        }
    }
}
