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

import com.github.web.scraping.lib.dom.data.parsing.SiteParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Encapsulates settings of the next level to scrape data from
 */
public class ScrapingStage {

    // how do we determine what URLs will be scraped here?
    // they must be scraped from the previous level
    // somehow we need to link the scraped data to these settings ... possibly usig an enum? or some other key ?

    @Getter
    private final SiteParser<?> siteParser;

    // identifies which parsed href belongs to this scrapingStage
    @Nullable
    private final Enum<?> hrefKey;

    // creates a full url from from a parsed href
    @Getter
    private final Function<String, String> hrefToURLMapper;

    @Getter
    private final List<ScrapingStage> nextStages;

    // create Paginator for pagination
    // create Scroller for scrolling ... JS sites ...


    public ScrapingStage(SiteParser<?> siteParser, @Nullable Enum<?> hrefKey, @Nullable Function<String, String> hrefToURLMapper, List<ScrapingStage> nextStages) {
        this.siteParser = siteParser;
        this.hrefKey = hrefKey;
        this.hrefToURLMapper = Objects.requireNonNullElse(hrefToURLMapper, s -> s);
        this.nextStages = nextStages;
    }

    public static Builder builder() {
        return new Builder();
    }


    public Optional<Enum<?>> getHrefKey() {
        return Optional.ofNullable(hrefKey);
    }

    public List<ScrapingStage> findNextStagesByIdentifier(Enum<?> identifier) {
        return this.nextStages.stream()
                .filter(ss -> ss.getHrefKey().isPresent() && ss.getHrefKey().get().equals(identifier))
                .collect(Collectors.toList());
    }

    public static class Builder {

        private SiteParser<?> siteParser;
        @Nullable
        private Enum<?> hrefKey;
        private Function<String, String> hrefToURLMapper;
        private final List<ScrapingStage> nextStages = new ArrayList<>();

        public Builder setParser(SiteParser<?> siteParser) {
            this.siteParser = siteParser;
            return this;
        }

        public Builder setHrefKey(@Nullable Enum<?> hrefKey) {
            this.hrefKey = hrefKey;
            return this;
        }

        public Builder setHrefToURLMapper(Function<String, String> hrefToURLMapper) {
            this.hrefToURLMapper = hrefToURLMapper;
            return this;
        }

        public Builder addNextStage(ScrapingStage nextStage) {
            this.nextStages.add(nextStage);
            return this;
        }

        public ScrapingStage build() {
            return new ScrapingStage(siteParser, hrefKey, hrefToURLMapper, nextStages);
        }

    }

}
