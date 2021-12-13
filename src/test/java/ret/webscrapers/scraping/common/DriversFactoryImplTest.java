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

package ret.webscrapers.scraping.common;

import aaanew.drivers.SeleniumDriversFactory;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import ret.webscrapers.AppConfig;

public class DriversFactoryImplTest {

    @Test
    public void test_going_to_blank_page_after_using_driver() throws InterruptedException {

        SeleniumDriversFactory driversFactory = new SeleniumDriversFactory(AppConfig.getChromeDriverDir(),false);

        WebDriver webDriver = driversFactory.startDriver();

        webDriver.get("https://google.com");

        System.out.println(webDriver.getTitle());

        webDriver.quit();
        Thread.sleep(2000);

    }

}
