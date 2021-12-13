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

package ret.webscrapers.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Deprecated
public abstract class ShuttableBaseActor extends AbstractActor {

    private ActorRef shutdownRequester = null;
    private int expChildShutdownCount = 0;
    private int childShutdownCompleteCount = 0;
    private Logger LOG = LogManager.getLogger(ShuttableBaseActor.class);

    protected void setSenderAsShutdownRequester() {
        this.shutdownRequester = getSender();
    }


    protected void onShutdownMsg(ShutdownMsg msg) {
        setSenderAsShutdownRequester();
        for (ActorRef child : getContext().getChildren()) {
            expChildShutdownCount++;
            child.tell(msg, getSelf());
        }
        if (expChildShutdownCount == 0) {
            shutdownSelf();
            LOG.info("No children to shutdown for parent actor: {} of name: {}. Shutting itself.", getClass().getSimpleName(), getSelf().path().name());
        }
    }


    protected void onChildActorShutdownCompleteMsg(ChildActorShutdownCompleteMsg msg) {
        childShutdownCompleteCount++;
        if (childShutdownCompleteCount == expChildShutdownCount) {
            LOG.info("Shut down all child actors gracefully for parent actor: {} of name: {}. Shutting itself.", getClass().getSimpleName(), getSelf().path().name());
            shutdownSelf();
        }
    }

    private void shutdownSelf() {
        getContext().stop(getSelf());
        shutdownRequester.tell(ChildActorShutdownCompleteMsg.getInstance(), getSelf());
    }

}
