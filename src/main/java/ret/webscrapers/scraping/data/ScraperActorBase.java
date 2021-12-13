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

package ret.webscrapers.scraping.data;

import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.webscrapers.messages.PipeMsg;
import ret.webscrapers.messages.QuitWebDriverIfIdleMsg;
import ret.webscrapers.pipe.SimplePipeActorBase;
import ret.webscrapers.pipe.SimpleRequestersMapper;

import java.time.Duration;

public abstract class ScraperActorBase<T extends PipeMsg> extends SimplePipeActorBase<T> implements DriverOperatorContainingActor {

    private static final Logger log = LogManager.getLogger(ScraperActorBase.class);
    protected final Duration quitIdleDriverCheckInterval;
    protected Cancellable quitIdleDriverCancellable;


    public ScraperActorBase(SimpleRequestersMapper requestersMapper, Duration quitIdleDriverCheckInterval) {
        super(requestersMapper);
        this.quitIdleDriverCheckInterval = quitIdleDriverCheckInterval;
    }


    @Override
    public void scheduleQuitIdleDriverMsg() {
        ActorSystem system = getContext().getSystem();
        Runnable task = () -> getSelf().tell(new QuitWebDriverIfIdleMsg(), getSelf());
        quitIdleDriverCancellable = system.scheduler().scheduleOnce(quitIdleDriverCheckInterval, task, system.dispatcher());
    }

    @Override
    public void terminateDriver() {
        getDriverOperator().terminateDriver();
    }

    @Override
    public void onQuitWebDriverIfIdleMsg(QuitWebDriverIfIdleMsg msg) {
        boolean quit = getDriverOperator().quitDriverIfIdle();
        if (quit) log.info("QUIT IDLE DRIVER for {}", getClass().getSimpleName());
        scheduleQuitIdleDriverMsg();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        scheduleQuitIdleDriverMsg();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        if (quitIdleDriverCancellable != null && !quitIdleDriverCancellable.isCancelled()) {
            quitIdleDriverCancellable.cancel();
        }
        terminateDriver();
    }
}
