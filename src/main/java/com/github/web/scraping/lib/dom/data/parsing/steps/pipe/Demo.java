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

import static com.github.web.scraping.lib.demos.TeleskopExpressDeCrawler.Product;
import static com.github.web.scraping.lib.demos.TeleskopExpressDeCrawler.ProductCode;

public class Demo {

    public static void main(String[] args) {

        new GetElementsByCssClassPipe("class_name")
                .collector(ProductCode::new, Product::setProductCode)
                .then(new ParseElementTextPipe()
                        .then(new ParseElementTextPipe()));

    }

}