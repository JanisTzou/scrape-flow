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


import aaanew.drivers.DriverOperatorBase;
import aaanew.drivers.SeleniumDriverManager;
import aaanew.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ret.appcore.model.ScrapedInzerat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SRealityImageParser extends DriverOperatorBase<WebDriver> implements ImageParser {

    private static final Logger log = LogManager.getLogger();


    public SRealityImageParser(SeleniumDriverManager driverManager) {
        super(driverManager);
    }


    public List<BufferedImage> scrapeImages(String inzeratUrl, ScrapedInzerat scrapedInzerat) {

        String inzIdentifier = scrapedInzerat.getInzeratIdentifier();
        WebDriver webDriver = driverManager.getDriver();
        webDriver.get(inzeratUrl);

        ConsumerOperation clickToGetToGallery = () -> {
            WebElement clickablePhoto = webDriver.findElement(By.xpath("/html/body/div[2]/div[1]/div[2]/div[2]/div[4]/div/div/div/div/div[3]/div[1]/div/div[2]/div"));
            JavascriptExecutor js = (JavascriptExecutor) webDriver;
            js.executeScript("arguments[0].click();", clickablePhoto);
            sleepAfterClickForImage();
        };

        try {
            OperationExecutor.attemptExecute(clickToGetToGallery, 100, 500, log, "SR_IMG: ", Optional.of("Fail getting to image gallery of " + inzIdentifier +  ". Keep trying."), "Fail getting to image gallery of: " + inzIdentifier, Optional.of("Success getting to image gallery of " + inzIdentifier));
        } catch (ProcessingException ex) {
            log.error(ex);
        }

        SupplierOperation<Optional<WebElement>> getNextBtnElemOperation = () -> {
            /**
             <button class="btn-circle-gallery-fs icof icon-arr-right ng-scope" ng-click="nextPhoto($event)" ng-if="!isThumbnails()"></button>
             */
            WebElement btnNextElem = null;
            Optional<WebElement> bodyElementOpt = SeleniumUtils.getBodyElement(webDriver, inzIdentifier, inzeratUrl);
            if (bodyElementOpt.isPresent()) {
                btnNextElem = SeleniumUtils.getElement(bodyElementOpt.get(), "button", "ng-click", "nextPhoto($event)").orElse(null);
                if (btnNextElem != null) {
                    log.info("Found next image button for {}", inzIdentifier);
                } else {
                    log.warn("Failed to find next image button for {}", inzIdentifier);
                }
            }
            return Optional.ofNullable(btnNextElem);
        };

        Optional<WebElement> nextBtnElemOp = Optional.empty();
        try {
            nextBtnElemOp = OperationExecutor.attemptExecute(getNextBtnElemOperation, 100, 500, log, "SR_IMG: ", Optional.of("Fail getting next img btn. Keep trying"), "Fail getting next img btn", Optional.empty());
        } catch (ProcessingException ex) {
            log.error(ex);
        }

        List<BufferedImage> images = new ArrayList<>();

        // TODO if we fail to get image too many times and miss a lot of them, we should return nothing rather than few photos ...
        //  hopefuly they will be scraped next ...

        try {
            if (nextBtnElemOp.isPresent()) {

                for (int i = 0; i < 30; i++) {

                    SupplierOperation<String> getImgUrlOperation = () -> {
                        /*
                        <img alt="" class="img ng-scope" ng-if="isFullscreen() &amp;&amp; !isVideo() &amp;&amp; images.fullscreen.image" my-image="" img-src="https://d18-a.sdn.cz/d_18/c_img_E_BV/8gvBqNc.jpeg?fl=res,1920,1080,1|wrm,/watermark/sreality.png,10|shr,,20|jpg,90" img-load="images.fullscreen.active.loading = true" img-done="images.fullscreen.active.loading = false" src="https://d18-a.sdn.cz/d_18/c_img_E_BV/8gvBqNc.jpeg?fl=res,1920,1080,1|wrm,/watermark/sreality.png,10|shr,,20|jpg,90">
                         */
                        Optional<WebElement> bodyElementOpt = SeleniumUtils.getBodyElement(webDriver, inzIdentifier, inzeratUrl);
                        if (bodyElementOpt.isPresent()) {
                            Optional<WebElement> imgSrcElement = SeleniumUtils.getElement(bodyElementOpt.get(), "img", "img-src");
                            if (imgSrcElement.isPresent()) {
                                String scrapedUrl = imgSrcElement.get().getAttribute("img-src");
                                return scrapedUrl;
                            }
                        }
                        return null;
                    };

                    String scrapedUrl = null;
                    try {
                        scrapedUrl = OperationExecutor.attemptExecute(getImgUrlOperation, 100, 500, log, "SR_IMG: ", Optional.of("Fail getting img url " + inzIdentifier + ". Keep trying"), "FAILED getting img url for: " + inzIdentifier + ". No more attempts!", Optional.empty());
                        log.info("Scraped img url for: {}", inzIdentifier);
                    } catch (ProcessingException ex) {
                        log.error(ex);
                        break;
                    }

                    if (scrapedUrl != null) {
                        try {
                            URL imageURL = new URL(scrapedUrl);
                            BufferedImage bufferedImage = ImageIO.read(imageURL);
                            if (bufferedImage != null) {
                                images.add(bufferedImage);
                            } else {
                                log.error("Loaded image null! Inzerat {}, Image url: {}", inzIdentifier, scrapedUrl);
                            }
                        } catch (IOException e) {
                            log.error("Error trying to read image for {}, from url = {}", inzIdentifier, scrapedUrl);
                        }
                    }

                    JavascriptExecutor js = (JavascriptExecutor) webDriver;
                    js.executeScript("arguments[0].click();", nextBtnElemOp.get());

                    String currentUrl = webDriver.getCurrentUrl();
                    if (currentUrl.contains("img=0")) {
                        log.info("Reached end");
                        break;
                    }

                    sleepAfterClickForImage();

                }
            }
        } catch (Exception e) {
            log.error("Error loading images from url: {}.", inzeratUrl, e);
        }

        return images;

    }

    private void sleepAfterClickForImage() {
        SeleniumUtils.sleep(400);
    }


}
