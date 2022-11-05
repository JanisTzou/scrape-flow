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

import com.github.scrape.flow.execution.StepOrder;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class ClientReservation {

    // can be null if the client is unknown at this point ...
    private ClientId clientId;
    private final StepOrder reservingStep;
    private final ClientReservationType type;

    // if the reservation is still needed
    private boolean finished;

//     might be false if it is waiting to be granted the 'reserved' status
    private boolean placeholder;

    public static ClientReservation newPlaceholderReservation(ClientId clientId, StepOrder reservingStep, ClientReservationType type) {
        return new ClientReservation(clientId, reservingStep, type, false, true);
    }

    public static ClientReservation newPlaceholderReservation(StepOrder reservingStep, ClientReservationType type) {
        return newPlaceholderReservation(null, reservingStep, type);
    }

    public boolean sharesClientWith(ClientReservation other) {
        return other != null && clientId != null && Objects.equals(other.clientId, clientId); // TODO think about this ...
    }


}
