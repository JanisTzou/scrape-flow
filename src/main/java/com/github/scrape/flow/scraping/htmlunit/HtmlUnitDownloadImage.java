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

package com.github.scrape.flow.scraping.htmlunit;

import com.github.scrape.flow.clients.ClientReservationType;
import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.*;
import lombok.extern.log4j.Log4j2;

import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

import static com.github.scrape.flow.data.collectors.Collector.AccumulatorType;

@Log4j2
public class HtmlUnitDownloadImage extends HtmlUnitScrapingStep<HtmlUnitDownloadImage>
        implements CollectingParsedValueToModelStep<HtmlUnitDownloadImage, BufferedImage>,
        MakingHttpRequests {

    HtmlUnitDownloadImage() {
    }

    @Override
    protected HtmlUnitDownloadImage copy() {
        return copyFieldValuesTo(new HtmlUnitDownloadImage());
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextAfter(ctx.getPrevStepOrder());
        Runnable runnable = new HtmlUnitDownloadImageRunnable(ctx, stepOrder, getHelper(services), getName(), getCollectors());
        submitForExecution(stepOrder, runnable, services);
        return stepOrder;
    }


    @Override
    public <T> HtmlUnitDownloadImage collectValue(BiConsumer<T, BufferedImage> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, BufferedImage.class, containerType, AccumulatorType.ONE));
    }

    @Override
    public <T> HtmlUnitDownloadImage collectValues(BiConsumer<T, BufferedImage> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, BufferedImage.class, containerType, AccumulatorType.MANY));
    }

    @Override
    protected ClientReservationType getClientReservationType() {
        return ClientReservationType.READING;
    }


}
