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

package ret.webscrapers.scraping.data.urls;

import aaanew.drivers.*;
import aaanew.drivers.lifecycle.DriverQuitStrategy;
import aaanew.drivers.lifecycle.DriverRestartStrategy;
import aaanew.drivers.lifecycle.QuitAfterIdleInterval;
import aaanew.drivers.lifecycle.RestartDriverAfterInterval;
import aaanew.throttling.model.ScrapedDataType;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ret.appcore.model.WebSegment;
import ret.appcore.model.enums.TypNabidkyEnum;
import ret.appcore.model.enums.TypNemovitostiEnum;
import ret.appcore.model.enums.WebsiteEnum;
import ret.webscrapers.AppConfig;
import ret.webscrapers.pipe.SimpleRequestersMapper;
import ret.webscrapers.scraping.data.urls.parsers.SRealityUrlsParser;
import ret.webscrapers.scraping.data.urls.parsers.UrlsParser;
import ret.webscrapers.scraping.data.urls.parsers.UrlsParserFactory;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class UrlsScraperActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system, Duration.create(10, TimeUnit.SECONDS), true);
        system = null;
    }

    @Ignore
    @Test
    public void test() throws InterruptedException {

        DriverRestartStrategy driverRestartStrategy = new RestartDriverAfterInterval(10000);
        DriverQuitStrategy driverQuitStrategy = new QuitAfterIdleInterval(5);
        SeleniumDriversFactory seleniumDriversFactory = new SeleniumDriversFactory(AppConfig.getChromeDriverDir(), false);
        SeleniumDriverManager seleniumDriverManager = new SeleniumDriverManager(ScrapedDataType.IMAGES, driverRestartStrategy, driverQuitStrategy, seleniumDriversFactory);
        UrlsParser urlsParser = new SRealityUrlsParser(seleniumDriverManager);
        SRealityUrlsScraper urlsScraperSR = new SRealityUrlsScraper(urlsParser);
        TestKit testKit = new TestKit(system);

        ActorRef urlsScraperActor = system.actorOf(UrlsScraperActor.props(urlsScraperSR, testKit.getTestActor(), SimpleRequestersMapper.getInstance(), java.time.Duration.ofSeconds(10000)));

        String segmentUrl = "https://www.sreality.cz/hledani/prodej/byty/praha?cena-od=600000&cena-do=5000000&bez-aukce=1";
        WebSegment webSegment = new WebSegment(WebsiteEnum.SREALITY, TypNabidkyEnum.PRODEJ, TypNemovitostiEnum.BYT, null, null, null, segmentUrl, false, null,null, true, 1);
        urlsScraperActor.tell(new UrlsScraperActor.ScrapeSegmentUrlsMsg(webSegment), ActorRef.noSender());

        Thread.sleep(1000000);
    }

    @Ignore
    @Test
    public void testIdnesRealityUrlsScraping() throws InterruptedException {

        UrlsScraperFactory urlsScraperFactory = new UrlsScraperFactory();
        HtmlUnitDriversFactory htmlUnitDriversFactory = new HtmlUnitDriversFactory();
        DriverManagerFactory driverManagerFactory = new DriverManagerFactory(null, htmlUnitDriversFactory);
        UrlsParserFactory urlsParserFactory = new UrlsParserFactory();
        DriverManager<?> driverManager = driverManagerFactory.newDriverManager(WebsiteEnum.IDNES_REALITY, ScrapedDataType.URLS);
        UrlsParser urlsParser = urlsParserFactory.newUrlsParser(WebsiteEnum.IDNES_REALITY, driverManager);
        UrlsScraperBase urlsScraper = urlsScraperFactory.newUrlsScraper(WebsiteEnum.IDNES_REALITY, urlsParser);
        TestKit testKit = new TestKit(system);

        ActorRef urlsScraperActor = system.actorOf(UrlsScraperActor.props(urlsScraper, testKit.getTestActor(), SimpleRequestersMapper.getInstance(), java.time.Duration.ofSeconds(10000)));

        String segmentUrl = "https://reality.idnes.cz/s/prodej/byty/1+kk/kraj-vysocina/";
        WebSegment webSegment = new WebSegment(WebsiteEnum.IDNES_REALITY, TypNabidkyEnum.PRODEJ, TypNemovitostiEnum.BYT, null, null, null, segmentUrl, false, null,null, true, 1);
        urlsScraperActor.tell(new UrlsScraperActor.ScrapeSegmentUrlsMsg(webSegment), ActorRef.noSender());

        Thread.sleep(1000000);
    }

    @Test
    public void testUrlsCounter() {

        UrlsCounter counter = new UrlsCounter();

        assertEquals(0, counter.getFinishedScrapingUrlsCount());
        assertEquals(0, counter.getSentUrlsCount());

        int finished = counter.incrementFinished(10);
        assertEquals(10, finished);
        assertEquals(10, counter.getFinishedScrapingUrlsCount());

        int sent = counter.incrementSent(20);
        assertEquals(20, sent);
        assertEquals(20, counter.getSentUrlsCount());

    }


}
