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

package ret.webscrapers.http;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class HeartbeatActor extends AbstractActor {

    private final static Logger log = LogManager.getLogger(HeartbeatActor.class);
    private final HttpClient httpClient;
    private Cancellable nextPingScheduled;
    private final Duration heartbeatPeriod;

    public HeartbeatActor(HttpClient httpClient, Duration heartbeatPeriod) {
        this.httpClient = httpClient;
        this.heartbeatPeriod = heartbeatPeriod;
    }

    public static Props props(HttpClient httpClient, Duration heartbeatPeriod) {
        return Props.create(HeartbeatActor.class, () -> new HeartbeatActor(httpClient, heartbeatPeriod));
    }

    // message
    private static class HeartbeatMsg {}

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(HeartbeatMsg.class, msg -> onHeartbeatMsg(msg))
                .build();
    }

    private void onHeartbeatMsg(HeartbeatMsg heartbeatMsg) {
        log.debug("Got HeartbeatMsg");
        httpClient.sendHeartbeat();
        Runnable pingTask = () -> {getSelf().tell(heartbeatMsg, getSelf());};
        ActorSystem system = getContext().getSystem();
        nextPingScheduled = system.scheduler().scheduleOnce(heartbeatPeriod, pingTask, system.dispatcher());
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        getSelf().tell(new HeartbeatMsg(), getSelf());
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        if (nextPingScheduled != null && !nextPingScheduled.isCancelled()) {
            nextPingScheduled.cancel();
        }
    }
}
