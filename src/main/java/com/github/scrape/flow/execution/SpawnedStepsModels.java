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

package com.github.scrape.flow.execution;

import com.github.scrape.flow.data.publishing.ModelToPublish;
import com.github.scrape.flow.scraping.SpawnedSteps;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Children spawned by the owning step to which a data model was published.
 * They are the steps directly below the owning step
 */
@Getter
@ToString
public class SpawnedStepsModels extends SpawnedSteps {

    /**
     * Data modified by the active steps which we wanna publish to registered listeners when all the steps finish
     */
    private final List<ModelToPublish> modelToPublishList;

    public SpawnedStepsModels(StepOrder parent, List<StepOrder> steps, List<ModelToPublish> modelToPublishList) {
        super(parent, steps);
        this.modelToPublishList = modelToPublishList;
    }

}
