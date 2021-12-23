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

import java.util.function.Function;

public class ParseData {

    public static ParseElementTextContent parseTextContent() {
        return new ParseElementTextContent();
    }

    public static ParseElementTextContent parseTextContent(Function<String, String> parsedTextTransformation) {
        return new ParseElementTextContent().setTransformation(parsedTextTransformation);
    }

    public static ParseElementHRef parseHRef() {
        return new ParseElementHRef();
    }

    public static ParseElementHRef parseHRef(Function<String, String> parsedTextTransformation) {
        return new ParseElementHRef(parsedTextTransformation);
    }

    // TODO attribute value etc ...

}
