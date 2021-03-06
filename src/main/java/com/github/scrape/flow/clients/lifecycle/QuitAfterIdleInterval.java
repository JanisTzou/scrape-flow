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

package com.github.scrape.flow.clients.lifecycle;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuitAfterIdleInterval implements DriverQuitStrategy {

    private final long maxIdleIntervalInMillis;

    @Override
    public boolean shouldQuit(long driverLastUsedTs) {
        long now = System.currentTimeMillis();
        return isIdleTimeLimitExceeded(driverLastUsedTs, now);
    }

    private boolean isIdleTimeLimitExceeded(long driverLastUsedTs, long now) {
        return (now - (driverLastUsedTs + maxIdleIntervalInMillis)) > 0;
    }
}
