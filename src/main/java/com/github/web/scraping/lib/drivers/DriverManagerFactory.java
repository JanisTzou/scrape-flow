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

package com.github.web.scraping.lib.drivers;


import com.github.web.scraping.lib.drivers.lifecycle.QuitAfterIdleInterval;
import com.github.web.scraping.lib.drivers.lifecycle.RestartDriverAfterInterval;
import com.github.web.scraping.lib.throttling.model.ScrapedDataType;
import ret.appcore.model.enums.WebsiteEnum;
import com.github.web.scraping.lib.AppConfig;

@Deprecated
public class DriverManagerFactory {

    private final SeleniumDriversFactory seleniumDriversFactory;
    private final HtmlUnitDriversFactory htmlUnitDriversFactory;


    public DriverManagerFactory(SeleniumDriversFactory seleniumDriversFactory, HtmlUnitDriversFactory htmlUnitDriversFactory) {
        this.seleniumDriversFactory = seleniumDriversFactory;
        this.htmlUnitDriversFactory = htmlUnitDriversFactory;
    }


    public DriverManager<?> newDriverManager(WebsiteEnum website, ScrapedDataType scrapedDataType) {

        switch (scrapedDataType) {
            case URLS:
                switch (website) {
                    case SREALITY:
                    case BEZ_REALITKY:
                        return new SeleniumDriverManager(
                                scrapedDataType,
                                new RestartDriverAfterInterval(AppConfig.maxIntervalSinceLastDriverRestart.toMillis()),
                                new QuitAfterIdleInterval(AppConfig.maxIdleDriverInterval.toMillis()),
                                seleniumDriversFactory);
                    case IDNES_REALITY:
                        return new HtmlUnitDriverManager(scrapedDataType, htmlUnitDriversFactory);
                    default:
                }
                break;

            case INZERAT:
                switch (website) {
                    case SREALITY:
                    case IDNES_REALITY:
                        return new HtmlUnitDriverManager(scrapedDataType, htmlUnitDriversFactory);
                    case BEZ_REALITKY:
                        return new SeleniumDriverManager(
                                scrapedDataType,
                                new RestartDriverAfterInterval(AppConfig.maxIntervalSinceLastDriverRestart.toMillis()),
                                new QuitAfterIdleInterval(AppConfig.maxIdleDriverInterval.toMillis()),
                                seleniumDriversFactory);
                    default:
                }
                break;

            case IMAGES:
                switch (website) {
                    case SREALITY:
                    case BEZ_REALITKY:
                        return new SeleniumDriverManager(
                                scrapedDataType,
                                new RestartDriverAfterInterval(AppConfig.maxIntervalSinceLastDriverRestart.toMillis()),
                                new QuitAfterIdleInterval(AppConfig.maxIdleDriverInterval.toMillis()),
                                seleniumDriversFactory);
                    case IDNES_REALITY:
                        return new HtmlUnitDriverManager(scrapedDataType, htmlUnitDriversFactory);
                    default:
                }
            default:
        }

        throw new IllegalArgumentException(String.format("No implementation of DriverManager for website '%s' and scrapedDataType '%s'", website, scrapedDataType));
    }

}
