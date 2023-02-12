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

package com.github.scrape.flow.scraping.selenium;

import com.github.scrape.flow.scraping.*;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterByCssClass;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterByTag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.util.Combinations;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"rawtypes", "OptionalGetWithoutIsPresent"})
@Log4j2
public class SeleniumFindChildren {

    private static final Map<SearchKey, SearchStrategy> strategies = new HashMap<>();

    static {
        add(new SearchStrategy(key(SeleniumFilterByTag.class)) {
            @Override
            public SearchResult apply(WebElement webElement, Filters filters) {
                final SeleniumFilterByTag byTag = filters.get(SeleniumFilterByTag.class).findFirst().get();
                final List<WebElement> found = webElement.findElements(By.xpath("child::" + byTag.getTagName()));
                return toResult(found, filters);
            }
        });

        add(new SearchStrategy(key(SeleniumFilterByTag.class, FilterFirstNth.class)) {
            @Override
            public SearchResult apply(WebElement webElement, Filters filters) {
                final SeleniumFilterByTag byTag = filters.get(SeleniumFilterByTag.class).findFirst().get();
                final FilterFirstNth firstNth = filters.get(FilterFirstNth.class).findFirst().get();
                final List<WebElement> found = webElement.findElements(By.xpath(String.format("child::%s[%d]", byTag.getTagName(), firstNth.getNth())));
                return toResult(found, filters);
            }
        });

        add(new SearchStrategy(key(FilterFirstNth.class)) {
            @Override
            public SearchResult apply(WebElement webElement, Filters filters) {
                final FilterFirstNth firstNth = filters.get(FilterFirstNth.class).findFirst().get();
                final List<WebElement> found = webElement.findElements(By.xpath(String.format("child::*[%d]", firstNth.getNth())));
                return toResult(found, filters);
            }
        });

        add(new SearchStrategy(key(FilterLast.class)) {
            @Override
            public SearchResult apply(WebElement webElement, Filters filters) {
                // https://www.javatpoint.com/webdriver-locating-strategies-by-xpath-using-last
                final List<WebElement> found = webElement.findElements(By.xpath("child::*[last()]"));
                return toResult(found, filters);
            }
        });

        add(new SearchStrategy(key(SeleniumFilterByTag.class, FilterFirstNth.class)) {
            @Override
            public SearchResult apply(WebElement e, Filters filters) {
                final SeleniumFilterByTag byTag = filters.get(SeleniumFilterByTag.class).findFirst().get();
                final FilterFirstNth firstNth = filters.get(FilterFirstNth.class).findFirst().get();
                final List<WebElement> found = e.findElements(By.xpath(String.format(".//%s[%d]", byTag.getTagName().toLowerCase(), firstNth.getNth())));
                return toResult(found, filters);
            }
        });

        add(new SearchStrategy(key(SeleniumFilterByCssClass.class)) { // one css class filter expected
            @Override
            public SearchResult apply(WebElement webElement, Filters filters) {
                final SeleniumFilterByCssClass byCssClass = filters.get(SeleniumFilterByCssClass.class).findFirst().get();
                // https://stackoverflow.com/questions/1604471/how-can-i-find-an-element-by-css-class-with-xpath#:~:text=//*%5Bcontains(%40class%2C%20%27Test%27)%5D
                final List<WebElement> found = webElement.findElements(By.xpath(String.format(".//*[contains(concat(' ', @class, ' '), ' %s ')]", byCssClass.getClassName())));
                return toResult(found, filters);
            }
        });

        add(new SearchStrategy(key(SeleniumFilterByTag.class, SeleniumFilterByCssClass.class)) { // one css class filter expected
            @Override
            public SearchResult apply(WebElement webElement, Filters filters) {
                final SeleniumFilterByTag byTag = filters.get(SeleniumFilterByTag.class).findFirst().get();
                final SeleniumFilterByCssClass byCssClass = filters.get(SeleniumFilterByCssClass.class).findFirst().get();
                // https://stackoverflow.com/questions/1604471/how-can-i-find-an-element-by-css-class-with-xpath#:~:text=//div%5Bcontains(%40class%2C%20%27Test%27)%5D
                final List<WebElement> found = webElement.findElements(By.xpath(String.format(".//%s[contains(concat(' ', @class, ' '), ' %s ')]", byTag.getTagName(), byCssClass.getClassName())));
                return toResult(found, filters);
            }
        });
    }

    private static void add(SearchStrategy strategy) {
        strategies.put(strategy.searchKey, strategy);
    }

    // TODO create a combination of all filter keys ...
    //  prioritize them
    //  find the best strategy to apply
    //   apply best and on result apply the non-applied filters ...
    //  use default if no suitable strategy found
    public static List<WebElement> find(WebElement parent, List<Filter<WebElement>> filters) {
        SearchKey searchKey = new SearchKey(filters);
        SearchStrategy strategy = Optional.ofNullable(strategies.get(searchKey))
                .or(() -> getDelegatingSearchStrategy(filters, searchKey))
                .orElse(new DefaultSearchStrategy(searchKey));

        return strategy.apply(parent, new Filters(filters)).getFound();
    }

    @SafeVarargs
    @Nonnull
    private static SearchKey key(Class<? extends Filter>... filters) {
        return new SearchKey(filters);
    }

    @Nonnull
    private static Optional<DelegatingSearchStrategy> getDelegatingSearchStrategy(List<Filter<WebElement>> filters, SearchKey searchKey) {
        return Combinator.getCombinations(filters).stream()
                .map(strategies::get)
                .filter(Objects::nonNull)
                .findFirst()
                .map(s -> new DelegatingSearchStrategy(searchKey, s));
    }

    private int weight(Filter.Type type) {
        switch (type) {
            case ID:
                return 10000;
            case TAG:
            case ATTRIBUTE:
            case CSS_CLASS:
                return 100;
            case POSITION:
            case TEXT_MATCHING:
                return 0;
            default:
                // TODO log warning ...
                return 0;

        }
    }

    @Data
    private static class SearchResult {
        private final List<Filter<WebElement>> applied;
        private final List<Filter<WebElement>> notApplied;
        private final List<WebElement> found;
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    private static class SearchKey {

        private final List<Class<? extends Filter>> filterClasses;

        @SafeVarargs
        private SearchKey(Class<? extends Filter>... filters) {
            filterClasses = sorted(Arrays.stream(filters));
        }

        private SearchKey(List<Filter<WebElement>> filters) {
            filterClasses = sorted(filters.stream().map(f -> f.getClass()));
        }

        @Nonnull
        private List<Class<? extends Filter>> sorted(Stream<? extends Class<? extends Filter>> classStream) {
            return classStream.sorted((c1, c2) -> String.CASE_INSENSITIVE_ORDER.compare(c1.getSimpleName(), c2.getSimpleName())).collect(Collectors.toList());
        }

        private boolean contains(Filter<WebElement> filter) {
            return filterClasses.contains(filter.getClass());
        }
    }

    @Data
    private abstract static class SearchStrategy implements BiFunction<WebElement, Filters, SearchResult> {

        private final SearchKey searchKey;

        protected SearchResult searchByNotAppliedFilters(SearchResult result) {
            final List<WebElement> found = FilterUtils.filter(result.found, result.notApplied);
            final ArrayList<Filter<WebElement>> applied = new ArrayList<>(result.applied);
            applied.addAll(result.notApplied);
            return new SearchResult(applied, Collections.emptyList(), found);
        }

        protected SearchResult toResult(List<WebElement> found, Filters filters) {
            List<Filter<WebElement>> applied = filters.get().stream().filter(searchKey::contains).collect(Collectors.toList());
            List<Filter<WebElement>> notApplied = filters.get().stream().filter(filter -> !searchKey.contains(filter)).collect(Collectors.toList());
            return new SearchResult(applied, notApplied, found);
        }

        protected List<Filter<WebElement>> getMatchingFilters(Filters filters) {
            return searchKey.getFilterClasses().stream()
                    .flatMap(c -> filters.get(c).map(f -> (Filter<WebElement>) f))
                    .distinct()
                    .collect(Collectors.toList());
        }

    }

    private static class DelegatingSearchStrategy extends SearchStrategy {

        private final SearchStrategy delegate;

        public DelegatingSearchStrategy(SearchKey searchKey, SearchStrategy delegate) {
            super(searchKey);
            this.delegate = delegate;
        }

        @Override
        public SearchResult apply(WebElement webElement, Filters filters) {
            log.trace("Running DelegatingSearchStrategy");
            List<Filter<WebElement>> delegateFilters = delegate.getMatchingFilters(filters);
            List<Filter<WebElement>> nonDelegateFilters = new ArrayList<>(filters.get());
            nonDelegateFilters.removeAll(delegateFilters);

            SearchResult delegateResult = delegate.apply(webElement, new Filters(delegateFilters));
            SearchResult tempResult = new SearchResult(delegateResult.applied, nonDelegateFilters, delegateResult.found);
            return searchByNotAppliedFilters(tempResult);
        }
    }

    private static class DefaultSearchStrategy extends SearchStrategy {

        public DefaultSearchStrategy(SearchKey searchKey) {
            super(searchKey);
        }

        @Override
        public SearchResult apply(WebElement webElement, Filters filters) {
            List<WebElement> children = SeleniumUtils.findChildren(webElement);
            List<WebElement> found = FilterUtils.filter(children, filters.get());
            return searchByNotAppliedFilters(new SearchResult(filters.get(), Collections.emptyList(), found));
        }
    }

    // TODO is this needed at all?
    @Deprecated
    private static class Combinator {

        private static Set<SearchKey> getCombinations(List<Filter<WebElement>> filters) {

            final Set<SearchKey> set = new HashSet<>();
            final List<Filter<WebElement>> nonPositionFilters = filters.stream().filter(f -> !f.getType().isPosition()).collect(Collectors.toList());

            for (int items = 1; items <= nonPositionFilters.size(); items++) {
                final Combinations combinations = new Combinations(nonPositionFilters.size(), items);
                final Iterator<int[]> iterator = combinations.iterator();
                while (iterator.hasNext()) {
                    final int[] next = iterator.next();
                    final List<Filter<WebElement>> collect = Arrays.stream(next).mapToObj(nonPositionFilters::get).collect(Collectors.toList());
                    // TODO only add if the key makes sense (we are allowed to apply it and have additional filtering later)
                    //  specifically any position filters can be applied only when all other filters have been used ... so those should be excluded from the combinations maybe?
                    set.add(new SearchKey(collect));
                }
            }

            return set;
        }

    }

    public static class Filters {

        private final List<Filter<WebElement>> filters;

        public Filters(List<Filter<WebElement>> filters) {
            this.filters = filters;
        }

        private <T> Stream<T> get(Class<T> clazz) {
            return filters.stream().filter(f -> clazz.isAssignableFrom(f.getClass())).map(clazz::cast);
        }

        List<Filter<WebElement>> get() {
            return filters;
        }
    }

}
