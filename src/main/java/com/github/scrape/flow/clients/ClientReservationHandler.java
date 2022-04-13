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
import com.github.scrape.flow.execution.StepOrder;
import com.github.scrape.flow.scraping.ClientType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;

import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
public class ClientReservationHandler {

    private final ClientReservationTracker reservationTracker;
    private final SeleniumClientManager seleniumClientManager;
    private final HtmlUnitClientManager htmlUnitClientManager;
    private final OrderedClientAccessHandler orderedClientAccessHandler;

    public synchronized Optional<ClientOperator<WebClient>> getHtmlUnitClient(StepOrder stepOrder) {
        return reservationTracker.getReservation(stepOrder)
                .map(r -> r.getClientId().getClientNo())
                .map(htmlUnitClientManager::getClient);
    }

    public synchronized Optional<ClientOperator<WebDriver>> getSeleniumClient(StepOrder stepOrder) {
        return reservationTracker.getReservation(stepOrder)
                .map(r -> r.getClientId().getClientNo())
                .map(seleniumClientManager::getClient);
    }

    public synchronized void makeReservationPlaceholder(ClientReservationRequest rq) {
        ClientReservationType type = rq.getReservationType();
        switch (type) {
            case READING:
                reservationTracker.addReadingReservationPlaceholder(rq.getStep());
                break;
            case MODIFYING:
                reservationTracker.addModifyingReservationPlaceholder(rq.getStep());
                break;
            case LOADING:
                reservationTracker.addLoadingReservationPlaceholder(rq.getStep());
                break;
            default:
                log.error("Unhandled reservationType {}", type);
        }
    }

    public synchronized boolean canActivateReservation(ClientReservationRequest rq) {
        switch (rq.getReservationType()) {
            case READING:
                return reservationTracker.canActivateReadingReservationOf(rq.getStep());
            case MODIFYING:
                return reservationTracker.canActivateModifyingReservationOf(rq.getStep());
            case LOADING:
                boolean anyUnreservedClient;
                switch (rq.getClientType()) {
                    case SELENIUM:
                        anyUnreservedClient = seleniumClientManager.getUnreservedClient().isPresent();
                        return anyUnreservedClient && orderedClientAccessHandler.enoughFreeClientsForPrecedingSteps(seleniumClientManager.maxUnreservedClients(), rq.getStep(), rq.getClientType());
                    case HTMLUNIT:
                        anyUnreservedClient = htmlUnitClientManager.getUnreservedClient().isPresent();
                        return anyUnreservedClient && orderedClientAccessHandler.enoughFreeClientsForPrecedingSteps(htmlUnitClientManager.maxUnreservedClients(), rq.getStep(), rq.getClientType());
                }
                return true;
            default:
                log.error("Unhandled reservationType {}", rq.getReservationType());
                return true;
        }
    }

    public synchronized void activateReservation(ClientReservationRequest rq) {
        switch (rq.getReservationType()) {
            case READING:
            case MODIFYING:
                reservationTracker.activateReservation(rq.getStep());
                break;
            case LOADING:
                switch (rq.getClientType()) {
                    case SELENIUM:
                        Optional<ClientOperator<WebDriver>> selOperator = seleniumClientManager.getUnreservedClient();
                        if (selOperator.isPresent()) {
                            ClientId clientId = selOperator.get().getClientId();
                            seleniumClientManager.reserveClient(clientId.getClientNo());
                            reservationTracker.activateReservation(rq.getStep(), clientId);
                        } else {
                            log.error("Failed to activate reservation for rq {}", rq);
                        }
                        break;
                    case HTMLUNIT:
                        Optional<ClientOperator<WebClient>> htmlOperator = htmlUnitClientManager.getUnreservedClient();
                        if (htmlOperator.isPresent()) {
                            ClientId clientId = htmlOperator.get().getClientId();
                            htmlUnitClientManager.reserveClient(clientId.getClientNo());
                            reservationTracker.activateReservation(rq.getStep(), clientId);
                        } else {
                            log.error("Failed to activate reservation for rq {}", rq);
                        }
                }
                break;
            default:
                log.error("Unhandled reservationType {}", rq.getReservationType());
        }
    }

    public synchronized void finishReservation(StepOrder step) {
        reservationTracker.finishReservation(step);
        Optional<ClientReservation> resOpt = reservationTracker.getReservation(step);
        if (resOpt.isPresent()) {
            ClientReservation res = resOpt.get();
            if (reservationTracker.finishedAllReservationsSharingSameClientAs(res)) {
                reservationTracker.removeAllReservationsSharingSameClientAs(res);
                ClientType clientType = res.getClientId().getClientType();
                int clientNo = res.getClientId().getClientNo();
                if (clientType.isSelenium()) {
                    seleniumClientManager.unreserveClient(clientNo);
                } else if (clientType.isHtmlUnit()) {
                    htmlUnitClientManager.unreserveClient(clientNo);
                }
            } else {
                // cannot free client yet
            }
        } else {
            log.error("Failed to find reservation for step {}", step);
        }
    }

}
