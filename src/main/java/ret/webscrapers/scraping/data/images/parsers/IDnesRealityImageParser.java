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
import aaanew.drivers.DriverOperatorBase;
import aaanew.utils.HtmlUnitUtils;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.ScrapedInzerat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IDnesRealityImageParser extends DriverOperatorBase<WebClient> implements ImageParser {

    private static final Logger log = LogManager.getLogger();


    public IDnesRealityImageParser(DriverManager<WebClient> driverManager) {
        super(driverManager);
    }

    @Override
    public List<BufferedImage> scrapeImages(String inzeratUrl, ScrapedInzerat scrapedInzerat) {

        /*
              <div class="carousel__list">

                <a href="https://reality.idnes.cz/file/thumbnail/5d9601da558f076f9778b8be?profile=front_detail_article_big_fit" class="carousel__item b-gallery__img-lg__item b-gallery__img--center" data-fancybox="images">
                  <span>
                    <img src="https://sta-reality2.1gr.cz/sta/compile/thumbs/2/6/a/ba9bc564b7d6a3e6f2d35066f8968.jpg" alt="" width="1018" height="600"/>
                  </span>
                </a>

                <a href="https://reality.idnes.cz/file/thumbnail/5d9601da558f076f9778b8bf?profile=front_detail_article_big_fit" class="carousel__item b-gallery__img-lg__item b-gallery__img--center" data-fancybox="images">
                  <span>
                    <img data-lazy="https://reality.idnes.cz/file/thumbnail/5d9601da558f076f9778b8bf?profile=front_detail_article_big" src="https://sta-reality2.1gr.cz/ui/image/no-image-gallery.png" alt="" width="1018" height="600"/>
                  </span>
                </a>
               ...

               </div>
         */

        List<BufferedImage> images = new ArrayList<>();

        WebClient webClient = driverManager.getDriver();

        try {

            HtmlPage htmlPage = (HtmlPage) webClient.getPage(inzeratUrl);
            Optional<DomElement> divElementOpt = HtmlUnitUtils.getChildElement(htmlPage.getBody(), "div", "class", "carousel__list", true);

            if (divElementOpt.isPresent()) {
                List<String> imgUrls = HtmlUnitUtils.getAllAttributeValuesFromChildElements(divElementOpt.get(), "a", "href");

                for (String imgUrl : imgUrls) {
                    try {
                        URL imageURL = new URL(imgUrl);
                        BufferedImage bufferedImage = ImageIO.read(imageURL);
                        images.add(bufferedImage);
                    } catch (IOException e) {
                        log.error("Error trying to read image for inzerat:{} from url = {}", scrapedInzerat.getInzeratIdentifier(), imgUrl);
                    }
                }
            } else {
                log.warn("Failed to find root element of urls for inzerat: {}", scrapedInzerat.getInzeratIdentifier());
            }


//
//            for (DomElement divElement : divElementOpt) {
//                DomNodeList<HtmlElement> imgElements = divElement.getElementsByTagName("a");
//                for (HtmlElement htmlElement : imgElements) {
//                    String scrapedUrl = htmlElement.getAttribute("href");
//                    try {
//                        URL imageURL = new URL(scrapedUrl);
//                        BufferedImage bufferedImage = ImageIO.read(imageURL);
//                        images.add(bufferedImage);
//                    } catch (IOException e) {
//                        log.error("Error trying to read image from url = {}", scrapedUrl);
//                    }
//                }
//            }

        } catch (IOException e) {
            log.error("Error while trying to scrape images for inzerat {}", scrapedInzerat.getInzeratIdentifier());
        }

        return images;
    }

}
