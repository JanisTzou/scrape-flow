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

package com.github.web.scraping.lib;

import com.github.web.scraping.lib.dom.data.parsing.ParsedElement;
import com.github.web.scraping.lib.dom.data.parsing.SiteParser;

import java.util.List;

public class Scraper {

    // can the parsing here be from both selenium and htmlunit?
    // TODO make flux based ...

    public void scrape(List<EntryPoint> entryPoints) {
        for (EntryPoint entryPoint : entryPoints) {
            String url = entryPoint.getUrl();
            ScrapingStage scrapingStage = entryPoint.getScrapingStage();
            doScrape(url, scrapingStage);
        }
    }

    private void doScrape(String url, ScrapingStage scrapingStage) {
        SiteParser<?> siteParser = scrapingStage.getSiteParser();
        List<ParsedElement> parsedElements = siteParser.parse(url);

        // TODO these results correspond to one "row" of data ...
        //  but we need to be able to iterate through multiple "rows" ...
        //  ... at the beginning we might need to scroll all the way down ...
        //  ... at the end we might need to paginate ...

        for (ParsedElement parsedElement : parsedElements) {
            List<ScrapingStage> nextStages = scrapingStage.findNextStagesByIdentifier(parsedElement.getIdentifier());
            for (ScrapingStage nextStage : nextStages) {
                String nextUrl = nextStage.getParsedHRefToURLMapper().apply(parsedElement.getHref());
                doScrape(nextUrl, nextStage);
            }
//            System.out.println(parsedElement);
            System.out.println(parsedElement.info());
        }
    }

    public void scrape(EntryPoint entryPoint) {
        this.scrape(List.of(entryPoint));
    }


}
