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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class ParsedElement implements StepResult { // TODO rename to parsed element? parsed field ? ...


    /*
    TODO think about all the stuff that we might want here ...
        ... href, text content ... what else ? How could we get this in a concatenated fashion so we have very isolated well defined operations as building blocks ...?
     */



    private final Enum<?> identifier;
    private final String href;  // TODO somehow from this href we might need to make another full URLs ...
    // TODO sometimes the NEXT stage might not have a href but a JS triggered button ...

    // TODO include element for debugging ?

    //    private final boolean shouldScrapeNext; // TODO should this be here ?
    private final String text;

    private final Object element;
    // can contain image also ...

    public String info() {
        return "identifier=" + identifier +
                ", text='" + text + "'";
    }



}
