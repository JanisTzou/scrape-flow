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

package ret.webscrapers.data.send;

import akka.actor.ActorRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.webscrapers.http.ConnectionListener;

// This class is needed as we need to be able to inform the DataSender about connection status change so
// we can preserve the order of the messages - new incoming ones and the saved ones that are to be send soon ...
public class DataSenderConnectionManager implements ConnectionListener {

    private final ActorRef dataSenderActor;
    private static Logger log = LogManager.getLogger(DataSenderConnectionManager.class);

    public DataSenderConnectionManager(ActorRef dataSenderActor) {
        this.dataSenderActor = dataSenderActor;
    }

    @Override
    public void onConnected() {
        log.info("Sending ReconnectedMsg to dataSenderActor.");
        dataSenderActor.tell(new DataSenderActor.ReconnectedMsg(), ActorRef.noSender());
    }

    @Override
    public void onDisconnected() {
        log.info("Sending DisconnectedMsg to dataSenderActor.");
        dataSenderActor.tell(new DataSenderActor.DisconnectedMsg(), ActorRef.noSender());
    }

}
