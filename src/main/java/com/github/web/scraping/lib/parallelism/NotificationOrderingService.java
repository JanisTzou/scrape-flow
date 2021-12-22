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

package com.github.web.scraping.lib.parallelism;

public class NotificationOrderingService {


    // TODO implementation should buffer data before it is sent to listeners so that correct order is achieved ... as if the data was was scraped sequentially ...
    //  it should be possible to turn off by clients ... for faster results and no buffering

    // use a sorted datastructure at whose one end will be the items needing to go first always ...
    // use a Comparable of the step order of a group of spawned steps ... or the smallest one should be enough ...

}
