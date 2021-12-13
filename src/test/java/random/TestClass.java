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

public class TestClass {

    int cislo1;
    int cislo2;
    String myText;

    public int getCislo1() {
        return cislo1;
    }

    public void setCislo1(int cislo1) {
        this.cislo1 = cislo1;
    }

    public int getCislo2() {
        return cislo2;
    }

    public void setCislo2(int cislo2) {
        this.cislo2 = cislo2;
    }

    public String getMyText() {
        return myText;
    }

    public void setMyText(String myText) {
        this.myText = myText;
    }

    public TestClass(int cislo1, int cislo2, String myText) {
        this.cislo1 = cislo1;
        this.cislo2 = cislo2;
        this.myText = myText;
    }

}
