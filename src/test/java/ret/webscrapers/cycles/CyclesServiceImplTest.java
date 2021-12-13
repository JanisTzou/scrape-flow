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

package ret.webscrapers.cycles;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalTime;

import static org.junit.Assert.assertEquals;

public class CyclesServiceImplTest {

    @Test
    public void calcTimeTillNextStart() {

        CyclesService cyclesService = new CyclesServiceImpl(null);
        LocalTime now = LocalTime.of(19, 50);
        Duration duration = cyclesService.calcTimeTillNextStart(1, now);
        assertEquals(600, duration.getSeconds());

        now = LocalTime.of(20, 50);
        duration = cyclesService.calcTimeTillNextStart(1, now);
        assertEquals(0, duration.getSeconds());

        now = LocalTime.of(00, 01);
        duration = cyclesService.calcTimeTillNextStart(1, now);
        assertEquals(71940, duration.getSeconds());
    }
}
