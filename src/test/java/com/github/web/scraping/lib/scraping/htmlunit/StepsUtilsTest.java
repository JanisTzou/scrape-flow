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

import com.github.web.scraping.lib.scraping.htmlunit.Paginate;
import com.github.web.scraping.lib.scraping.htmlunit.ReturnNextPage;
import com.github.web.scraping.lib.scraping.htmlunit.StepsUtils;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class StepsUtilsTest {

    @Test
    public void findStepOfTypeInSequence() {

        Paginate sequence = new Paginate().next(new ReturnNextPage());

        Optional<ReturnNextPage> returnNextPageStep = StepsUtils.findStepOfTypeInSequence(sequence, ReturnNextPage.class);

        assertTrue(returnNextPageStep.isPresent());

    }
}
