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
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.web.scraping.lib.dom.data.parsing.ElementClicked;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

// TODO maybe we should express better that we expect the next page here ... or maybe in a new step ...
 public class ClickElement extends HtmlUnitParsingStep<ClickElement> implements HtmlUnitChainableStep<ClickElement> {

    public ClickElement(List<HtmlUnitParsingStep<?>> nextSteps) {
        super(nextSteps);
    }

    public ClickElement() {
        this(null);
    }

    public static ClickElement instance() {
        return new ClickElement();
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

    @Override
    public ClickElement then(HtmlUnitParsingStep nextStep) {
        this.nextSteps.add(nextStep);
        return this;
    }

}
