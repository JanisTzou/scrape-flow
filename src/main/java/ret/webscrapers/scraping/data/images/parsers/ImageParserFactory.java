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

package ret.webscrapers.scraping.data.images.parsers;


import aaanew.drivers.DriverManager;
import aaanew.drivers.HtmlUnitDriverManager;
import aaanew.drivers.SeleniumDriverManager;
import ret.appcore.model.enums.WebsiteEnum;

public class ImageParserFactory {

    public ImageParser newImageParser(WebsiteEnum website, DriverManager<?> driverManager) {
        switch (website) {
            case SREALITY:
                return new SRealityImageParser((SeleniumDriverManager) driverManager);
            case BEZ_REALITKY:
                return new BezRealitkyImageParser((SeleniumDriverManager) driverManager);
            case IDNES_REALITY:
                return new IDnesRealityImageParser((HtmlUnitDriverManager) driverManager);
            default:
                throw new IllegalArgumentException("ImageParser FOR " + website + "  NOT IMPLEMETED YET");
        }

    }

}
