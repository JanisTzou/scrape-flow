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

package ret.webscrapers.messages;


import ret.appcore.model.WebSegment;
import ret.appcore.model.scraping.WebSegmentScrapingStats;

// message
public class FinishedWebSegmentMsg extends WebSegmentPipeMsg {

    private final WebSegmentScrapingStats webSegmentScrapingStats;

    public FinishedWebSegmentMsg(WebSegment webSegment, WebSegmentScrapingStats webSegmentScrapingStats) {
        super(webSegment);
        this.webSegmentScrapingStats = webSegmentScrapingStats;
    }

    public WebSegmentScrapingStats getWebSegmentScrapingStats() {
        return webSegmentScrapingStats;
    }


    @Override
    public String toString() {
        return "FinishedScrapingWebSegmentMsg{" +
                "webSegment=" + webSegment +
                ", webSegmentScrapingStats=" + webSegmentScrapingStats +
                '}';
    }
}
