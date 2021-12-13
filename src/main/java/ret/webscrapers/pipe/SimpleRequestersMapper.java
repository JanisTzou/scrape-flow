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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Shared instance by all implementors of SimplePipeActorBase
 */
public class SimpleRequestersMapper {

    private static final Logger log = LogManager.getLogger();
    private static final SimpleRequestersMapper instance = new SimpleRequestersMapper();
    private final ConcurrentMap<ActorRef, ActorRef> actorToRequestorActor = new ConcurrentHashMap<>();
    private final Set<ActorRef> actorsWithAllowedRequestorsReplacement = new HashSet<>();

    private SimpleRequestersMapper() {
    }

    public static SimpleRequestersMapper getInstance() {
        return instance;
    }

    public void put(ActorRef selfActor, ActorRef newRequestorActor) {
        ActorRef currRequester = actorToRequestorActor.get(selfActor);
        if (currRequester == null) {
            log.info("Set {} as currRequester of {}", ActorUtils.getName(newRequestorActor), ActorUtils.getName(selfActor));
            actorToRequestorActor.put(selfActor, newRequestorActor);
        } else {
            boolean replacementAllowed = actorsWithAllowedRequestorsReplacement.contains(selfActor);
            boolean requesterChanged = currRequester != newRequestorActor;
            if (requesterChanged && !replacementAllowed) {
                throw new IllegalStateException("The pipe actors contract is broken: Existing currRequester: " + ActorUtils.getName(currRequester) + " was to be replaced by: " + ActorUtils.getName(newRequestorActor));
            } else if (requesterChanged && replacementAllowed) {
                actorToRequestorActor.put(selfActor, newRequestorActor);
                log.info("Changed currRequester: " + ActorUtils.getName(currRequester) + " to new requester: " + ActorUtils.getName(newRequestorActor));
            }
        }
    }

    public Optional<ActorRef> getRequesterFor(ActorRef selfActor) {
        ActorRef requesterActor = actorToRequestorActor.get(selfActor);
        return Optional.ofNullable(requesterActor);
    }

    public void addActorWithAllowedRequestorsReplacement(ActorRef actor) {
        actorsWithAllowedRequestorsReplacement.add(actor);
        log.info("Added {} to actorsWithAllowedRequestorsReplacement", ActorUtils.getName(actor));
    }

}
