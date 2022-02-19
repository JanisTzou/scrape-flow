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

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Options {

    private static final int REQUEST_RETRIES_MAX_DEFAULT = 1;

    private volatile int maxRequestRetries;
    private volatile boolean ignoreDuplicateURLs;  // TODO implement logic to actually ignore duplicates ...

    public Options() {
        this(REQUEST_RETRIES_MAX_DEFAULT, false);
    }

    public Options(Options options) {
        this(options.maxRequestRetries, options.ignoreDuplicateURLs);
    }

    public Options copy() {
        return new Options(this);
    }

}
