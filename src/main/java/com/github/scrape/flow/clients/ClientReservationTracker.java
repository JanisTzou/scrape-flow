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
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tracks all the steps that are still 'active' - they might be executing or waiting to be executed
 */
@Log4j2
@NotThreadSafe
public class ClientReservationTracker {

    private final Trie<String, ClientReservation> reservationsTrie = new PatriciaTrie<>();

    void addReservation(ClientReservation reservation) {
        reservationsTrie.put(getKey(reservation.getReservingStep()), reservation);
    }

    public SortedMap<String, ClientReservation> getReservationsSubTree(StepOrder parent) {
        return new TreeMap<>(reservationsTrie.prefixMap(getKey(parent)));
    }

    public Optional<ClientReservation> getReservation(StepOrder step) {
        return Optional.ofNullable(reservationsTrie.get(getKey(step)));
    }

    public void addReadingReservationPlaceholder(StepOrder step) {
        addPlaceholder(step, ClientReservationType.READING);
    }

    public void addModifyingReservationPlaceholder(StepOrder step) {
        addPlaceholder(step, ClientReservationType.MODIFYING);
    }

    public void addLoadingReservationPlaceholder(StepOrder step) {
        ClientReservation res = ClientReservation.newPlaceholder(step, ClientReservationType.LOADING);
        addReservation(res);
    }

    private void addPlaceholder(StepOrder step, ClientReservationType reservationType) {
        Optional<StepOrder> parent = step.getParent();
        if (parent.isPresent()) {
            ClientId parentClientId = reservationsTrie.get(getKey(parent.get())).getClientId();
            ClientReservation res = ClientReservation.newPlaceholder(parentClientId, step, reservationType);
            addReservation(res);
        } else {
            log.error("Failed to get parent for {}", step);
        }
    }

    public void finishReservation(StepOrder step) {
        ClientReservation res = reservationsTrie.get(getKey(step));
        res.setFinished(true);
    }

    public boolean finishedAllReservationsSharingSameClientAs(ClientReservation reservation) {
        return getReservationsSharingSameClientAs(reservation.getReservingStep()).stream().allMatch(ClientReservation::isFinished);
    }

    public void removeAllReservationsSharingSameClientAs(ClientReservation reservation) {
        getReservationsSharingSameClientAs(reservation.getReservingStep()).forEach(r -> reservationsTrie.remove(getKey(r.getReservingStep())));
    }

    public boolean canActivateReadingReservationOf(StepOrder step) {
        List<ClientReservation> all = getReservationsSharingSameClientAs(step);
        Stream<ClientReservation> preceding = all.stream().filter(r -> r.getReservingStep().isBefore(step));
        boolean anyUnfinishedModifying = preceding.anyMatch(r -> r.getType().isModifying() && !r.isFinished());
        return !anyUnfinishedModifying;
    }

    public boolean canActivateModifyingReservationOf(StepOrder step) {
        List<ClientReservation> all = getReservationsSharingSameClientAs(step);
        Stream<ClientReservation> preceding = all.stream().filter(r -> r.getReservingStep().isBefore(step));
        boolean anyUnfinished = preceding.anyMatch(r -> !r.isFinished());
        return !anyUnfinished;
    }

    public void activateReservation(StepOrder step) {
        ClientReservation res = reservationsTrie.get(getKey(step));
        res.setPlaceholder(false);
    }

    public void activateReservation(StepOrder step, ClientId clientId) {
        ClientReservation res = reservationsTrie.get(getKey(step));
        res.setPlaceholder(false);
        res.setClientId(clientId);
    }


    // only relevant for read or modify that know their client beforehand?
    List<ClientReservation> getReservationsSharingSameClientAs(StepOrder step) {
        ClientReservation res = reservationsTrie.get(getKey(step));
        ClientReservation lastShared = res;
        for (int i = 0; i < step.size(); i++) {
            Optional<StepOrder> parent = step.getParent();
            if (parent.isPresent()) {
                ClientReservation parentRes = reservationsTrie.get(getKey(parent.get()));
                if (parentRes != null && res.sharesClientWith(parentRes)) {
                    lastShared = parentRes;
                } else {
                    break;
                }
                step = parent.get();
            } else {
                break;
            }
        }
        return reservationsTrie.prefixMap(getKey(lastShared.getReservingStep())).values()
                .stream()
                .filter(r -> r.sharesClientWith(res))
                .collect(Collectors.toList());
    }

    private String getKey(StepOrder step) {
        return step.asString();
    }

}
