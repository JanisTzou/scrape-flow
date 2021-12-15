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

import com.github.web.scraping.lib.dom.data.parsing.FullUrlCreator;
import com.github.web.scraping.lib.dom.data.parsing.SiteParser;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Encapsulates settings of the next level to scrape data from
 */
public class CrawlingStage {

    // how do we determine what URLs will be scraped here?
    // they must be scraped from the previous level
    // somehow we need to link the scraped data to these settings ... possibly usig an enum? or some other key ?

    @Getter
    private final SiteParser<?> siteParser;

    // TODO maybe encapsulate the identifier with the mapper bewlo so it is clear that they belong together ...
    // identifies which parsed href belongs to this scrapingStage
    @Nullable
    private final Enum<?> parsedHRefIdentifier;

    // creates a full url from from a parsed href
    @Getter
    private final FullUrlCreator fullURLCreator;

    @Getter
    private final List<CrawlingStage> nextStages;

    // create Paginator for pagination
    // create Scroller for scrolling ... JS sites ...


    public CrawlingStage(SiteParser<?> siteParser, @Nullable Enum<?> parsedHRefIdentifier, @Nullable FullUrlCreator fullURLCreator, List<CrawlingStage> nextStages) {
        this.siteParser = siteParser;
        this.parsedHRefIdentifier = parsedHRefIdentifier;
        this.fullURLCreator = Objects.requireNonNullElse(fullURLCreator, s -> s);
        this.nextStages = nextStages;
    }

    public static Builder builder() {
        return new Builder();
    }


    public Optional<Enum<?>> getParsedHRefIdentifier() {
        return Optional.ofNullable(parsedHRefIdentifier);
    }

    public List<CrawlingStage> findNextStagesByReference(Enum<?> identifier) {
        return this.nextStages.stream()
                .filter(ss -> ss.getParsedHRefIdentifier().isPresent() && ss.getParsedHRefIdentifier().get().equals(identifier))
                .collect(Collectors.toList());
    }

    public static class Builder {

        private SiteParser<?> siteParser;
        @Nullable
        private Enum<?> parsedHRefIdentifier;
        private FullUrlCreator fullURLCreator;
        private final List<CrawlingStage> nextStages = new ArrayList<>();

        public Builder setParser(SiteParser<?> siteParser) {
            this.siteParser = siteParser;
            return this;
        }

//        public Builder setParsedHRefIdentifier(@Nullable Enum<?> parsedHRefIdentifier) {
//            this.parsedHRefIdentifier = parsedHRefIdentifier;
//            return this;
//        }
//
//        public Builder setHrefToURLMapper(Function<String, String> hrefToURLMapper) {
//            this.hrefToURLMapper = hrefToURLMapper;
//            return this;
//        }

        /**
         * @param parsedHRefIdentifier identifier of the scraped HRef value
         * @param fullURLCreator useful when it is necessary to provide a base URL for the scraped HRef to work
         */
        public Builder setReferenceForParsedHrefToCrawl(@Nullable Enum<?> parsedHRefIdentifier, FullUrlCreator fullURLCreator) {
            this.parsedHRefIdentifier = parsedHRefIdentifier;
            this.fullURLCreator = fullURLCreator;
            return this;
        }

        public Builder addNextStage(CrawlingStage nextStage) {
            this.nextStages.add(nextStage);
            return this;
        }

        public CrawlingStage build() {
            return new CrawlingStage(siteParser, parsedHRefIdentifier, fullURLCreator, nextStages);
        }

    }

}
