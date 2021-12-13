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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Meant for PipeNarrowingActorBase implementors ...
 */
public class MultipleRequestersMapper {

    private final ConcurrentMap<String, ActorRef> actorToRequestorActor = new ConcurrentHashMap<>();

    public void put(String token, ActorRef requestorActor) {
        actorToRequestorActor.put(token, requestorActor);
    }


    public Optional<ActorRef> getRequester(String token) {
        ActorRef requestor = actorToRequestorActor.get(token);
        return Optional.ofNullable(requestor);
    }

    public void removeRequester(String token) {
        actorToRequestorActor.remove(token);
    }

    public boolean requesterExists(String token) {
        return actorToRequestorActor.containsKey(token);
    }

}
