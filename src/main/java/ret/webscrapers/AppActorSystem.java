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

package ret.webscrapers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Scheduler;
import scala.concurrent.ExecutionContext;

import java.time.Duration;

public class AppActorSystem {

    private final ActorSystem system;

    public AppActorSystem(ActorSystem system) {
        this.system = system;
    }

    public ActorSystem system() {
        return system;
    }

    public Scheduler scheduler() {
        return system.scheduler();
    }

    public ExecutionContext executionContext() {
        return system.dispatcher();
    }

    public Cancellable scheduleOnce(Object message, ActorRef sender, ActorRef receiver, Duration sendAfterDuration) {
        Runnable task = () -> receiver.tell(message, sender);
        return system.scheduler().scheduleOnce(sendAfterDuration, task, system.dispatcher());
    }

    public void shutdown() {
        system.terminate();
    }
}
