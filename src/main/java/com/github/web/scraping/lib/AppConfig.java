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

package com.github.web.scraping.lib;


import com.github.web.scraping.lib.throttling.model.ScrapedDataType;
import com.github.web.scraping.lib.throttling.model.ScraperSettings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.enums.WebsiteEnum;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Deprecated
public class AppConfig {

    private final static Config conf = ConfigFactory.load();
    private static final Logger log = LogManager.getLogger(AppConfig.class);


    // IMPORTANT: has to be at the top - other configurations depend on this ...:
    // MAC or WIN
    public static final boolean isMac               = conf.getBoolean("chromeDriver.isMac");

    // scraper client
    public static final int scraperClientId     = conf.getInt("app.scraperClientId");

    // file sys - base dirs
    public static final String baseDirWIN           = conf.getString("app.fileSys.baseDirWIN");
    public static final String baseDirMAC           = conf.getString("app.fileSys.baseDirMAC");

    // file sys - chrome driver
    private static final String chromeDriverPath    = baseDir() + File.separator + conf.getString("app.fileSys.chromeDriverPath");
    private static final String chromeDriverPathMac = baseDir() + File.separator + conf.getString("app.fileSys.chromeDriverPathMAC");

    // file sys - jsons + images
    public static final String jsonsDir             = baseDir() + File.separator + conf.getString("app.fileSys.jsonsDir");
    public static final String imagesDir            = baseDir() + File.separator + conf.getString("app.fileSys.imagesDir");

    // file sys - websegments
    public static final String websegmentsDir       = baseDir() + File.separator + conf.getString("app.fileSys.websegmentsDir");
    public static final String websegmentBeingProcessedFileName  = websegmentsDir + File.separator + conf.getString("app.fileSys.websegmentBeingProcessedFileName");


    // selenium drivers
    public static final boolean headlessDriver                      = conf.getBoolean("chromeDriver.headlessDriver");
    public static final Duration checkForIdleDriverInterval         = conf.getDuration("chromeDriver.checkForIdleDriverInterval");
    public static final Duration maxIdleDriverInterval              = conf.getDuration("chromeDriver.maxIdleDriverInterval");
    public static final Duration maxIntervalSinceLastDriverRestart  = conf.getDuration("chromeDriver.maxIntervalSinceLastDriverRestart");

    // scraping - web responsiveness
    public static final Duration responsivenessCheckInterval    = conf.getDuration("scraping.websResponsiveness.responsivenessCheckInterval");
    public static final int minTotalScrapedItemsCountToConsider = conf.getInt("scraping.websResponsiveness.minTotalScrapedItemsCountToConsider");
    public static final Duration maxStatsAge                    = conf.getDuration("scraping.websResponsiveness.maxStatsAge");

    // websegments
    public static final Duration lastProcessedSegmentsAgeToConsider = conf.getDuration("scraping.websegments.lastProcessedSegmentsAgeToConsider");

    // cycles
    public static final boolean calculateNextCycleStart = conf.getBoolean("scraping.cycles.calculateNextCycleStart");
    public static final Duration startNewCycleDelay = conf.getDuration("scraping.cycles.startNewCycleDelay");

    public static boolean imageScrapingEnabled = conf.getBoolean("scraping.imageScrapingEnabled");

    public static boolean dontSendNewDataWhileUnsentFilesExist = conf.getBoolean("scraping.ipc.dontSendNewDataWhileUnsentFilesExist");


    // scraping - scraper settings
    private static final List<? extends Config> scrapersSettings = conf.getConfigList("scraping.scrapersSettings");


    public static List<ScraperSettings> getScrapersSettings() {

        List<ScraperSettings> settings = new CopyOnWriteArrayList<>();

        try {
            for (Config config : scrapersSettings) {
                String websiteStr = config.getString("website");
                WebsiteEnum website = WebsiteEnum.valueOf(websiteStr);
                String statsTypeStr = config.getString("dataType");
                ScrapedDataType statsType = ScrapedDataType.valueOf(statsTypeStr);
                int minScrapersCount = config.getInt("minScrapersCount");
                int maxScrapersCount = config.getInt("maxScrapersCount");
                int lowerItemScrapingDurationMillis = config.getInt("lowerItemScrapingDurationMillis");
                int upperItemScrapingDurationMillis = config.getInt("upperItemScrapingDurationMillis");
                if (lowerItemScrapingDurationMillis > upperItemScrapingDurationMillis) {
                    throw new IllegalArgumentException("Wrong configuration: lowerItemScrapingDurationMillis > upperItemScrapingDurationMillis !!!");
                }
                settings.add(new ScraperSettings(website, statsType, minScrapersCount, maxScrapersCount, lowerItemScrapingDurationMillis, upperItemScrapingDurationMillis));
            }
        } catch (IllegalArgumentException e) {
            log.error("Error initialisong ScrapersSettings", e);
        }

        checkAllStatsTypesAndWebsitesHaveSettings(settings);
        return settings;
    }


    private static void checkAllStatsTypesAndWebsitesHaveSettings(List<ScraperSettings> settings) {
        boolean anyMissing = false;
        List<ScrapedDataType> excluded = Arrays.asList(ScrapedDataType.URLS);
        List<WebsiteEnum> websites = Arrays.stream(WebsiteEnum.values()).filter(websiteEnum -> !websiteEnum.equals(WebsiteEnum.UNKNOWN)).collect(Collectors.toList());
        for (WebsiteEnum website : websites) {
            for (ScrapedDataType scrapedDataType : ScrapedDataType.values()) {
                if (!excluded.contains(scrapedDataType)) {
                    Optional<ScraperSettings> settingsOp = settings.stream().filter((ScraperSettings stats) -> stats.getWebsite().equals(website) && stats.getType().equals(scrapedDataType)).findFirst();
                    if (!settingsOp.isPresent()) {
                        anyMissing = true;
                        log.error("Missing scraper configuration for website: '{}' and scrapedDataType: '{}'", website, scrapedDataType);
                    }
                }
            }
        }
        if (anyMissing) {
            throw new IllegalStateException("Found that ScrapersSettings were not created for some websites and/or statsTypes combinations.");
        }
    }


    public static String baseDir() {
        if (isMac) {
            return baseDirMAC;
        } else {
            return baseDirWIN;
        }
    }


    public static String getChromeDriverDir() {
        if (isMac) {
            return chromeDriverPathMac;
        } else {
            return chromeDriverPath;
        }
    }


    public static void printConfig() {
        log.info("CONFIG: isMac = '{}'", isMac);
        log.info("CONFIG: scraperClientId = '{}'", scraperClientId);
        log.info("CONFIG: baseDir() = '{}'", baseDir());
        log.info("CONFIG: chromeDriverDir = '{}'", getChromeDriverDir());
        log.info("CONFIG: jsonsDir = '{}'", jsonsDir);
        log.info("CONFIG: imagesDir = '{}'", imagesDir);
        log.info("CONFIG: headlessDriver = '{}'", headlessDriver);
        log.info("CONFIG: checkForIdleDriverInterval = '{}' millis", checkForIdleDriverInterval.toMillis());
        log.info("CONFIG: maxIdleDriverInterval = '{}' millis", maxIdleDriverInterval.toMillis());
        log.info("CONFIG: maxIntervalSinceLastDriverRestart = '{}' millis", maxIntervalSinceLastDriverRestart.toMillis());
        log.info("CONFIG: responsivenessCheckInterval = '{}' millis", responsivenessCheckInterval.toMillis());
        log.info("CONFIG: minTotalScrapedItemsCountToConsider = '{}'", minTotalScrapedItemsCountToConsider);
        log.info("CONFIG: maxStatsAge = '{}' millis", maxStatsAge.toMillis());
        for (ScraperSettings scrapersSetting : getScrapersSettings()) {
            log.info("CONFIG: ScrapersSettings = '{}'", scrapersSetting);
        }
        log.info("CONFIG: imageScrapingEnabled = '{}'", imageScrapingEnabled);
        log.info("CONFIG: calculateNextCycleStart = '{}'", calculateNextCycleStart);
        log.info("CONFIG: startNewCycleDelay = '{}'", startNewCycleDelay);
    }

    public static void turnOffHtmlUnitLogger() {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
    }

}
