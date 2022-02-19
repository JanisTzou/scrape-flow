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

package com.github.scrape.flow.scraping;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("UnusedReturnValue")
@RequiredArgsConstructor
public class ConfigurableScraping {

    private final Scraping scraping;

    /**
     * limit the number of retries for a request when it fails (e.g. when loading a new page)
     * @return reference to this instance
     */
    public Scraping setMaxRequestRetries(int max) {
        scraping.getServices().getOptions().setMaxRequestRetries(max);
        return scraping;
    }

    public Scraping setIgnoreDuplicateURLs(boolean enabled) {
        scraping.getServices().getOptions().setIgnoreDuplicateURLs(enabled);
        return scraping;
    }

}
