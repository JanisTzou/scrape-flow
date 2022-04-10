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

package com.github.scrape.flow.clients;

import com.gargoylesoftware.htmlunit.WebClient;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class HtmlUnitClientManager implements ClientManager<WebClient> {

    private final HtmlUnitClientFactory clientFactory;
    private final Map<Integer, ClientOperator<WebClient>> clientOperators = new ConcurrentHashMap<>();

    @Override
    public Optional<ClientOperator<WebClient>> getUnreservedClient() {
        return clientOperators.values().stream()
                .filter(c -> !c.isReserved())
                .findFirst()
                .or(this::createNewDriverIfNotAtMaxLimit);
    }

    public ClientOperator<WebClient> getClient(int clientNo) {
        return clientOperators.get(clientNo);
    }

    public void reserveClient(int clientNo) {
        clientOperators.get(clientNo).reserve();
    }

    public void unreserveClient(int clientNo) {
        clientOperators.get(clientNo).unReserve();
    }

    private Optional<ClientOperator<WebClient>> createNewDriverIfNotAtMaxLimit() {
        if (clientOperators.size() < clientFactory.maxDrivers()) {
            int nextClientNo = clientOperators.size() + 1;
            HtmlUnitClientOperator operator = new HtmlUnitClientOperator(
                    nextClientNo,
                    clientFactory
            );
            clientOperators.put(nextClientNo, operator);
            return Optional.of(operator);
        }
        return Optional.empty();
    }

}
