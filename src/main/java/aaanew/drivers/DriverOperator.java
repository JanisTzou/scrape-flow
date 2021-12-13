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

package aaanew.drivers;

public interface DriverOperator {

    boolean terminateDriver();

    boolean quitDriverIfIdle();

    void restartDriverImmediately();

    boolean restartDriverIfNeeded();

    /**
     * For Selenium this should be restarted. For HtmlUnit this should quit the WebClient ...
     * @return
     */
    boolean restartOrQuitDriverIfNeeded();

    void goToDefaultPage();

}
