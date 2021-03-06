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

package com.github.scrape.flow.scraping.selenium;

import java.util.function.Function;

public class SeleniumFlow {


    public static class Get {

        public static SeleniumGetAncestor parent() {
            return new SeleniumGetAncestor(SeleniumGetAncestor.Type.PARENT);
        }

        public static SeleniumGetDescendants descendants() {
            return new SeleniumGetDescendants();
        }

    }


    public static class Do {

        public static SeleniumNavigateToParsedLink navigateToParsedLink() {
            return new SeleniumNavigateToParsedLink();
        }

    }


    public static class Parse {

        public static SeleniumParseElementHRef hRef() {
            return new SeleniumParseElementHRef();
        }

        public static SeleniumParseElementHRef hRef(Function<String, String> parsedValueMapper) {
            return new SeleniumParseElementHRef(parsedValueMapper);
        }

        public static SeleniumParseElementTextContent textContent() {
            return new SeleniumParseElementTextContent();
        }

    }


}
