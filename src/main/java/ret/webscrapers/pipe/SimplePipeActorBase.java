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

package ret.webscrapers.pipe;

import akka.actor.ActorRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.webscrapers.actors.ActorUtils;
import ret.webscrapers.messages.PipeMsg;

import java.util.Optional;

public abstract class SimplePipeActorBase<T extends PipeMsg> extends PipeActorBase<T> {

    private static Logger log = LogManager.getLogger(SimplePipeActorBase.class);

    protected final SimpleRequestersMapper requestersMapper;

    public SimplePipeActorBase(SimpleRequestersMapper requestersMapper) {
        this.requestersMapper = requestersMapper;
    }

    protected void setSenderAsRequestor(Optional<String> inzeratIdentifier) {
        ActorRef requestor = getSender();
        String requesterName = ActorUtils.getName(requestor);
        log.info("{}: Setting actor as requestor: {}; inzerat: {}", selfName, ActorUtils.getName(requestor), inzeratIdentifier.orElse("N/A"));
        requestersMapper.put(getSelf(),requestor);
    }


    protected void sendToPrevPipeActor(Object message, Optional<String> inzeratIdentifier) {
        Optional<ActorRef> requesterOp = requestersMapper.getRequesterFor(getSelf());
        if (requesterOp.isPresent()) {
            log.info("{}: Sending msg back to requestor: {}; inzerat: {}", selfName, ActorUtils.getName(requesterOp.get()), inzeratIdentifier.orElse("N/A"));
            requesterOp.get().tell(message, getSelf());
        } else {
            log.error("{}: Failed to find requesterActor ; Inzerat: {}", selfName, inzeratIdentifier.orElse("N/A"));
        }
    }

}
