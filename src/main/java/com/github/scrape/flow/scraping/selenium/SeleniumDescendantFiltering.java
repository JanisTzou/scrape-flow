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

import com.github.scrape.flow.scraping.Filter;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterByAttribute;
import com.github.scrape.flow.scraping.selenium.filters.SeleniumFilterByTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.scrape.flow.scraping.selenium.SeleniumUtils.*;

@Deprecated
public class SeleniumDescendantFiltering {

    private static final Logger log = LogManager.getLogger();

    public static List<WebElement> getElementsByFilters(WebElement webElement, List<Filter<WebElement>> filters) {

        // by attribute
        // --> 1 - by id
        // --> 2 - by class
        // --> 4 - by other attribute // TODO ,aybe this is more specific then by tag?
        // 3 -by tag


        final List<Filter<WebElement>> applied = new ArrayList<>();
        final List<Filter<WebElement>> apply = new ArrayList<>(filters);

        List<WebElement> found;
        found = getElementsById(webElement, applied, apply);
        found = getElementsByClass(webElement, found, applied, apply);
        found = getElementsByTag(webElement, found, applied, apply);
        // TODO find by other attributes ...

        return found;
    }

    public static List<WebElement> getElementsById(WebElement root, List<Filter<WebElement>> applied, List<Filter<WebElement>> apply) {
        // TODO there should only be one ... if there are more log warning and use the first found
        final List<SeleniumFilterByAttribute> filters = filtersByAttribute(apply, a -> a.equalsIgnoreCase("id"));

        boolean appliedFirst = false;
        List<WebElement> elements = Collections.emptyList();

        for (SeleniumFilterByAttribute filter : filters) {
            if (!appliedFirst) {
                elements = root.findElements(By.id(filter.getAttributeValue()));
                applied.add(filter);
                apply.remove(filter);
                appliedFirst = true;
            } else {
                applied.add(filter);
                // ignore multiple id filters
            }
        }

        return elements;
    }

    public static List<WebElement> getElementsByClass(WebElement root, List<WebElement> found, List<Filter<WebElement>> applied, List<Filter<WebElement>> apply) {
        final List<SeleniumFilterByAttribute> filters = filtersByAttribute(apply, a -> a.equalsIgnoreCase("class")); // TODO there should only be one ... if there are more log warning and use the first found
        List<WebElement> elements = found;
        if (elements.isEmpty()) {
            for (int i = 0; i < filters.size(); i++) {
                SeleniumFilterByAttribute filter = filters.get(i);
                if (i == 0) {
                    elements = root.findElements(By.className(filter.getAttributeValue()));
                } else {
                    elements = elements.stream().filter(e -> hasCssClass(e, filter.getAttributeValue())).collect(Collectors.toList());
                }
                applied.add(filter);
                apply.remove(filter);
            }
        } else {
            for (SeleniumFilterByAttribute filter : filters) {
                elements = elements.stream().filter(e -> hasCssClass(e, filter.getAttributeValue())).collect(Collectors.toList());
            }
        }

        return elements;
    }

    public static List<WebElement> getElementsByTag(WebElement root, List<WebElement> found, List<Filter<WebElement>> applied, List<Filter<WebElement>> apply) {
        final List<SeleniumFilterByTag> filters = filtersByTag(apply); // TODO there should only be one ... if there are more log warning and use the first found
        List<WebElement> elements = found;
        if (elements.isEmpty()) {
            for (SeleniumFilterByTag filter : filters) {
                // TODO only one should be legal ... handle in the setup ...
                elements = root.findElements(By.tagName(filter.getTagName()));
                applied.add(filter);
                apply.remove(filter);
            }
        } else {
            for (SeleniumFilterByTag filter : filters) {
                elements = elements.stream().filter(e -> hasTagName(e, filter.getTagName())).collect(Collectors.toList());
            }
        }

        return elements;
    }


    // TODO by other attribute ...



    private static List<SeleniumFilterByAttribute> filtersByAttribute(List<Filter<WebElement>> filters, Predicate<String> attrPredicate) {
        return filters.stream()
                .filter(f -> f instanceof SeleniumFilterByAttribute).map(f -> (SeleniumFilterByAttribute) f)
                .filter(f -> attrPredicate.test(f.getAttributeName()))
                .collect(Collectors.toList());
    }

    private static List<SeleniumFilterByTag> filtersByTag(List<Filter<WebElement>> filters) {
        return filters.stream()
                .filter(f -> f instanceof SeleniumFilterByTag).map(f -> (SeleniumFilterByTag) f)
                .collect(Collectors.toList());
    }




}
