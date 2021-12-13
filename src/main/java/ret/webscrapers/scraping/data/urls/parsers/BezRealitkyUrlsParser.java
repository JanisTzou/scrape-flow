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

package ret.webscrapers.scraping.data.urls.parsers;


import aaanew.drivers.DriverOperatorBase;
import aaanew.drivers.SeleniumDriverManager;
import aaanew.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.*;

public class BezRealitkyUrlsParser extends DriverOperatorBase<WebDriver> implements UrlsParser {

    private static final Logger log = LogManager.getLogger(BezRealitkyUrlsParser.class);


    public BezRealitkyUrlsParser(SeleniumDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public void goToPage(String pageUrl) {
        driverManager.getDriver().get(pageUrl);
        SeleniumUtils.sleep(10000);
    }


    @Override
    public boolean hasMoreUrlsToScrape() {
        /*
            <button type="button" class="btn btn-secondary btn-icon">
                <span class="btn__icon">
                <span class="icon-svg icon-svg--double-arrow"><svg class="icon-svg__svg" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><use xlink:href="https://www.bezrealitky.cz/img/bg/icons-svg.svg#icon-double-arrow" x="0" y="0" width="100%" height="100%"></use></svg></span>
                </span>
                    Zobrazit dalších 15 nabídek
            </button>
         */

        Optional<WebElement> showNextButton = getShowNextButton();

        return (showNextButton.isPresent());
    }


    private Optional<WebElement> getShowNextButton() {

        SupplierOperation<Optional<WebElement>> getNextBtnOperation = () -> {

            List<WebElement> btnElems = driverManager.getDriver().findElements(By.tagName("button"));

            for (int idx = btnElems.size() - 1; idx >= 0 ; idx--) { // needs to be reversed ... takes a lot less time ...
                WebElement btnElem = btnElems.get(idx);
                if (SeleniumUtils.hasAttribute(btnElem, "class")) {
                    String attributeValue = SeleniumUtils.getAttributeValue(btnElem, "class");
                    if (attributeValue.equals("btn btn-secondary btn-icon")) {
                        if (btnElem.getText().contains("Zobrazit dalš")) {
                            return Optional.ofNullable(btnElem);
                        }
                    }
                }

            }

            return Optional.empty();
        };


        try {
            return OperationExecutor.attemptExecute(getNextBtnOperation, 100, 5000, log, "BR: ",
                    Optional.of("Failed to get next btn, KEEP TRYING ... "), "FAILED to get next btn.", Optional.of("SUCCESS getting next btn."));
        } catch (ProcessingException e) {
            log.error(e);
            return Optional.empty();
        }

    }

    @Override
    public Set<String> scrapePropertyUrls() {
        scrollWholePropertyListToBottom();
        return getPropertyUrls();
    }


    private void scrollWholePropertyListToBottom() {

        while (hasMoreUrlsToScrape()) {
            Optional<WebElement> showNextButtonOp = getShowNextButton();
            if (showNextButtonOp.isPresent()) {

                ConsumerOperation clickNextOperation = () -> {
                    JavascriptExecutor js = (JavascriptExecutor) driverManager.getDriver();
                    js.executeScript("arguments[0].click();", showNextButtonOp.get());
                    log.info("Clicked button");
                };

                try {
                    OperationExecutor.attemptExecute(clickNextOperation, 200, 5000, log, "BR: ",
                            Optional.of("Failed to get next btn, KEEP TRYING ... "), "FAILED to get next btn.", Optional.of("SUCCESS getting next btn."));
                } catch (ProcessingException e) {
                    log.error(e);
                    break;
                }

            } else {
                break;
            }

        }
    }


    private Set<String> getPropertyUrls() {

        log.info("Entered scrapePropertyUrls");

                /*
                    <article class="product product--apartment product--apartment--with-hover has-ctas">

                        ...

                                    <span class="items__item">
                                        <a href="https://www.bezrealitky.cz/nemovitosti-byty-domy/539401-nabidka-prodej-bytu-cyprichova-hlavni-mesto-praha" target="_blank" class="btn btn-shadow btn-primary btn-sm">
                                            Detail nabídky
                                        </a>
                                    </span>
                 */


        Set<String> urls = Collections.synchronizedSet(new HashSet<>());

        List<WebElement> divElements = driverManager.getDriver().findElements(By.tagName("article"));

        for (WebElement divElem : divElements) {

            SupplierOperation<Optional<String>> getUrlOperation = () -> {

                if (SeleniumUtils.hasAttribute(divElem, "class")) {
                    if (SeleniumUtils.getAttributeValue(divElem, "class").equals("product product--apartment product--apartment--with-hover has-ctas")) {

                        String divText = divElem.getText().trim();

                        if (divText.contains("Detail nabídky")) {

                            List<WebElement> aElems = divElem.findElements(By.tagName("a"));

                            for (WebElement aElement : aElems) {
                                if (SeleniumUtils.hasAttribute(aElement, "href")) {
                                    String url = SeleniumUtils.getAttributeValue(aElement, "href");
                                    if (url.length() > 0 && ! url.contains("nove-bydleni")) {
                                        return Optional.ofNullable(url);
                                    }
                                }
                            }

                        }

                    }
                }

                return Optional.empty();
            };


            try {
                Optional<String> urlOp = OperationExecutor.attemptExecute(getUrlOperation, 50, 1000, log, "BR: ",
                        Optional.of("Failed to parse url, KEEP TRYING ... "), "FAILED to parse url.", Optional.empty());
                urlOp.ifPresent(urls::add);

            } catch (ProcessingException e) {
                log.error(e);
            }

        }

        return urls;
    }

    @Override
    public void reset() {
        // not needed
    }
}
