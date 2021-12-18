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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;

/**
 * Allows sharing information between parsing steps
 */
@Getter
@Setter
@ToString
public class ParsingContext<ModelT, ContainerT> {

    private DomNode node;

    @Nullable
    private ModelProxy<ModelT> modelProxy;

    @Nullable
    private ContainerT container;

    private boolean collectorToParentModel;

    private String parsedText;

    public ParsingContext(DomNode node) {
        this(node, null, null, false);
    }

    public ParsingContext(DomNode node, @Nullable ModelProxy<ModelT> modelProxy, @Nullable ContainerT container, boolean collectorToParentModel) {
        this.node = node;
        this.modelProxy = modelProxy;
        this.container = container;
        this.collectorToParentModel = collectorToParentModel;
    }
}
