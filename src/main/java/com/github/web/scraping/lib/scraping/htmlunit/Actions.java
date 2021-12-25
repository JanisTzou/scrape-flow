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

import com.gargoylesoftware.htmlunit.html.DomNode;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class Actions {

    public static FilterElements filterElements(Predicate<DomNode> domNodePredicate) {
        return new FilterElements(domNodePredicate);
    }

    public static MapElements mapElements(Function<DomNode, Optional<DomNode>> mapper) {
        return new MapElements(mapper);
    }

    /**
     * <p>Replaces the current page with a new one that is loaded after the link is followed.
     * <p><b>IMPORTANT</b>: use this only when there are no other steps working with the page.
     * If not sure, use {@link Actions#navigateToParsedLink(HtmlUnitSiteParser)}
     */
    public static FollowLink followLink() {
        return new FollowLink();
    }

    /**
     * Can be used to load a new page after a link has been parsed e.g. after {@link Parse#hRef()}.
     * <br>
     * Loads the site into a new page instance - this is important e.g. when we are scraping additional data from the same page.
     * If you are sure that parsing is finished at current page you at that point, use {@link Actions#followLink()}
     */
    public static NavigateToParsedLink navigateToParsedLink(HtmlUnitSiteParser siteParser) {
        return new NavigateToParsedLink(siteParser);
    }

    public static ReturnNextPage returnNextPage() {
        return new ReturnNextPage();
    }

    public static Paginate paginate() {
        return new Paginate();
    }

}
