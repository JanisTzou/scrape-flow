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

package com.github.scrape.flow;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.junit.Test;

import java.util.Map;
import java.util.SortedMap;

import static org.junit.Assert.assertEquals;

public class PatritiaTreeExperiments {

    @Test
    public void experiments() {

        final Trie<String, String> patriciaTrie = new PatriciaTrie<>();

        final String key0 = "hi";
        final String key1 = "hi there!";
        final String key2 = "hi here!";
        final String key3 = "there!";

        patriciaTrie.put(key0, key0);
        patriciaTrie.put(key1, key1);
        patriciaTrie.put(key2, key2);
        patriciaTrie.put(key3, key3);

        SortedMap<String, String> prefixMap;

        // example 1

        prefixMap = patriciaTrie.prefixMap("hi");

        assertEquals(3, prefixMap.size());
        assertEquals(Map.of(key0, key0, key1, key1, key2, key2), prefixMap);

        // example 2

        prefixMap = patriciaTrie.prefixMap("hi th");

        assertEquals(1, prefixMap.size());
        assertEquals(Map.of(key1, key1), prefixMap);

        // example 3

        prefixMap = patriciaTrie.prefixMap("th");

        assertEquals(1, prefixMap.size());
        assertEquals(Map.of(key3, key3), prefixMap);

        // example 4

        patriciaTrie.remove("hi");

        prefixMap = patriciaTrie.prefixMap("hi");

        assertEquals(2, prefixMap.size());



    }
}
