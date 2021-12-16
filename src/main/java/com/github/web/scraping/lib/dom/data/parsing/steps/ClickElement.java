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

import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.*;
import com.github.web.scraping.lib.dom.data.parsing.ElementClicked;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO maybe we should express better that we expect the next page here ...
@RequiredArgsConstructor
public class ClickElement extends HtmlUnitParsingStep {

    private final Enum<?> dataType;
    // TODO support next strategies ...

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Enum<?> identifier) {
        return new Builder().setId(identifier);
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        // TODO clean this mess ...
        if (ctx.getNode() instanceof HtmlAnchor anch) {
            try {
                HtmlPage page = anch.getHtmlPageOrNull();
                WebWindow enclosingWindow = page.getEnclosingWindow();
                anch.click();
                HtmlPage currPage = (HtmlPage) enclosingWindow.getEnclosedPage();
//                System.out.println(currPage.asXml());
                return List.of(new ElementClicked(anch, currPage));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    private String removeNestedElementsTextContent(String textContent, HtmlElement el) {
        for (DomElement childElement : el.getChildElements()) {
            textContent = textContent.replace(childElement.getTextContent(), "");
        }
        return textContent;
    }


    public static class Builder {

        // TODO use this one as well ...
        private final List<HtmlUnitParsingStep> nextSteps = new ArrayList<>();
        private Enum<?> identifier;

        public Builder setId(Enum<?> identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder then(HtmlUnitParsingStep nextStep) {
            this.nextSteps.add(nextStep);
            return this;
        }

        public ClickElement build() {
            return new ClickElement(identifier);
        }
    }

}
