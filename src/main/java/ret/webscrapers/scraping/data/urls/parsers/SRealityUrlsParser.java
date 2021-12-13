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
import aaanew.utils.OperationExecutor;
import aaanew.utils.ProcessingException;
import aaanew.utils.SeleniumUtils;
import aaanew.utils.SupplierOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.*;

public class SRealityUrlsParser extends DriverOperatorBase<WebDriver> implements UrlsParser {

    private static final Logger log = LogManager.getLogger();


    public SRealityUrlsParser(SeleniumDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public void goToPage(String pageUrl) {
        driverManager.getDriver().get(pageUrl);
        SeleniumUtils.sleep(1000);
    }

    @Override
    public boolean hasMoreUrlsToScrape() {
        /*
            <p class="status-text ng-binding" ng-show="!properties.length &amp;&amp; wasDataLoaded &amp;&amp; notFound &amp;&amp; !customMsg">Zadanému filtru neodpovídá žádný inzerát!</p>
         */
        SupplierOperation<Boolean> hasMoreLinksOperation = () -> {
            List<WebElement> statusElem = driverManager.getDriver().findElements(By.tagName("p"));
            for (WebElement webElement : statusElem) {
                if (SeleniumUtils.hasAttribute(webElement, "class")) {
                    String attributeValue = SeleniumUtils.getAttributeValue(webElement, "class");
                    if (attributeValue.equals("status-text ng-binding")) {
                        if (webElement.getText().equals("Zadanému filtru neodpovídá žádný inzerát!")) {
                            return false;
                        }
                    }
                }
            }
            return true;
        };

        boolean hasMoreLinks = false;
        try {
            hasMoreLinks = OperationExecutor.attemptExecute(hasMoreLinksOperation, 50, 5000, log, "SR: ",
                    Optional.of("FAILED trying to check if there are more links. Keep trying"), "FAILED checking if there are more links.", Optional.of("SUCCESS checking if there are more links."));
        } catch (ProcessingException e) {
            log.error("Error while trying to check if there are more links.");
        }

        if (!hasMoreLinks) {

        }

        return hasMoreLinks;
    }


    @Override
    public Set<String> scrapePropertyUrls() {

        log.debug("Entered scrapePropertyUrls");

        Set<String> urls = Collections.emptySet();

        Optional<WebElement> estatesListElementOp = getEstatesListElement();

        if (estatesListElementOp.isPresent()) {

            SupplierOperation<Set<String>> getPropertyUrlsOperation = () -> {
                Set<String> urlsAccumulator = Collections.synchronizedSet(new HashSet<>());
                List<WebElement> aElements = estatesListElementOp.get().findElements(By.tagName("a"));
                for (WebElement aElement : aElements) {
                    if (SeleniumUtils.hasAttribute(aElement, "ng-href")) {
                        String propertyUrl = aElement.getAttribute("ng-href");
                        if (propertyUrl.contains("detail")) {
                            urlsAccumulator.add("https://www.sreality.cz" + propertyUrl);
                        }
                    }
                }
                return urlsAccumulator;
            };

            try {
                urls = OperationExecutor.attemptExecute(getPropertyUrlsOperation, 50, 5000, log, "SR: ",
                        Optional.of("FAILED get PropertyUrls. Keep trying"), "FAILED getting PropertyUrls.", Optional.of("SUCCESS getting PropertyUrls."));
            } catch (ProcessingException e) {
                log.error("Error while trying to get PropertyUrls.");
            }
        }

        return urls;
    }



    private Optional<WebElement> getEstatesListElement() {
    /*
        <div ng-if="estatesResource" class="ng-scope">
     */
        List<WebElement> divElements = driverManager.getDriver().findElements(By.tagName("div"));

        SupplierOperation<Optional<WebElement>> getEstatesListOperation = () -> {
            for (WebElement divElement : divElements) {
                if (SeleniumUtils.hasAttribute(divElement, "ng-if")) {
                    String attributeVal = divElement.getAttribute("ng-if");
                    if (attributeVal.equals("estatesResource")) {
                        log.debug("Found estatesResource element");
                        return Optional.of(divElement);
                    }
                }
            }
            log.warn("Did not find estatesResource element");
            return Optional.empty();
        };

        Optional<WebElement> result = Optional.empty();
        try {
            result = OperationExecutor.attemptExecute(getEstatesListOperation, 50, 5000, log, "SR: ",
                    Optional.of("FAILED trying to get EstatesListElement. Keep trying"), "FAILED getting EstatesListElement.", Optional.of("SUCCESS getting EstatesListElement."));
        } catch (ProcessingException e) {
            log.error("Error while trying to get EstatesListElement.");
        }

        return result;
    }


    @Override
    public void reset() {
        // not needed ...
    }
}
