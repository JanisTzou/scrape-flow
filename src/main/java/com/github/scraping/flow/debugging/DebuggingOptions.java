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

package com.github.scraping.flow.debugging;

public class DebuggingOptions {

    private volatile boolean onlyScrapeFirstElements = false;
    private volatile boolean onlyScrapeNFirstElements = false;
    private volatile boolean logFoundElementsSource = false;
    private volatile boolean logSourceCodeOfLoadedPage = false;
    private volatile boolean logFoundElementsCount = false;

    public DebuggingOptions() {
    }

    public DebuggingOptions(DebuggingOptions d) {
        this(d.onlyScrapeFirstElements,
                d.onlyScrapeNFirstElements,
                d.logFoundElementsSource,
                d.logSourceCodeOfLoadedPage,
                d.logFoundElementsCount
        );
    }

    private DebuggingOptions(boolean onlyScrapeFirstElements,
                             boolean onlyScrapeNFirstElements,
                             boolean logFoundElementsSource,
                             boolean logSourceCodeOfLoadedPage,
                             boolean logFoundElementsCount) {
        this.onlyScrapeFirstElements = onlyScrapeFirstElements;
        this.onlyScrapeNFirstElements = onlyScrapeNFirstElements;
        this.logFoundElementsSource = logFoundElementsSource;
        this.logSourceCodeOfLoadedPage = logSourceCodeOfLoadedPage;
        this.logFoundElementsCount = logFoundElementsCount;
    }

    /**
     * When we have a long sequence, that we want to test, this will traverse the scraping sequence by always using
     * the first found element and continuing from there - and ignore all other found elements in the same step f there were many
     */
    public void setOnlyScrapeFirstElements(boolean enabled) {
        this.onlyScrapeFirstElements = enabled;
    }

    /**
     * This turns on logging of the XML for found elements
     */
    public void setLogFoundElementsSource(boolean enabled) {
        this.logFoundElementsSource = enabled;
    }

    /**
     * This turns on logging count found elements
     */
    public void setLogFoundElementsCount(boolean enabled) {
        this.logFoundElementsCount = enabled;
    }

    public boolean isOnlyScrapeFirstElements() {
        return onlyScrapeFirstElements;
    }

    public boolean isLogFoundElementsSource() {
        return logFoundElementsSource;
    }

    public boolean isLogFoundElementsCount() {
        return logFoundElementsCount;
    }

    public DebuggingOptions copy() {
        return new DebuggingOptions(this);
    }
}
