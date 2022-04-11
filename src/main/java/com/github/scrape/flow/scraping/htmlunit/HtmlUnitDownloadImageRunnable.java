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
import com.github.scrape.flow.data.collectors.Collectors;
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ParsedValueToModelCollector;
import com.github.scrape.flow.scraping.ScrapingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;

@Log4j2
@RequiredArgsConstructor
public class HtmlUnitDownloadImageRunnable implements Runnable {

    private final ScrapingContext ctx;
    private final StepOrder stepOrder;
    private final HtmlUnitNodeSearchBasedStepHelper helper;
    private final String stepName;
    private final Collectors collectors;

    @Override
    public void run() {
        Supplier<List<DomNode>> nodesSearch = () -> List.of(ctx.getNode());

        try {
            URL imageURL = new URL(ctx.getParsedURL());
            BufferedImage bufferedImage = ImageIO.read(imageURL);
            ParsedValueToModelCollector.setParsedValueToModel(collectors, ctx, bufferedImage, stepName);

            log.debug("Success downloading image");
        } catch (Exception e) {
            log.error("Error downloading image from URL {}", ctx.getParsedURL());
        }

        helper.execute(nodesSearch, ctx, stepOrder);
    }


}
