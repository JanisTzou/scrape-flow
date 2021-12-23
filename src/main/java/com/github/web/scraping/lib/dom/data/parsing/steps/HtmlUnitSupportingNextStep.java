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

public interface HtmlUnitSupportingNextStep<C>  {

    /**
     * <p>Specifies the next step to execute after the step that the method is called on finishes.
     *
     * @return reference to the same object on which the method was called - this allows chaining multiple following steps to execute when that step finishes.
     * <p>
     * <b>Importantly</b>, this type of chaining does not mean that the declared steps will execute sequentially in the order of declaration of <code>next()</code> method calls.
     * The declared steps will execute asynchronously, and they will interleave. Nonetheless, ordering is still handled internally and the order of parsed data
     * is guaranteed when the client code publishes the output to registered listeners.
     * <br>
     * <br>
     * <p>To enforce the execution order to be the same as the order of step declaration use {@link HtmlUnitSupportingNextStep#nextExclusively(HtmlUnitParsingStep)} - this can be used for  any steps needed.
     *
     */
    C next(HtmlUnitParsingStep<?> nextStep);

    /**
     * This method guarantees that the step sequence specified as a parameter will fully finish before any other steps declared below this step start being executed.
     * <br>
     * <br>
     * This is useful in specific situations e.g. when we need to ensure that all data is parsed in certain steps because the data gets propagated to following parsing steps.
     * If this method is not used, then in the context of parallel execution the propagated data might not be fully populated with parsed data at the moment they are published to clients via register listeners.
     * <br>
     * <br>
     * Note that this method reduces the achieved parallelism.
     *
     * @return reference to the same object on which the method was called - this allows chaining multiple following steps to execute when that step finishes.
     */
    C nextExclusively(HtmlUnitParsingStep<?> nextStep);

}
