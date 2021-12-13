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

/**
 * Used for a pipe actor that processes messages from multiple actors
 * and needs to know which ones to ultimitley send the result to after it
 * receives responses from pipe actors coming next after it.
 * @param <T>
 */
public abstract class PipeNarrowingActorBase<T extends PipeMsg> extends PipeActorBase<T> {

    private static Logger log = LogManager.getLogger();

    MultipleRequestersMapper requestersMapper;

    public PipeNarrowingActorBase(MultipleRequestersMapper requestersMapper) {
        this.requestersMapper = requestersMapper;
    }

    protected void setSenderAsRequestor(String token) {
        ActorRef requester = getSender();
        String requesterName = ActorUtils.getName(requester);
        log.info("{}: Setting actor {} as requester for: {}", selfName, requesterName, token);
        if (requestersMapper.requesterExists(token)) {
            log.error("{}: Requester for token {} already exists: {}, will be overwritten by: {}", token, selfName, requestersMapper.getRequester(token), requesterName);
        }
        requestersMapper.put(token, requester);
    }

    protected void sendToPrevPipeActor(Object message, String token) {
        Optional<ActorRef> requesterOp = requestersMapper.getRequester(token);
        String requesterName = ActorUtils.getName(requesterOp.get());
        if (requesterOp.isPresent()) {
            log.info("{}: Sending response back to requester: {}; for: {}", selfName, requesterName, token);
            requestersMapper.removeRequester(token);
            requesterOp.get().tell(message, getSelf());
        } else {
            log.error("{}: Failed to find requesterActor for: {}", selfName, token);
        }
    }

}
