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

package com.github.web.scraping.lib.scraping.htmlunit;

import com.github.web.scraping.lib.parallelism.ParsedDataListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// models that are mean to be published to registered listeners ...
@RequiredArgsConstructor
@Getter
public class ModelToPublish {

    private final Object model;
    private final Class<?> modelClass;
    private final ParsedDataListener<Object> parsedDataListener;

}
