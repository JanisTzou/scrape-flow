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

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.webscrapers.actors.ActorUtils;
import ret.webscrapers.messages.PipeMsg;

import java.time.Duration;
import java.util.Optional;

public abstract class PipeActorBase<T extends PipeMsg> extends AbstractActor {

    private static Logger log = LogManager.getLogger(PipeActorBase.class);
    protected String selfName;

    protected PipeActorBase() {
        selfName = ActorUtils.getName(getSelf());
    }

    protected void sendToNextPipeActor(ActorRef recipient, Object message, Optional<String> inzeratIdentifier) {
        logInfo(recipient, message, inzeratIdentifier);
        recipient.tell(message, getSelf());
    }

    protected void sendToNextPipeActor(ActorRef recipient, Object message, Duration delay, Optional<String> inzeratIdentifier) {
        logInfo(recipient, message, inzeratIdentifier);
        ActorSystem system = getContext().getSystem();
        system.scheduler().scheduleOnce(delay, recipient, message, system.dispatcher(), getSelf());
    }

    private void logInfo(ActorRef recipient, Object message, Optional<String> inzeratIdentifier) {
        String recipientName = ActorUtils.getName(recipient);
        log.debug("{}: sending msg {} to next actor: {}; Inzerat: {}", selfName, geMsgName(message), recipientName, inzeratIdentifier.orElse("N/A"));
    }

    protected abstract void onFinishedByNextPipeActor(T finishedMsg);

    @Override
    public void unhandled(Object message) {
        logUngandled(message);
    }

    protected void logUngandled(Object message) {
        log.error("{}: ERROR: got UNKNOWN MESSAGE {} from actor: {}", selfName, message, ActorUtils.getName(getSender()));
    }

    protected String senderName() {
        return ActorUtils.getName(getSender());
    }

    protected void logReceivedMsg(Logger log, Object msg) {
        String msgName = geMsgName(msg);
        log.debug("{}: Got {} from: {}", selfName, msgName, senderName());
    }

    protected void logReceivedMsgInz(Logger log, Object msg, String inzeratIdentifier) {
        String msgName = geMsgName(msg);
        log.debug("{}: Got {} from: {}; for: {}", selfName, msgName, senderName(), inzeratIdentifier);
    }

    protected void logReceivedMsg(Logger log, Object msg, String logText) {
        String msgName = geMsgName(msg);
        log.debug("{}: Got {} from: {}; {}", selfName, msgName, senderName(), logText);
    }

    private String geMsgName(Object msg) {
        return msg.getClass().getSimpleName();
    }

}
