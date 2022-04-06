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
import com.github.scrape.flow.scraping.ClientType;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.scrape.flow.clients.ClientReservationType.LOADING;
import static com.github.scrape.flow.clients.ClientReservationType.READING;
import static org.junit.Assert.*;

public class ClientReservationTrackerTest {

    private final StepOrder step_1 = StepOrder.from(1);
    private final StepOrder step_1_1 = StepOrder.from(1, 1);
    private final StepOrder step_1_1_1 = StepOrder.from(1, 1, 1);
    private final StepOrder step_1_2 = StepOrder.from(1, 2);
    private final StepOrder step_1_2_1 = StepOrder.from(1, 2, 1);
    private final StepOrder step_1_2_2 = StepOrder.from(1, 2, 2);
    private final ClientId clientId1 = new ClientId(ClientType.HTMLUNIT, 1);
    private final ClientId clientId2 = new ClientId(ClientType.HTMLUNIT, 2);
    private final ClientId clientId3 = new ClientId(ClientType.HTMLUNIT, 3);

    @Test
    public void placeholderReservationIsAdded() {
        ClientReservationTracker tracker = new ClientReservationTracker();

        tracker.addLoadingReservationPlaceholder(step_1);

        Optional<ClientReservation> res = tracker.getReservation(step_1);
        assertTrue(res.isPresent());
    }

    @Test
    public void placeholderReservationIsActivated() {
        ClientReservationTracker tracker = new ClientReservationTracker();

        tracker.addLoadingReservationPlaceholder(step_1);
        tracker.activateReservation(step_1, clientId1);

        Optional<ClientReservation> res = tracker.getReservation(step_1);
        assertTrue(res.isPresent());
        assertFalse(res.get().isPlaceholder());
    }

    @Test
    public void allReservationSharingTheSameClientAreFound() {
        ClientReservationTracker tracker = new ClientReservationTracker();

        tracker.addReservation(ClientReservation.newPlaceholder(clientId1, step_1, LOADING));
        tracker.addReservation(ClientReservation.newPlaceholder(clientId2, step_1_1, LOADING));
        tracker.addReservation(ClientReservation.newPlaceholder(clientId2, step_1_1_1, READING));
        tracker.addReservation(ClientReservation.newPlaceholder(clientId3, step_1_2, LOADING));

        assertEquals(List.of(step_1), toSteps(tracker.getReservationsSharingSameClientAs(step_1)));
        assertEquals(List.of(step_1_2), toSteps(tracker.getReservationsSharingSameClientAs(step_1_2)));
        assertEquals(List.of(step_1_1, step_1_1_1), toSteps(tracker.getReservationsSharingSameClientAs(step_1_1)));
        assertEquals(List.of(step_1_1, step_1_1_1), toSteps(tracker.getReservationsSharingSameClientAs(step_1_1_1)));

    }

    private List<StepOrder> toSteps(List<ClientReservation> reservationsSharingSameClientAs) {
        return reservationsSharingSameClientAs.stream().map(ClientReservation::getReservingStep).collect(Collectors.toList());
    }

}
