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

package ret.webscrapers.messages;

import java.util.Arrays;
import java.util.List;

/**
 * Currently used just for statistics purposes ...
 */
public enum FinishedStatus {

    SUCCESS_INZERAT_AND_IMAGES,
    SUCCESS_INZERAT,
    NOT_TO_SCRAPE,
    FAILED;

    private final static List<FinishedStatus> SUCCESSFUL_STATUSES = Arrays.asList(SUCCESS_INZERAT_AND_IMAGES, SUCCESS_INZERAT, NOT_TO_SCRAPE);

    private final static List<FinishedStatus> FAILED_STATUSES = Arrays.asList(FAILED);

    public static boolean consideredSuccess(FinishedStatus status) {
        return SUCCESSFUL_STATUSES.contains(status);
    }

    public static boolean consideredFailure(FinishedStatus status) {
        return FAILED_STATUSES.contains(status);
    }

}
