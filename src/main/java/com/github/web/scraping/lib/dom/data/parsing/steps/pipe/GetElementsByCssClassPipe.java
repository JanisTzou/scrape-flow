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

package com.github.web.scraping.lib.dom.data.parsing.steps.pipe;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.web.scraping.lib.dom.data.parsing.ParsingContext;
import com.github.web.scraping.lib.dom.data.parsing.StepResult;
import com.github.web.scraping.lib.dom.data.parsing.steps.*;
import com.github.web.scraping.lib.scraping.utils.HtmlUnitUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class GetElementsByCssClassPipe extends HtmlUnitParsingStepPipe {

    private final String cssClassName;

    public GetElementsByCssClassPipe(String cssClassName) {
        this.cssClassName = cssClassName;
    }

    @Override
    public List<StepResult> execute(ParsingContext ctx) {
        Supplier<List<DomNode>> nodesSearch = () -> HtmlUnitUtils.getAllChildElementsByClass(ctx.getNode(), cssClassName);
        // TODO what to do here ....
        return Collections.emptyList();
    }


}
