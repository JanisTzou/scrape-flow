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

import com.github.scrape.flow.clients.lifecycle.QuitAfterIdleInterval;
import com.github.scrape.flow.clients.lifecycle.RestartDriverAfterInterval;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class SeleniumClientManager implements ClientManager<WebDriver> {

    private final SeleniumClientFactory clientFactory;
    private final Map<Integer, ClientOperator<WebDriver>> clientOperators = new ConcurrentHashMap<>();

    @Override
    public Optional<ClientOperator<WebDriver>> getUnreservedClient() {
        return clientOperators.values().stream()
                .filter(c -> !c.isReserved())
                .findFirst()
                .or(this::createNewDriverIfNotAtMaxLimit);
    }

    public ClientOperator<WebDriver> getClient(int clientNo) {
        return clientOperators.get(clientNo);
    }

    public void reserveClient(int clientNo) {
        clientOperators.get(clientNo).reserve();
    }

    public void unreserveClient(int clientNo) {
        clientOperators.get(clientNo).unReserve();
    }

    private Optional<ClientOperator<WebDriver>> createNewDriverIfNotAtMaxLimit() {
        if (clientOperators.size() < clientFactory.maxDrivers()) {
            int nextClientNo = clientOperators.size() + 1;
            SeleniumClientOperator operator = new SeleniumClientOperator(
                    nextClientNo,
                    new RestartDriverAfterInterval(Long.MAX_VALUE),
                    new QuitAfterIdleInterval(Long.MAX_VALUE),
                    clientFactory
            );
            clientOperators.put(nextClientNo, operator);
            return Optional.of(operator);
        }
        return Optional.empty();
    }


}
