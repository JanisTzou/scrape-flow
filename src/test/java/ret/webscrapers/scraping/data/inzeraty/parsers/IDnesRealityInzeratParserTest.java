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
import java.util.List;
import java.util.Optional;

@Ignore
public class IDnesRealityInzeratParserTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void test() throws MalformedURLException {

        AppConfig.turnOffHtmlUnitLogger();

//        String urlStr0 = "https://reality.idnes.cz/detail/prodej/byt/praha-8-pod-labutkou/5c3dbed2e880544ae336f16d/?s-et=flat&s-ot=sale&s-l=SPRAVNI_OBVOD-86&s-qc%5BsubtypeFlat%5D%5B0%5D=1k";
//        String urlStr0 = "https://reality.idnes.cz/detail/prodej/byt/praha-8-frantiska-kadlece/5db9c41c558f0744c25fd152/";
        String urlStr0 = "https://reality.idnes.cz/detail/prodej/byt/praha-6-belohorska/5d05653537ba4d57dd037df2/";
//        String urlStr0 = "https://reality.idnes.cz/detail/prodej/byt/praha-8-krynicka/5cdc76dce8805443527793e3/?s-et=flat&s-ot=sale&s-l=SPRAVNI_OBVOD-86&s-qc%5BsubtypeFlat%5D%5B0%5D=1k&page=2";
//        String urlStr1 = "https://reality.idnes.cz/detail/prodej/byt/praha-1-karoliny-svetle/5c9b957be880541c3e587f56/?s-et=flat&s-l=VUSC-19";
//        String urlStr2 = "https://reality.idnes.cz/detail/prodej/byt/praha-1-opatovicka/5d8219e6558f0777af1b2922/";
//        String urlStr3 = "https://reality.idnes.cz/detail/prodej/byt/kamenicky-senov/5d925f6f37ba4d7e1a217da3/?page=6";
//        String urlStr4 = "https://reality.idnes.cz/detail/prodej/byt/unicov-sternberska/5d936bc9558f0750c43132b3/?s-et=flat&s-l=VUSC-124";
//        String urlStr5 = "https://reality.idnes.cz/detail/prodej/byt/plumlov-lesnicka/5cd95f11a26e3a3bbc73a023/?s-et=flat&s-l=VUSC-124";
//        String urlStr6 = "https://reality.idnes.cz/detail/pronajem/byt/olomouc-brnenska/5d93698c558f074006729db3/?s-et=flat&s-l=VUSC-124";

//        List<String> urls = List.of(urlStr0, urlStr1, urlStr2, urlStr3, urlStr4, urlStr5, urlStr6);
        List<String> urls = List.of(urlStr0);

        for (String url : urls) {
            HtmlUnitDriversFactory driversFactory = new HtmlUnitDriversFactory();
            DriverManagerFactory driverManagerFactory = new DriverManagerFactory(null, driversFactory);
            InzeratParserFactory inzeratParserFactory = new InzeratParserFactory();
            DriverManager<?> driverManager = driverManagerFactory.newDriverManager(WebsiteEnum.IDNES_REALITY, ScrapedDataType.INZERAT);
            InzeratParser inzeratParser = inzeratParserFactory.newInzeratParser(WebsiteEnum.IDNES_REALITY, driverManager);

            TypNemovitostiEnum typNemovitosti;
            if (url.contains("/byt")) {
                typNemovitosti = TypNemovitostiEnum.BYT;
            } else {
                typNemovitosti = TypNemovitostiEnum.DUM;
            }

            Optional<ScrapedInzerat> scrapedInzerat = inzeratParser.scrapeData(url, WebsiteEnum.IDNES_REALITY, TypNabidkyEnum.PRODEJ, typNemovitosti, null);
            System.out.println(scrapedInzerat.get());
            System.out.println(scrapedInzerat.get().getInzeratInfo().getScrapedProdavajici());
        }

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
