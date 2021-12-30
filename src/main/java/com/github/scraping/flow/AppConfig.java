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

package com.github.scraping.flow;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.logging.Level;

@Deprecated
public class AppConfig {

    private final static Config conf = ConfigFactory.load();


    // IMPORTANT: has to be at the top - other configurations depend on this ...:
    // MAC or WIN
    public static final boolean isMac = conf.getBoolean("chromeDriver.isMac");

    // file sys - base dirs
    public static final String baseDirWIN = conf.getString("app.fileSys.baseDirWIN");
    public static final String baseDirMAC = conf.getString("app.fileSys.baseDirMAC");

    // file sys - chrome driver
    private static final String chromeDriverPath = baseDir() + File.separator + conf.getString("app.fileSys.chromeDriverPath");
    private static final String chromeDriverPathMac = baseDir() + File.separator + conf.getString("app.fileSys.chromeDriverPathMAC");


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


    public static void turnOffHtmlUnitLogger() {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
    }

}
