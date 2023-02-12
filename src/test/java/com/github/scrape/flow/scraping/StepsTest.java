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

import org.junit.Test;

import static org.junit.Assert.*;

public class StepsTest {

    @Test
    public void test() {

        Steps.add(2, 1);
        Steps.add(3, 2);

        Steps.add(5, 4);
        Steps.add(6, 5);

        Steps.add(4, 3);

        System.out.println("end");

    }
}
