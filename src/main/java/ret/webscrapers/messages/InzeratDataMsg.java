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

import ret.appcore.model.ScrapedInzerat;
import ret.appcore.model.WebSegment;

import java.awt.image.BufferedImage;
import java.util.List;

public class InzeratDataMsg extends InzeratUrlPipeMsg {

    protected final ScrapedInzerat scrapedInzerat;

    public InzeratDataMsg(ScrapedInzerat scrapedInzerat,
                          WebSegment webSegment,
                          String inzeratUrl,
                          String inzeratIdentifier) {
        super(webSegment, inzeratUrl, inzeratIdentifier);
        this.scrapedInzerat = scrapedInzerat;
    }

    public InzeratWithImagesDataMsg toInzeratWithImagesDataMsg(List<BufferedImage> bufferedImages) {
        return new InzeratWithImagesDataMsg(getScrapedInzerat(), getWebSegment(), getInzeratUrl(), bufferedImages, getInzeratIdentifier());
    }

    public ScrapedInzerat getScrapedInzerat() {
        return scrapedInzerat;
    }


    @Override
    public String toString() {
        return "InzeratDataMsg{" +
                "webSegment=" + webSegment +
                ", inzeratIdentifier='" + inzeratIdentifier + '\'' +
                ", inzeratUrl='" + inzeratUrl + '\'' +
                ", scrapedInzerat=" + scrapedInzerat +
                '}';
    }
}
