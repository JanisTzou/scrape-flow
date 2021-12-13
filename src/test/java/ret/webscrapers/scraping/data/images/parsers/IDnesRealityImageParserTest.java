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
import aaanew.drivers.DriverManagerFactory;
import aaanew.drivers.HtmlUnitDriversFactory;
import aaanew.throttling.model.ScrapedDataType;
import org.junit.Ignore;
import org.junit.Test;
import ret.appcore.model.ScrapedInzerat;
import ret.appcore.model.enums.WebsiteEnum;

import java.awt.image.BufferedImage;
import java.util.List;

@Ignore
public class IDnesRealityImageParserTest {

    @Test
    public void scrapeImages() {

        String url = "https://reality.idnes.cz/detail/pronajem/byt/praha-10-pocernicka/5d9601da558f076f9778b8bd/?s-et=flat&s-l=VUSC-19";
        HtmlUnitDriversFactory driversFactory = new HtmlUnitDriversFactory();
        DriverManagerFactory driverManagerFactory = new DriverManagerFactory(null, driversFactory);
        ImageParserFactory imageParserFactory = new ImageParserFactory();
        DriverManager<?> driverManager = driverManagerFactory.newDriverManager(WebsiteEnum.IDNES_REALITY, ScrapedDataType.IMAGES);
        ImageParser imageParser = imageParserFactory.newImageParser(WebsiteEnum.IDNES_REALITY, driverManager);
        List<BufferedImage> bufferedImages = imageParser.scrapeImages(url, new ScrapedInzerat());

        System.out.println(bufferedImages.size());

    }
}
