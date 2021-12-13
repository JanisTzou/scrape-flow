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

package com.github.web.scraping.lib.scraping;

import com.github.web.scraping.lib.dom.data.parsing.ParsingResult;
import com.github.web.scraping.lib.dom.data.parsing.SiteParser;
import com.github.web.scraping.lib.EntryPoint;

import java.util.List;

public class Scraper {

    public void start(List<EntryPoint> entryPoints) {
        // TODO flux based ...
        for (EntryPoint entryPoint : entryPoints) {
            String url = entryPoint.getUrl();
            SiteParser<?> siteParser = entryPoint.getScrapingSettings().getSiteParser();
            List<ParsingResult> parsingResults = siteParser.parse(url);

            // temporary ...
            for (ParsingResult parsingResult : parsingResults) {
                System.out.println(parsingResult);
            }

            // TODO what to do with the results ? ... there might be nested parsing needed ... so we can fins settings for that ...?
        }
    }

    public void start(EntryPoint entryPoint) {
        this.start(List.of(entryPoint));
        // can the parsing here be from both selenium and htmlunit?
        // TODO ...
    }


}
