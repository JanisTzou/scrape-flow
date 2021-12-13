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

package ret.webscrapers.pipe.services;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.Inzerat;
import ret.appcore.model.scraping.InzeratUUIDList;
import ret.webscrapers.http.HttpClient;
import ret.webscrapers.http.HttpResponse;
import ret.webscrapers.http.HttpServiceBase;
import ret.webscrapers.http.ResponseType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public abstract class AbstractQueryService extends HttpServiceBase implements PipeService {

    private static Logger log = LogManager.getLogger(AbstractQueryService.class);
    private Set<String> notToScrapeInzeratyUUIDs = Collections.synchronizedSet(new HashSet<>());
    private final String queryUrl;


    public AbstractQueryService(HttpClient httpClient, String queryUrl) {
        super(httpClient);
        this.queryUrl = queryUrl;
    }


    protected boolean shouldScrapeDataFor(String inzeratUrl) {
        String inzeratIdentifier = Inzerat.makeIdentifier(inzeratUrl);
        String url = makeShouldScrapeDataUrl(inzeratIdentifier);
        HttpResponse<Boolean> httpResponse = httpClient.makeRequest(url, HttpClient.Method.GET, null, Boolean.class, true);
        if (httpResponse.getResponseType().equals(ResponseType.SUCCESS)) {
            return httpResponse.getData().get();
        } else {
            // TODO NOT GOOD THAT THIS IS SHARED WITH INZERAT DATA ... when we want to hand e images differently !
            //  alternatively return false by default ... Server can handle missing images ...
            boolean notToScrape = notToScrapeInzeratyUUIDs.contains(inzeratIdentifier);
            return ! notToScrape;
        }
    }



    public boolean initialiseOrUpdate() {
        boolean success = false;
        HttpResponse<InzeratUUIDList> httpResponse = httpClient.makeRequest(queryUrl, HttpClient.Method.GET, null, InzeratUUIDList.class, true);
        if (httpResponse.getResponseType().equals(ResponseType.SUCCESS)) {
            if (httpResponse.getData().isPresent()) {
                reset();
                Set<String> notToScrape = Collections.synchronizedSet(new HashSet<>());
                for (String inzeratUUID : httpResponse.getData().get().getInzeratUUIDs()) {
                    notToScrape.add(inzeratUUID);
                }
                this.notToScrapeInzeratyUUIDs = notToScrape;
                success = true;
            }
        } else {
            success = false;
        }
        return success;
    }


    protected abstract String makeShouldScrapeDataUrl(String inzeratUrl);

    public void reset() {
        // not needed atm
    }

}
