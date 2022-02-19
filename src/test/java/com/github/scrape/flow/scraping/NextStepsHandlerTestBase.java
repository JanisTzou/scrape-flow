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

import java.util.List;

import static org.mockito.Mockito.mock;

public abstract class NextStepsHandlerTestBase {

    protected ScrapingStep<?> step1 = mock(ScrapingStep.class);
    protected ScrapingStep<?> step2 = mock(ScrapingStep.class);
    protected List<ScrapingStep<?>> steps = List.of(step1, step2);
    protected ScrapingContext context = mock(ScrapingContext.class);
    protected ScrapingServices services = mock(ScrapingServices.class);

}
