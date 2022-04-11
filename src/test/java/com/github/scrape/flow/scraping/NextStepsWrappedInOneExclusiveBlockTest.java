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
import com.github.scrape.flow.scraping.htmlunit.HtmlUnitStepBlock;
import org.junit.Test;
import org.mockito.Mockito;

import static com.github.scrape.flow.scraping.NextStepsWrappedInOneExclusiveBlock.ExclusiveBlockFactory;
import static org.mockito.Mockito.*;

public class NextStepsWrappedInOneExclusiveBlockTest extends NextStepsHandlerTestBase {

    @Test
    public void stepsAreWrappedInStepBlockWhichGetsExecuted() {

        ExclusiveBlockFactory bfMock = mock(ExclusiveBlockFactory.class);
        HtmlUnitStepBlock sbMock = mock(HtmlUnitStepBlock.class);
        when(bfMock.wrapInExclusiveBlock(anyList())).thenReturn(sbMock);
        StepOrder stepOrder = StepOrder.from(1, 1);
        when(sbMock.execute(context, services)).thenReturn(stepOrder);

        NextStepsWrappedInOneExclusiveBlock nextStepsHandler = new NextStepsWrappedInOneExclusiveBlock(bfMock);
        nextStepsHandler.execute(steps, StepOrder.INITIAL, context, services);

        steps.forEach(Mockito::verifyNoInteractions);
        verify(sbMock, times(1)).execute(context, services);
    }

}
