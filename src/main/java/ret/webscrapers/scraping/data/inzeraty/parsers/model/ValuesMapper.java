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

package ret.webscrapers.scraping.data.inzeraty.parsers.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ValuesMapper {

    private static final Logger log = LogManager.getLogger();
    private static final Map<String, String> webToInternalValuesMap = new ConcurrentHashMap<>();

    static {
        store("Garsoniéra", "1+kk");
        store("Cihla", "Cihlová");
        store("Panel", "Panelová");
        store("přízemí", "1");
        store("přízemí (1. NP = nadzemní podlaží)", "1");
        store("snížené přízemí (1. PP = podzemní podlaží)", "-1");
        store("snížené", "-1");
        store("zvýšené přízemí (1. NP)", "1");
    }

    private static void store(String keyValue, String mappedValue) {
        webToInternalValuesMap.put(keyValue.toLowerCase(), mappedValue);
    }


    public static String mapValue(String value) {
        if (value == null) {
            return value;
        }

        String mappedValue = webToInternalValuesMap.get(value.toLowerCase());
        if (mappedValue != null) {
            log.info("Mapped value {} to {}", value, mappedValue);
            return mappedValue;
        } else {
            return value;
        }
    }

}
