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

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ActorTest {

    private static ActorSystem system = ActorSystem.create("TEST_ACT_SYS");
    private static String ERR_TRIGGER_MSG = "CAUSE_ERROR";
    private final Map<ActorRef, String> actorToStringMap = new HashMap<>();


    @Test
    public void test_that_ActorRefs_Equal_After_Restart() throws InterruptedException {
        ActorRef testActor = system.actorOf(TestActor.props(), "myName");
        System.out.println(testActor.path().name());
        actorToStringMap.put(testActor, "value");
        System.out.println(testActor);
        testActor.tell(ERR_TRIGGER_MSG, ActorRef.noSender());
        Thread.sleep(200);
        System.out.println(actorToStringMap.get(testActor));
    }

    @Test
    public void test_Actor_Receives_Unknown_Message() throws InterruptedException {
        ActorRef testActor = system.actorOf(TestActor.props());
        testActor.tell("SOME MESSAGE", ActorRef.noSender());
        Thread.sleep(100);
    }

    private static class TestActor extends AbstractActor {

        static Props props() {
            return Props.create(TestActor.class, TestActor::new);
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals(ERR_TRIGGER_MSG, msg -> onCauseError(msg))
                    .matchAny(msg -> unhandled(msg))
                    .build();
        }

        private void onCauseError(String msg) {
            int i = 1 / 0;
        }

        public void unhandled(Object msg) {
            System.out.println("Got unhadled message");
        }

        @Override
        public void preStart() throws Exception {
            super.preStart();
            System.out.println("Restarting");
        }
    }
}
