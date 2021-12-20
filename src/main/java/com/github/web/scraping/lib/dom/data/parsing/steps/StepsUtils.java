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

import lombok.extern.log4j.Log4j2;

import java.util.Set;

@Log4j2
public class StepsUtils {

    // TODO not ideal ... especially that it has to be called repeatedly when e.g. in class Paginate ... if steps have reference to parents then we do not have to do this ... we can propagate it upwards to parents ...
    public static void propagateServicesRecursively(HtmlUnitParsingStep<?> nextStep, CrawlingServices services, Set<HtmlUnitParsingStep<?>> visited) {
        if (visited.contains(nextStep)) {
            throw new IllegalStateException("Circular step dependencies detected for step: " + nextStep.getName());
        }
        nextStep.setServices(services);
        visited.add(nextStep);
        for (HtmlUnitParsingStep<?> ns : nextStep.nextSteps) {
            propagateServicesRecursively(ns, services, visited);
        }
    }

}
