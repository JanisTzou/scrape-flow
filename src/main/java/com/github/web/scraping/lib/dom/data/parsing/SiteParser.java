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

import com.github.web.scraping.lib.drivers.DriverManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

// TODO consider naming this something like ParserManager and the parsing strategire just "Parser"
/**
 * Parses data from a given site/URL based on provided parsingStrategies
 */
@RequiredArgsConstructor
public abstract class SiteParser<T> {

    protected final DriverManager<T> driverManager;

    // TODO how to organize the results? we might have tabular data and need to distinguish between different rows ...
    //  also there might be some shared data ...
    //  we should be able to assign group numbers
    public abstract List<ParsedElement> parse(String url);

    // TODO create specific implementations for HtmlUnit and Selenium ...

}
