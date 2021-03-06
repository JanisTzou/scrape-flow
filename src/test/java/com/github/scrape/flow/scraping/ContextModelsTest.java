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

import com.github.scrape.flow.data.collectors.ModelWrapper;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContextModelsTest {

    @Test
    public void getModelFor() {

        ContextModels models = new ContextModels();

        String strModel = "test";
        models.add(strModel, String.class);
        Object objModel = new Object();
        models.add(objModel, Object.class);

        Optional<ModelWrapper> modelWrapper = models.getModelFor(String.class);

        assertTrue(modelWrapper.isPresent());
        assertEquals(strModel, modelWrapper.get().getModel());

    }
}
