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

import com.github.web.scraping.lib.dom.data.parsing.steps.ModelProxy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;

@RequiredArgsConstructor
@Getter
@ToString
public class ParsedElement implements StepResult { // TODO rename to parsed element? parsed field ? ...

    private final Enum<?> identifier;

    // TODO sometimes the NEXT stage might not have a href but a JS triggered button ...
    private final String href;

    private final String text;

    private final boolean hasHRef;

    // TODO make properly iomplementated later ...
    @Nullable
    @Setter
    private ModelProxy<Object> modelProxy;

    private final Object element;

}
