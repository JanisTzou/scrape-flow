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

package com.github.web.scraping.lib.parallelism;

import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Log4j2
public class NotificationService {

    private final NotificationOrderingService orderingService = null;

    private final StepAndDataRelationshipTracker stepAndDataRelationshipTracker;

    private final Set<Object> published = Collections.synchronizedSet(new HashSet<>()); // TODO temporary ...

    public NotificationService(StepAndDataRelationshipTracker stepAndDataRelationshipTracker) {
        this.stepAndDataRelationshipTracker = stepAndDataRelationshipTracker;
    }


    public void notifyAfterStepFinished(StepExecOrder stepExecOrder) {
        // TODO there is a problem somewhere, some data are not getting published ever ...
        List<StepAndDataRelationshipTracker.FinalizedModel> finalizedData = stepAndDataRelationshipTracker.getModelsWithNoActiveSteps(stepExecOrder);
        if (!finalizedData.isEmpty()) {
            for (StepAndDataRelationshipTracker.FinalizedModel data : finalizedData) {
                log.debug("{} has finalized data of type '{}'", stepExecOrder, data.getModel().getClass().getSimpleName());
                if (data.getModelListener() != null && !published.contains(data.getModel())) { // TODO temporary ...
                    published.add(data.getModel());
                    log.debug("{} About to publish data to listener after step finished", stepExecOrder);
                    data.getModelListener().onParsingFinished(data.getModel());
                }
            }
        }
    }

}
