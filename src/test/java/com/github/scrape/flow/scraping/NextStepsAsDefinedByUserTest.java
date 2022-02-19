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

import com.github.scrape.flow.execution.StepOrder;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.*;

public class NextStepsAsDefinedByUserTest extends NextStepsHandlerTestBase {

    @Test
    public void eachStepIsExecuted() {
        NextStepsAsDefinedByUser nextStepsHandler = new NextStepsAsDefinedByUser();
        nextStepsHandler.execute(StepOrder.INITIAL, steps, context, services);

        steps.forEach(step -> verify(step, times(1)).execute(context, services));

    }

}
