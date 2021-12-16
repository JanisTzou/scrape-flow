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

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsedElement;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class ParseElementHRef extends HtmlUnitParsingStep {

    private final Enum<?> identifier;
    // TODO support next strategies ...
    // TODO add some filtering logic for the hrefs parsed ...

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Enum<?> identifier) {
        return new Builder().setId(identifier);
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        if (ctx.getNode() instanceof HtmlAnchor anch) {
            String href = anch.getHrefAttribute();
            if (href != null) {
                return List.of(new ParsedElement(identifier, href, null, true, ctx.getNode()));
            }
        }
        return Collections.emptyList();
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

        public ParseElementHRef build() {
            return new ParseElementHRef(identifier);
        }
    }

}
