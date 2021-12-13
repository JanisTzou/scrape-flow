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


import aaanew.drivers.SeleniumDriverManager;
import aaanew.drivers.SeleniumDriversFactory;
import aaanew.drivers.lifecycle.DriverQuitStrategy;
import aaanew.drivers.lifecycle.DriverRestartStrategy;
import aaanew.drivers.lifecycle.QuitAfterIdleInterval;
import aaanew.drivers.lifecycle.RestartDriverAfterInterval;
import aaanew.throttling.model.ScrapedDataType;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ret.appcore.model.Inzerat;
import ret.appcore.model.ScrapedInzerat;
import ret.webscrapers.AppConfig;

import java.awt.image.BufferedImage;
import java.util.List;

@Ignore
public class SRealityImageParserTest {

    SeleniumDriverManager seleniumDriverManager = null;

    @After
    public void tearDown() {
        if (seleniumDriverManager != null) {
            seleniumDriverManager.terminateDriver();
        }
    }

    @Test
    public void scrapeImages() {

//        String inzeratUrl = "https://www.sreality.cz/detail/prodej/byt/1+kk/praha-praha-8-krizikova/1154936412";
//        String inzeratUrl = "https://www.sreality.cz/detail/prodej/byt/1+kk/praha-liben-vojenova/2312760924#img=0&fullscreen=false";
        String inzeratUrl = "https://www.sreality.cz/detail/prodej/byt/1+kk/praha-karlin-/899341916";

        DriverRestartStrategy driverRestartStrategy = new RestartDriverAfterInterval(10000);
        DriverQuitStrategy driverQuitStrategy = new QuitAfterIdleInterval(5);
        SeleniumDriversFactory seleniumDriversFactory = new SeleniumDriversFactory(AppConfig.getChromeDriverDir(), false);
        seleniumDriverManager = new SeleniumDriverManager(ScrapedDataType.IMAGES, driverRestartStrategy, driverQuitStrategy, seleniumDriversFactory);
        SRealityImageParser imageParserSR = new SRealityImageParser(seleniumDriverManager);

        ScrapedInzerat scrapedInzerat = new ScrapedInzerat();
        for (int i = 0; i < 1; i++) {
            scrapedInzerat.setInzeratIdentifier(Inzerat.makeIdentifier(inzeratUrl));
            List<BufferedImage> bufferedImages = imageParserSR.scrapeImages(inzeratUrl, scrapedInzerat);
            System.out.println("Scraped image count = " + bufferedImages.size());
            seleniumDriverManager.terminateDriver();
        }

//        for (BufferedImage bufferedImage : bufferedImages) {
//            System.out.println(bufferedImage.getWidth() + " x " + bufferedImage.getHeight());
//        }

    }
}
