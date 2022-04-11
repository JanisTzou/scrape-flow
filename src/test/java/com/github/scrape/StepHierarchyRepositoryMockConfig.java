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

package com.github.scrape;

import com.github.scrape.flow.execution.*;
import com.github.scrape.flow.scraping.ScrapingStep;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@Configuration
public class StepHierarchyRepositoryMockConfig {

    @Bean
    public StepHierarchyRepository stepHierarchyRepository() {
        StepHierarchyRepository mock = mock(StepHierarchyRepository.class);
        when(mock.getMetadataFor(any(ScrapingStep.class))).thenReturn(mock(StepMetadata.class));
        return mock;
    }

}
