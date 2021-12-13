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

package ret.webscrapers.scraping.data.images.query;

import ret.webscrapers.http.HttpClient;
import ret.webscrapers.http.RetApiConfig;
import ret.webscrapers.pipe.services.AbstractQueryService;

public class ImageQueryServiceImpl extends AbstractQueryService implements ImageQueryService {

    public ImageQueryServiceImpl(HttpClient httpClient, String queryUrl) {
        super(httpClient, queryUrl);
    }

    public boolean shouldScrapeImageFor(String inzeratUrl) {
        return shouldScrapeDataFor(inzeratUrl);
    }

    @Override
    protected String makeShouldScrapeDataUrl(String inzeratIdentifier) {
        return RetApiConfig.makeShouldScrapeImageURL(inzeratIdentifier);
    }
}
