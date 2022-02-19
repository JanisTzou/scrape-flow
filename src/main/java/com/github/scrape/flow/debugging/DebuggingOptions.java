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

package com.github.scrape.flow.debugging;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DebuggingOptions {

    private volatile boolean onlyScrapeFirstElements = false;
    private volatile boolean onlyScrapeNFirstElements = false;
    private volatile boolean logFoundElementsSource = false;
    private volatile boolean logSourceCodeOfLoadedPage = false;
    private volatile boolean logFoundElementsCount = false;

    private DebuggingOptions(DebuggingOptions d) {
        this(d.onlyScrapeFirstElements,
                d.onlyScrapeNFirstElements,
                d.logFoundElementsSource,
                d.logSourceCodeOfLoadedPage,
                d.logFoundElementsCount
        );
    }

    public DebuggingOptions copy() {
        return new DebuggingOptions(this);
    }
}
