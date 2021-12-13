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

package com.github.web.scraping.lib.throttling;


import com.github.web.scraping.lib.throttling.model.ThrottleCommand;
import ret.appcore.model.enums.WebsiteEnum;

@Deprecated
public class ThrottleScrapersMsg {

    private final WebsiteEnum website;
    private ThrottleCommand throttleCommand;

    public ThrottleScrapersMsg(WebsiteEnum website, ThrottleCommand throttleCommand) {
        this.website = website;
        this.throttleCommand = throttleCommand;
    }

    public WebsiteEnum getWebsite() {
        return website;
    }

    public ThrottleCommand getThrottleCommand() {
        return throttleCommand;
    }


    @Override
    public String toString() {
        return "ThrottleScrapersMsg{" +
                "website=" + website +
                ", throttleCommand=" + throttleCommand +
                '}';
    }
}
