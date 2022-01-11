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

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.github.scrape.flow.data.collectors.Collector;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.MakingHttpRequests;
import com.github.scrape.flow.scraping.ScrapingServices;
import com.github.scrape.flow.scraping.Throttling;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.github.scrape.flow.data.collectors.Collector.AccumulatorType;

@Log4j2
public class DownloadImage extends CommonOperationsStepBase<DownloadImage>
        implements CollectingParsedValueToModelStep<DownloadImage, BufferedImage>,
        Throttling, MakingHttpRequests {

    DownloadImage() {
    }

    @Override
    protected DownloadImage copy() {
        return copyFieldValuesTo(new DownloadImage());
    }

    @Override
    protected StepOrder execute(ScrapingContext ctx, ScrapingServices services) {
        StepOrder stepOrder = services.getStepOrderGenerator().genNextOrderAfter(ctx.getPrevStepOrder());

        Runnable runnable = () -> {
            Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode());

            URL imageURL = null;
            try {
                imageURL = new URL(ctx.getParsedURL());
                BufferedImage bufferedImage = ImageIO.read(imageURL);
                setParsedValueToModel(this.getCollectors(), ctx, bufferedImage, getName(), stepDeclarationLine);

                log.debug("Success downloading image");
            } catch (Exception e) {
                log.error("Error downloading image from URL {}", ctx.getParsedURL());
            }

            getHelper().execute(ctx, nodesSearch, stepOrder, getExecuteIf(), services);
        };

        submitForExecution(stepOrder, runnable, services.getTaskService());

        return stepOrder;
    }


    @Override
    public <T> DownloadImage collectOne(BiConsumer<T, BufferedImage> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, BufferedImage.class, containerType, AccumulatorType.ONE));
    }

    @Override
    public <T> DownloadImage collectMany(BiConsumer<T, BufferedImage> modelMutation, Class<T> containerType) {
        return addCollector(new Collector(modelMutation, BufferedImage.class, containerType, AccumulatorType.MANY));
    }

}
