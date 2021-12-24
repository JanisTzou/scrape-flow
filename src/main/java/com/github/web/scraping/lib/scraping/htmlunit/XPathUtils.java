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

package com.github.web.scraping.lib.scraping.htmlunit;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// these will be specific for all drivers...
public class XPathUtils {

    // TODO s / ideas:
    // split XPath by "/"
    // identify starting from the end which numbers are different ...

    // create xpath map of the whole document? Identify repeated items?
    // ... that way we might not need to provide two but just one XPath to identify iterable elements ...

    public static void getXPaths(HtmlPage loadedPage) {
        HtmlElement htmlElement = loadedPage.getBody();
        List<String> collector = new ArrayList<>();
        getXPathsStartingFrom(htmlElement, collector);
    }

    public static List<String> getXPathsStartingFrom(HtmlElement htmlElement) {
        List<String> collector = new ArrayList<>();
        getXPathsStartingFrom(htmlElement, collector);
        return collector;
    }

    private static void getXPathsStartingFrom(HtmlElement htmlElement, List<String> collector) {
        String xPath = htmlElement.getCanonicalXPath();
        collector.add(xPath);
//        System.out.println(xPath);
        for (DomElement childElement : htmlElement.getChildElements()) {
            if (childElement instanceof HtmlElement htmlEl) {
                getXPathsStartingFrom(htmlEl, collector);
            }
        }
    }

    // TODO see what could be solved by using XPAth expressions ...

    // TODO better clearer names ...


    /**
     * @param tagOffset how many tags should be ommitted starting from the end
     */
    public static String getXPathSubstrHead(String xPath, int tagOffset) {
        int idx = StringUtils.lastOrdinalIndexOf(xPath, "/", tagOffset);
        return xPath.substring(0, idx);
    }

    public static String getXPathSubstrTail(String xPath, int tagOffset) {
        int idx = StringUtils.lastOrdinalIndexOf(xPath, "/", tagOffset);
        return xPath.substring(idx);
    }

    public static String getXPathSubstrTailFromStart(String xPath, int tagOffset) {
        if (xPath.startsWith("/")) {
            tagOffset++;
        }
        int idx = StringUtils.ordinalIndexOf(xPath, "/", tagOffset);
        return xPath.substring(idx, xPath.length());
    }

    public static Optional<String> getXPathDiff(String xPathShorter, String xPathLonger) {
        String diff = xPathLonger.replace(xPathShorter, "");
        if (!diff.equals(xPathLonger)) {
            return Optional.of(diff);
        } else {
            return Optional.empty();
        }
    }

    public static String concat(String ... xPathParts) {
        StringBuilder builder = new StringBuilder();
        boolean lastPartEndsWithSlash = false;
        for (String part : xPathParts) {
            if (builder.isEmpty() || lastPartEndsWithSlash || part.startsWith("/")) {
                builder.append(part);
            } else {
                builder.append("/").append(part);
            }
            lastPartEndsWithSlash = part.endsWith("/");
        }
        return builder.toString();
    }

    public static String regexEscape(String xPath) {
        return xPath.replace("[", "\\[")
                .replace("]", "\\]")
                .replace("/", "\\/");
    }


}
