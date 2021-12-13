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

package ret.webscrapers.scraping.data.inzeraty.parsers;


import aaanew.drivers.DriverManager;
import aaanew.drivers.DriverManagerFactory;
import aaanew.drivers.HtmlUnitDriversFactory;
import aaanew.throttling.model.ScrapedDataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ret.appcore.model.ScrapedInzerat;
import ret.appcore.model.enums.TypNabidkyEnum;
import ret.appcore.model.enums.TypNemovitostiEnum;
import ret.appcore.model.enums.WebsiteEnum;
import ret.webscrapers.AppConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Ignore
public class SRealityInzeratParserTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void test() throws MalformedURLException {

        AppConfig.turnOffHtmlUnitLogger();
//        String urlStr = "https://www.sreality.cz/detail/prodej/byt/1+kk/praha-liben-pod-labutkou/4118388316";
//        String urlStr = "https://www.sreality.cz/detail/prodej/byt/6-a-vice/praha-karlin-saldova/174980700#img=0&fullscreen=false";
//        String urlStr = "https://www.sreality.cz/detail/prodej/byt/2+kk/praha-karlin-/3261587036";
        String urlStr = "https://www.sreality.cz/detail/prodej/byt/2+kk/praha-dolni-chabry-k-beranovu/1068818012#img=0&fullscreen=false";

        HtmlUnitDriversFactory driversFactory = new HtmlUnitDriversFactory();
        DriverManagerFactory driverManagerFactory = new DriverManagerFactory(null, driversFactory);
        InzeratParserFactory inzeratParserFactory = new InzeratParserFactory();
        DriverManager<?> driverManager = driverManagerFactory.newDriverManager(WebsiteEnum.SREALITY, ScrapedDataType.INZERAT);
        InzeratParser inzeratParser = inzeratParserFactory.newInzeratParser(WebsiteEnum.SREALITY, driverManager);
        Optional<ScrapedInzerat> scrapedInzerat = inzeratParser.scrapeData(urlStr, WebsiteEnum.SREALITY, TypNabidkyEnum.PRODEJ, TypNemovitostiEnum.BYT, null);
        System.out.println(scrapedInzerat);
    }


    @Test
    public void testDownloadingImage() throws IOException {

//        String url = "https://www.bezrealitky.cz/media/cache/record_main/data/record/images/542k/542398/1546640379-akhehloagy.jpg";
        String url = "https://d18-a.sdn.szn.cz/d_18/c_img_G_2/BDeByWG.jpeg?fl=res,749,562,3|shr,,20|jpg,90";

        URL imageURL = new URL(url);

        //read url and retrieve image
        BufferedImage saveImage = ImageIO.read(imageURL);

        //download image to the workspace where the project is, save picture as picture.png (can be changed)

        ImageIO.write(saveImage, "jpg", new File("G:\\sreality\\downloaded_image" + ".jpg"));
    }


}
