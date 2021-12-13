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

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTests {

    @Test
    public void test_pattern_matching_of_dispozice_bytu() {

        /*
        Prodej bytu 2+1 65 m²
        Prodej bytu 1+kk 29 m²
         */

        String str = "Prodej bytu 2+1 65 m²";

        Pattern rawRateStrs = Pattern.compile("(\\d\\+(kk))|(\\d\\+\\d)");
        Matcher m = rawRateStrs.matcher(str);

        while(m.find()){
            System.out.println(m.group());
        }

    }


    @Test
    public void test_pattern_matching_of_Praha_Cislo() {
        /*
        ulice U družstva Život, Praha 4 - část obce Nusle
        Praha 7
         */

        String str = "Praha 7 - Praha ";

        Pattern rawRateStrs = Pattern.compile("((Praha )\\d)");
        Matcher m = rawRateStrs.matcher(str);

        while(m.find()){
            System.out.println(m.group());
        }

    }

    @Test
    public void test_pattern_area() {

        /*
        	42 m²
         */

        String str1 = "42.0 m²";

        Pattern rawRateStrs = Pattern.compile("(^\\d+)");
        Matcher m = rawRateStrs.matcher(str1);

        m.find();
        System.out.println(m.group());
//        while(m.find()){
//            System.out.println(m.group());
//        }

    }

    @Test
    public void test_pattern_castka() {

        /*
        	42 m²
         */

        String str = "3.090.000,00 Kč";
        String modStr = str.replace(".", "");

        Pattern rawRateStrs = Pattern.compile("(^\\d+)");
        Matcher m = rawRateStrs.matcher(modStr);

        while(m.find()){
            System.out.println(m.group());
        }

    }


    @Test
    public void testParsingGeoLocation() {

        String valueSr = "//mapy.cz/?x=14.340280&y=50.037386&z=18";

        Pattern rawRateStrs = Pattern.compile("y=\\d+.\\d+&");
        Matcher m = rawRateStrs.matcher(valueSr);

        while(m.find()){
            System.out.println(m.group());
        }

    }

    @Test
    public void testParsingLocationFromSrHeader() {

        //(?<=, )(\s|\p{L})+(?= - část obce)  : matches mesto


        //(?<=-)(, (\p{L}|\s)+)
        //(?<= - )\p{L}+    : matches "část"
        String header = "Ulrichovo náměstí, Hradec Králové - část obce Hradec Králové, okres xy";

    }

    @Test
    public void testFindingQuestionMark() {
//        String url = "https://reality.idnes.cz/detail/prodej/byt/praha-8-primatorska/5d70fd0d37ba4d278e5532a3/?s-et=flat&s-ot=sale&s-l=SPRAVNI_OBVOD-86&page=4";
        String url = "https://reality.idnes.cz/detail/prodej/byt/praha-8-voctarova/5d8dfcb337ba4d039578c7b3/";
        int idx = url.indexOf("?");
        System.out.println(idx);
    }

}
