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

import com.github.web.scraping.lib.demos.TeleskopExpressDeCrawler;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class App {

    public static void main(String[] args) {

//        new IFortunaCzCrawler().start();
//        System.out.println("-".repeat(150));
//        new AktualneCzCrawler().start();
//        System.out.println("-".repeat(150));
//        new SupraDalekohledyCzCrawler().start();
        new TeleskopExpressDeCrawler().start();

    }


}
