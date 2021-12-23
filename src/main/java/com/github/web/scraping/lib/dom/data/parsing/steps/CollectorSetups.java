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

package com.github.web.scraping.lib.dom.data.parsing.steps;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CollectorSetups {

    private final List<CollectorSetup> operations = new CopyOnWriteArrayList<>();

    public void add(CollectorSetup operation) {
        operations.add(operation);
    }

    public List<CollectorSetup> getAll() {
        return Collections.unmodifiableList(operations);
    }

    public List<CollectorSetup> getModelSuppliers() {
        return operations.stream().filter(co -> co.getModelSupplier() != null).collect(Collectors.toList());
    }

    public List<CollectorSetup> getAccumulators() {
        return operations.stream().filter(co -> co.getAccumulator() != null).collect(Collectors.toList());
    }

}
