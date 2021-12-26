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

package com.github.web.scraping.lib.debugging;

public class Debugging {

    private volatile boolean onlyScrapeFirstElements = false;
    private volatile boolean onlyScrapeNFirstElements = false;
    private volatile boolean logSourceCodeOfFoundElements = false;
    private volatile boolean logSourceCodeOfLoadedPage = false;
    // TODO only single pagination => one page change ... not all if there are many ...?
    // TOOD limit pagination count? Actually a similar option might be useful for real prunning ...

    public Debugging() {
    }

    public Debugging(Debugging d) {
        this(d.onlyScrapeFirstElements,
                d.onlyScrapeNFirstElements,
                d.logSourceCodeOfFoundElements,
                d.logSourceCodeOfLoadedPage
        );
    }

    private Debugging(boolean onlyScrapeFirstElements,
                      boolean onlyScrapeNFirstElements,
                      boolean logSourceCodeOfFoundElements,
                      boolean logSourceCodeOfLoadedPage) {
        this.onlyScrapeFirstElements = onlyScrapeFirstElements;
        this.onlyScrapeNFirstElements = onlyScrapeNFirstElements;
        this.logSourceCodeOfFoundElements = logSourceCodeOfFoundElements;
        this.logSourceCodeOfLoadedPage = logSourceCodeOfLoadedPage;
    }

    /**
     * When we have a long sequence, that we want to test, this will traverse the scraping sequence by always using
     * using the first found element and continuing from there - and ignore all other found elements in the same step f there were many
     */
    public Debugging onlyScrapeFirstElements(boolean enabled) {
        this.onlyScrapeFirstElements = enabled;
        return this;
    }

    /**
     * This turns on logging of the XML for all found elements
     */
    public Debugging logSourceCodeOfFoundElements(boolean enabled) {
        this.logSourceCodeOfFoundElements = enabled;
        return this;
    };

    public boolean isOnlyScrapeFirstElements() {
        return onlyScrapeFirstElements;
    }

    public boolean isLogSourceCodeOfFoundElements() {
        return logSourceCodeOfFoundElements;
    }

    public Debugging copy() {
        return new Debugging(this);
    }
}
