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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class Collecting<R, T> {

    private final Supplier<T> modelSupplier;
    private final Supplier<R> containerSupplier;

    @Getter
    private final BiConsumer<R, T> accumulator;

    public Collecting() {
        this(null, null, null);
    }


    public Optional<R> supplyContainer() {
        final R container;
        if (containerSupplier != null && accumulator != null) {
            return Optional.ofNullable(containerSupplier.get());
        } else {
            return Optional.empty();
        }
    }

    public Optional<T> supplyModel() {
        if (modelSupplier != null) {
            return Optional.of(modelSupplier.get());
        } else {
            return Optional.empty();
        }
    }

}
