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

package ret.webscrapers.random;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ret.appcore.model.enums.WebsiteEnum;

import java.util.Set;

public class RandomTest {

    private static ActorSystem system;

    @BeforeClass
    public static void beforeClass() throws Exception {
        system = ActorSystem.create("TestSystem");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        system.terminate();
        Thread.sleep(100);
    }

    @Test
    public void test() {
        Set<WebsiteEnum> values = Set.of(WebsiteEnum.values());
        for (WebsiteEnum websiteEnum : values) {
            System.out.println(websiteEnum);
        }
    }

    @Test
    public void testActorReceivesSubclassInstance() throws InterruptedException {

        String msg = "message";

        ActorRef alfaActor = system.actorOf(Props.create(ActorAlfa.class, () -> new ActorAlfa()));
        alfaActor.tell(msg, ActorRef.noSender());

        ActorRef betActor = system.actorOf(Props.create(ActorAlfa.class, () -> new ActorAlfa()));
        betActor.tell(msg, ActorRef.noSender());

        Thread.sleep(100);
    }

    private static class ActorAlfa extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(CharSequence.class, charSequence -> {
                        System.out.println("AlfaActor received charSequence");
                    })
                    .build();
        }
    }

    private static class ActorBeta extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(String.class, string -> {
                        System.out.println("BetaActor received string");
                    })
                    .build();
        }
    }


}
