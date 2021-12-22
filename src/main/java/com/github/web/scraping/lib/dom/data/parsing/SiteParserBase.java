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

import com.github.web.scraping.lib.dom.data.parsing.steps.ScrapingServices;
import com.github.web.scraping.lib.drivers.DriverManager;
import lombok.RequiredArgsConstructor;

/**
 * Parses data from a given site/URL based on provided parsingStrategies
 */
@RequiredArgsConstructor
public abstract class SiteParserBase<T> implements SiteParserInternal<T> {

    protected final DriverManager<T> driverManager;

    protected ScrapingServices services;

    // only for internal uses ...
    public void setServicesInternal(ScrapingServices services) {
        this.services = services;
    }

    // TODO create specific implementations for HtmlUnit and Selenium ...

}
