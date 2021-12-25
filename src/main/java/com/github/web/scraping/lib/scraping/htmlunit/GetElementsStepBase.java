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

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.web.scraping.lib.scraping.htmlunit.GetElementsStepBase.TraversalFilterOption.*;

// TODO we should not apply this to GetElementsByDomTraversal ... we would be adding the same functionality to them ... as they already provide ...
@Log4j2
public abstract class GetElementsStepBase<C extends GetElementsStepBase<C>> extends CommonOperationsStepBase<C> {

    private int traversalFilterParam;
    private TraversalFilterOption traversalFilterOption = NO_FILTER;

    public GetElementsStepBase(List<HtmlUnitScrapingStep<?>> nextSteps) {
        super(nextSteps);
    }

    void setTraversalFilterOption(TraversalFilterOption traversalFilterOption) {
        this.traversalFilterOption = traversalFilterOption;
    }

    /**
     * @return copy of this step
     */
    public C getFirst() {
        return copyAndSetOption(FIRST);
    }

    /**
     * @return copy of this step
     */
    public C getLast() {
        return copyAndSetOption(LAST);
    }

    private C copyAndSetOption(TraversalFilterOption option) {
        return copyThisMutateAndGet(copy -> {
            copy.setTraversalFilterOption(option);
            return copy;
        });
    }

    protected <T> List<T> filterByTraverseOption(List<T> nodes) {
        switch (traversalFilterOption) {
            case NO_FILTER:
                return nodes;
            case FIRST:
                return nodes.stream().findFirst().stream().toList();
            case LAST: {
                List<T> copy = new ArrayList<>(nodes);
                Collections.reverse(copy);
                return copy.stream().findFirst().stream().toList();
            }
            default:
                log.error("Missing handling for traversalFilterOption {}", traversalFilterOption);
                return Collections.emptyList();
        }
    }


    @SuppressWarnings("unchecked")
    protected C copyFieldValuesTo(GetElementsStepBase<?> other) {
        super.copyFieldValuesTo(other);
        other.traversalFilterParam = this.traversalFilterParam;
        other.traversalFilterOption = this.traversalFilterOption;
        return (C) other;
    }

    // TODO create more ...

    protected enum TraversalFilterOption {
        NO_FILTER,
        FIRST,
        LAST
    }

}
