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

package random;


import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import ret.appcore.json.Json;

import java.io.File;
import java.time.LocalTime;
import java.util.*;

public class RandomTests {


    private int number = 0;


    @Test
    public void benchmarkVolatile() {

        RndClass rndClass = new RndClass(12345L, "Some text");

        long begin = System.currentTimeMillis();
        for (long i = 0; i < 100000000; i++) {
            rndClass.setLongVar(i);
            rndClass.getLongVar();
//            rndClass.getStrVar();
        }
        long end = System.currentTimeMillis();

        System.out.println((end - begin));
    }



    private static class RndClass {

        long longVar;
        String strVar;

        public RndClass(long longVar, String strVar) {
            this.longVar = longVar;
            this.strVar = strVar;
        }

        public long getLongVar() {
            return longVar;
        }

        public void setLongVar(long longVar) {
            this.longVar = longVar;
        }

        public String getStrVar() {
            return strVar;
        }

        public void setStrVar(String strVar) {
            this.strVar = strVar;
        }
    }


    @Test
    public void testSerialisingToJson() {

        List<String> stringList = Arrays.asList("One", "Two");

        System.out.println("Original list: " + stringList);

        Optional<String> json = Json.write(stringList);

        System.out.println("Json: " + json.get());

        Optional<List> parsedStrs = Json.parse(json.get(), List.class);

        System.out.println("Parsed list: " + parsedStrs.get());

    }


    @Test
    public void testSerialisingSingleStringToJson() {

        boolean value = true;

        Optional<String> json = Json.write(value);

        System.out.println("Json: " + json.get());

        Optional<Boolean> parsedStrs = Json.parse(json.get(), Boolean.class);

        System.out.println("Parsed value: " + parsedStrs.get());
    }


    @Test
    public void testSyntax() {

        boolean success = false;
        success |= true;
        System.out.println(success);

        success = true;
        success |= false;
        System.out.println(success);
    }


    @Test
    public void decrInPlace() {
        System.out.println(number);
        number++;
        System.out.println(number);
    }

    @Test
    public void testUUID() {
        System.out.println(UUID.randomUUID().toString());
    }

    @Test
    public void testLocalTime() {
        System.out.println(LocalTime.of(00, 00).toSecondOfDay());
    }

    @Test
    public void test() {
        SomeClass<Void> someClass = new SomeClass<>(null);

    }

    private static class SomeClass<T> {
        T data;

        public SomeClass(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }
    }

    @Test
    public void testTsLength() {
        System.out.println((System.currentTimeMillis() + "").length());
    }

    @Test
    public void testPrinfFullFileNameFromFile() {
        File file = new File("/Users/tzoumas/IdeaProjects/WebScrapers/src/test/java/com/estatetracker/webscrapers/pipe/scraping/inzeraty/parsers/LocationResolverSRTest.java");
        System.out.println(file.getPath());
    }

    @Test
    public void testHashSha256() {
        String sha256Hex = DigestUtils.sha1Hex("https://www.youtube.com/watch?v=Vs-5ZGRly9A");
        System.out.println(sha256Hex);
    }

    @Test
    public void testUncheckedWarning() {
        List strList = new ArrayList<>();
        // This causes unchecked cast warning
//        List<Integer> intList = (List<Integer>) strList;
    }

    @Test
    public void testIncrementing() {
        int remainingAnswers = 0;
        int result = getIncrement();
        System.out.println(result);
        System.out.println(remainingAnswers);
    }

    private int getIncrement() {
        int remainingAnswers = 0;
        remainingAnswers++;
        return remainingAnswers;
    }

}
