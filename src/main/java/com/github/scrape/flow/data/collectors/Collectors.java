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

package com.github.scrape.flow.data.collectors;

import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@NoArgsConstructor
public class Collectors {

    private final List<Collector> list = new CopyOnWriteArrayList<>();

    public Collectors copy() {
        Collectors copy = new Collectors();
        copy.list.addAll(this.list);
        return copy;
    }

    public void add(Collector collector) {
        list.add(collector);
    }

    public List<Collector> getAll() {
        return Collections.unmodifiableList(list);
    }

    public List<Collector> getModelSuppliers() {
        return list.stream().filter(co -> co.getModelSupplier() != null).collect(java.util.stream.Collectors.toList());
    }

    public List<Collector> getAccumulators() {
        return list.stream().filter(co -> co.getAccumulator() != null).collect(java.util.stream.Collectors.toList());
    }

}
