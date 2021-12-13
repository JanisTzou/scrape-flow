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

package ret.webscrapers.http;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class RetApiConfig {

    private final static Config retApiConf = ConfigFactory.load("ret_api.conf");
    private final static Config applicationConf = ConfigFactory.load("application.conf");

    private static final Logger log = LogManager.getLogger(RetApiConfig.class);

    private static final String host  = retApiConf.getString("base.host");
    private static final String port  = retApiConf.getString("base.port");

    // retApp - URLs
    private static final String context  = retApiConf.getString("retApp.appContext");
    private static final String apiURL  = retApiConf.getString("retApp.apiUrl");
    private static final String baseURL = host + ":" + port + "/" + context + "/" + apiURL;

    // segments
    private static final String allWebSegmentsURLPattern = baseURL + "/" + retApiConf.getString("retApp.allWebSegmentsURLPattern");
    public static final String stolenWebSegmentsURL = baseURL + "/" + retApiConf.getString("retApp.stolenWebSegmentsURL");
    public static final Duration stolenWebSegmentsPullPeriod = retApiConf.getDuration("retApp.stolenWebSegmentsPullPeriod");

    public static String makeAllWebSegmentsUrl(int scraperClientId) {
        return String.format(allWebSegmentsURLPattern, scraperClientId);
    }

    // inzeraty
    public static final String notScrapedInzeratyListURL = baseURL + "/" + retApiConf.getString("retApp.notScrapedInzeratyListURL");
    public static final String noImagesInzeratIdentifierListURL = baseURL + "/" + retApiConf.getString("retApp.notScrapedImagesInzeratyListURL");
    public static final String shouldScrapeInzeratURLPattern = baseURL + "/" + retApiConf.getString("retApp.shouldScrapeInzeratURLPattern");

    public static String makeShouldScrapeInzeratURL(String inzeratUUID) {
        return String.format(shouldScrapeInzeratURLPattern, inzeratUUID);
    }

    // images
    private static final String shouldScrapeImageURLPattern = baseURL + "/" + retApiConf.getString("retApp.shouldScrapeImageURLPattern");

    public static String makeShouldScrapeImageURL(String inzeratUUID) {
        return String.format(shouldScrapeImageURLPattern, inzeratUUID);
    }

    // data
    public static final String inzeratImagesDataURL  = baseURL + "/" + retApiConf.getString("retApp.inzeratImagesDataURL");
    public static final String finishedWebsegmentDataURL  = baseURL + "/" + retApiConf.getString("retApp.finishedWebsegmentDataURL");
    public static final String startedCycleDataURL = baseURL + "/" + retApiConf.getString("retApp.startedCycleDataURL");
    public static final String finishedCycleDataURL  = baseURL + "/" + retApiConf.getString("retApp.finishedCycleDataURL");

    // other / system
    public static final String heartbeatURLPattern = baseURL + "/" + retApiConf.getString("retApp.heartbeatURL");
    public static final Duration heartbeatPeriod = retApiConf.getDuration("retApp.heartbeatPeriod");

    // services
    public static final Duration servicesUpdateRetryPeriod = retApiConf.getDuration("services.updateRetryPeriod");
    public static final Duration servicesUpdatePeriod = retApiConf.getDuration("services.updatePeriod");

    // security
    public static final String adminUserName = applicationConf.getString("retApp.security.adminName");
    public static final String adminPassword = applicationConf.getString("retApp.security.adminPassword");

    public static void printConfig() {
        log.info("RET API CONFIG: host = '{}'", host);
        log.info("RET API CONFIG: port = '{}'", port);
        log.info("RET API CONFIG: context = '{}'", context);
        log.info("RET API CONFIG: baseURL = '{}'", baseURL);
        log.info("RET API CONFIG: allWebSegmentsURLPattern = '{}'", allWebSegmentsURLPattern);
        log.info("RET API CONFIG: stolenWebSegmentsURL = '{}'", stolenWebSegmentsURL);
        log.info("RET API CONFIG: stolenWebSegmentsPullPeriod = '{}'", stolenWebSegmentsPullPeriod);
        log.info("RET API CONFIG: notScrapedInzeratyListURL = '{}'", notScrapedInzeratyListURL);
        log.info("RET API CONFIG: notScrapedImagesInzeratyListURL = '{}'", noImagesInzeratIdentifierListURL);
        log.info("RET API CONFIG: shouldScrapeInzeratURLPattern = '{}'", shouldScrapeInzeratURLPattern);
        log.info("RET API CONFIG: inzeratImagesDataURL = '{}'", inzeratImagesDataURL);
        log.info("RET API CONFIG: finishedWebsegmentDataURL = '{}'", finishedWebsegmentDataURL);
        log.info("RET API CONFIG: startedCycleDataURL = '{}'", startedCycleDataURL);
        log.info("RET API CONFIG: finishedCycleDataURL = '{}'", finishedCycleDataURL);
        log.info("RET API CONFIG: heartbeatURL = '{}'", heartbeatURLPattern);
        log.info("RET API CONFIG: heartbeatPeriod = '{}' millis", heartbeatPeriod.toMillis());
        log.info("RET API CONFIG: servicesUpdateRetryPeriod = '{}' millis", servicesUpdateRetryPeriod.toMillis());
        log.info("RET API CONFIG: servicesUpdatePeriod = '{}' millis", servicesUpdatePeriod.toMillis());
        log.info("RET API CONFIG: admin username = {}", adminUserName);
        log.info("RET API CONFIG: admin password = {}", adminPassword);
    }
}
