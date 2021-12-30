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

package com.github.web.scraping.lib.scraping;

public class DebuggableScraping {

    private final Scraping scraping;

    public DebuggableScraping(Scraping scraping) {
        this.scraping = scraping;
    }

    /**
     * Useful when we want to test the scraping sequence but want to globally limit the number of elements scraped.
     * Only first elements found at each step will be used for the execution of subsequent steps. If there are any filters applied at a step,
     * then those will be applied first and the first element of the resulting element list will be taken.
     * @return reference to this instance
     */
    public Scraping setOnlyScrapeFirstElements(boolean enabled) {
        scraping.getServices().getGlobalDebugging().setOnlyScrapeFirstElements(enabled);
        return scraping;
    }

    /**
     * Logs the source code of each found element as XML
     * @return @return reference to this instance
     */
    public Scraping setLogFoundElementsSource(boolean enabled) {
        scraping.getServices().getGlobalDebugging().setLogFoundElementsSource(enabled);
        return scraping;
    }

}
