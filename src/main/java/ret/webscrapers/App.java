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

package ret.webscrapers;

import aaanew.drivers.DriverManager;
import aaanew.drivers.DriverManagerFactory;
import aaanew.drivers.HtmlUnitDriversFactory;
import aaanew.drivers.SeleniumDriversFactory;
import aaanew.throttling.ResponsivenessDataCollectingActor;
import aaanew.throttling.ResponsivenessStatisticsAggregator;
import aaanew.throttling.ThrottleManagerActor;
import aaanew.throttling.ThrottlingCalculator;
import aaanew.throttling.model.ScrapedDataType;
import aaanew.throttling.model.ScraperSettingsAll;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.enums.WebsiteEnum;
import ret.webscrapers.actors.ActorUtils;
import ret.webscrapers.cycles.*;
import ret.webscrapers.data.DataService;
import ret.webscrapers.data.DataServiceImpl;
import ret.webscrapers.data.HttpHandlersManager;
import ret.webscrapers.data.repo.read.UnsentFilesReader;
import ret.webscrapers.data.send.DataSenderActor;
import ret.webscrapers.data.send.DataSenderConnectionManager;
import ret.webscrapers.http.HeartbeatActor;
import ret.webscrapers.http.HttpClient;
import ret.webscrapers.http.RetApiConfig;
import ret.webscrapers.init.PipeInitialiserActor;
import ret.webscrapers.messages.InzeratDataMsg;
import ret.webscrapers.pipe.MultipleRequestersMapper;
import ret.webscrapers.pipe.SimpleRequestersMapper;
import ret.webscrapers.pipe.services.Services;
import ret.webscrapers.pipe.services.ServicesUpdaterActor;
import ret.webscrapers.scraping.data.ActorPoolManager;
import ret.webscrapers.scraping.data.images.ImageScraper;
import ret.webscrapers.scraping.data.images.ImageScraperActor;
import ret.webscrapers.scraping.data.images.ImageScraperFactory;
import ret.webscrapers.scraping.data.images.ImageScrapersManagerActor;
import ret.webscrapers.scraping.data.images.parsers.ImageParser;
import ret.webscrapers.scraping.data.images.parsers.ImageParserFactory;
import ret.webscrapers.scraping.data.images.query.ImageQueryActor;
import ret.webscrapers.scraping.data.images.query.ImageQueryService;
import ret.webscrapers.scraping.data.images.query.ImageQueryServiceImpl;
import ret.webscrapers.scraping.data.inzeraty.InzeratQueryManagerActor;
import ret.webscrapers.scraping.data.inzeraty.InzeratScraper;
import ret.webscrapers.scraping.data.inzeraty.InzeratScraperActor;
import ret.webscrapers.scraping.data.inzeraty.InzeratScraperFactory;
import ret.webscrapers.scraping.data.inzeraty.parsers.InzeratParser;
import ret.webscrapers.scraping.data.inzeraty.parsers.InzeratParserFactory;
import ret.webscrapers.scraping.data.inzeraty.query.InzeratQueryActor;
import ret.webscrapers.scraping.data.inzeraty.query.InzeratQueryService;
import ret.webscrapers.scraping.data.inzeraty.query.InzeratQueryServiceImpl;
import ret.webscrapers.scraping.data.urls.UrlsScraperActor;
import ret.webscrapers.scraping.data.urls.UrlsScraperBase;
import ret.webscrapers.scraping.data.urls.UrlsScraperFactory;
import ret.webscrapers.scraping.data.urls.parsers.UrlsParser;
import ret.webscrapers.scraping.data.urls.parsers.UrlsParserFactory;
import ret.webscrapers.websegments.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class App {

    static {
        System.setProperty("level", "trace");
        System.setProperty("app-name", "SCRAPER");
        System.setProperty("basePath", "/Users/janis/Projects_Data/ProjectRET/Apps/WebScrapers/logs");
    }

    private static Logger log = LogManager.getLogger(App.class);

    private static ActorSystem system = ActorSystem.create("AppSystem");

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .build();

    public static HttpClient httpClient;


    public static void main(String args[]) {

        try {

            AppConfig.printConfig();
            RetApiConfig.printConfig();
            AppConfig.turnOffHtmlUnitLogger();

            httpClient = new HttpClient(okHttpClient);

            final AppActorSystem appActorSystem = new AppActorSystem(system);

            final SimpleRequestersMapper requestersMapper = SimpleRequestersMapper.getInstance();

            // data handling and sending
            final UnsentFilesReader fileListReader = new UnsentFilesReader(AppConfig.jsonsDir, AppConfig.imagesDir);
            final HttpHandlersManager httpHandlersManager = new HttpHandlersManager(httpClient, AppConfig.jsonsDir, AppConfig.imagesDir);
            final DataService dataService = new DataServiceImpl(httpHandlersManager, fileListReader);
            final ActorRef dataSenderActor = system.actorOf(DataSenderActor.props(dataService, new MultipleRequestersMapper()), "DataSenderActor");

            // connection
            final DataSenderConnectionManager dataSenderConnectionManager = new DataSenderConnectionManager(dataSenderActor);
            httpClient.register(dataSenderConnectionManager);
            httpClient.sendInitialisationHeartbeat();

            // Responsiveness management
            final ResponsivenessStatisticsAggregator statistics = new ResponsivenessStatisticsAggregator(AppConfig.minTotalScrapedItemsCountToConsider, AppConfig.maxStatsAge.toMillis());
            final ActorRef responsivenessStatsCollectorActor = system.actorOf(ResponsivenessDataCollectingActor.props(statistics), "ResponsivenessDataCollectingActor");

            // scraping general
            final ScraperSettingsAll scrapersSettingsAll = new ScraperSettingsAll(AppConfig.getScrapersSettings());
            final SeleniumDriversFactory seleniumDriversFactory = new SeleniumDriversFactory(AppConfig.getChromeDriverDir(), AppConfig.headlessDriver);
            final HtmlUnitDriversFactory htmlUnitDriversFactory = new HtmlUnitDriversFactory();
            final DriverManagerFactory driverManagerFactory = new DriverManagerFactory(seleniumDriversFactory, htmlUnitDriversFactory);

            // ImageScrapers
            final ImageParserFactory imageParserFactory = new ImageParserFactory();
            final ImageScraperFactory imageScraperFactory = new ImageScraperFactory();
            final Map<WebsiteEnum, Queue<ActorRef>> imageScrapersMap = new HashMap<>();
            final Queue<ActorRef> imagesScrapersSR = initImageScraperActors(WebsiteEnum.SREALITY, ScrapedDataType.IMAGES, requestersMapper, dataSenderActor, responsivenessStatsCollectorActor, scrapersSettingsAll, imageScraperFactory, driverManagerFactory, imageParserFactory);
            final Queue<ActorRef> imagesScrapersBR = initImageScraperActors(WebsiteEnum.BEZ_REALITKY, ScrapedDataType.IMAGES, requestersMapper, dataSenderActor, responsivenessStatsCollectorActor, scrapersSettingsAll, imageScraperFactory, driverManagerFactory, imageParserFactory);
            final Queue<ActorRef> imagesScrapersIR = initImageScraperActors(WebsiteEnum.IDNES_REALITY, ScrapedDataType.IMAGES, requestersMapper, dataSenderActor, responsivenessStatsCollectorActor, scrapersSettingsAll, imageScraperFactory, driverManagerFactory, imageParserFactory);
            imageScrapersMap.put(WebsiteEnum.SREALITY, imagesScrapersSR);
            imageScrapersMap.put(WebsiteEnum.BEZ_REALITKY, imagesScrapersBR);
            imageScrapersMap.put(WebsiteEnum.IDNES_REALITY, imagesScrapersIR);

            final Map<WebsiteEnum, Integer> websiteToActorsToKeepAliveMapImages = scrapersSettingsAll.getWebsiteToActorsToKeepAliveMap(ScrapedDataType.IMAGES);
            final ActorPoolManager<InzeratDataMsg> imageScrapersManager = new ActorPoolManager<>(ScrapedDataType.IMAGES, imageScrapersMap, websiteToActorsToKeepAliveMapImages);

            final ActorRef imagesScrapersManagerActor = system.actorOf(ImageScrapersManagerActor.props(imageScrapersManager, new MultipleRequestersMapper()), "ImageScrapersManagerActor");

            final ImageQueryService imageQueryService = new ImageQueryServiceImpl(httpClient, RetApiConfig.noImagesInzeratIdentifierListURL);
            final InzeratQueryService inzeratQueryService = new InzeratQueryServiceImpl(httpClient, RetApiConfig.notScrapedInzeratyListURL);

            // InzeratScrapers
            final InzeratParserFactory inzeratParserFactory = new InzeratParserFactory();
            final InzeratScraperFactory inzeratScraperFactory = new InzeratScraperFactory();
            final Map<WebsiteEnum, Queue<ActorRef>> inzeratScrapersMap = new HashMap<>();
            final Queue<ActorRef> inzeratQueryActorsSR = initInzeratQueryActors(WebsiteEnum.SREALITY, ScrapedDataType.INZERAT, requestersMapper, dataSenderActor, responsivenessStatsCollectorActor, scrapersSettingsAll, imagesScrapersManagerActor, imageQueryService, inzeratScraperFactory, inzeratQueryService, inzeratParserFactory, driverManagerFactory);
            final Queue<ActorRef> inzeratQueryActorsBR = initInzeratQueryActors(WebsiteEnum.BEZ_REALITKY, ScrapedDataType.INZERAT, requestersMapper, dataSenderActor, responsivenessStatsCollectorActor, scrapersSettingsAll, imagesScrapersManagerActor, imageQueryService, inzeratScraperFactory, inzeratQueryService, inzeratParserFactory, driverManagerFactory);
            final Queue<ActorRef> inzeratQueryActorsIR = initInzeratQueryActors(WebsiteEnum.IDNES_REALITY, ScrapedDataType.INZERAT, requestersMapper, dataSenderActor, responsivenessStatsCollectorActor, scrapersSettingsAll, imagesScrapersManagerActor, imageQueryService, inzeratScraperFactory, inzeratQueryService, inzeratParserFactory, driverManagerFactory);
            inzeratScrapersMap.put(WebsiteEnum.SREALITY, inzeratQueryActorsSR);
            inzeratScrapersMap.put(WebsiteEnum.BEZ_REALITKY, inzeratQueryActorsBR);
            inzeratScrapersMap.put(WebsiteEnum.IDNES_REALITY, inzeratQueryActorsIR);

            final Map<WebsiteEnum, Integer> websiteToActorsToKeepAliveMapInzerat = scrapersSettingsAll.getWebsiteToActorsToKeepAliveMap(ScrapedDataType.INZERAT);
            final ActorPoolManager<String> inzeratQueryActorsManager = new ActorPoolManager<>(ScrapedDataType.INZERAT, inzeratScrapersMap, websiteToActorsToKeepAliveMapInzerat);
            final ActorRef inzeratQueryManagerActor = system.actorOf(InzeratQueryManagerActor.props(inzeratQueryActorsManager, requestersMapper), "InzeratQueryManagerActor");

            // Throttling
            final ThrottlingCalculator throttler = new ThrottlingCalculator(statistics, scrapersSettingsAll);
            // Starts throttling implicitly upon instantiation (make explicit?)
            final ActorRef throttleManagerActor = system.actorOf(ThrottleManagerActor.props(throttler, inzeratQueryManagerActor, imagesScrapersManagerActor, AppConfig.responsivenessCheckInterval), "ThrottleManagerActor");

            // UrlsScrapers
            final UrlsScraperFactory urlsScraperFactory = new UrlsScraperFactory();
            final UrlsParserFactory urlsParserFactory = new UrlsParserFactory();
            final ActorRef urlScrapingActorSR = initUrlScrapingActor(WebsiteEnum.SREALITY, ScrapedDataType.URLS, requestersMapper, inzeratQueryManagerActor, urlsScraperFactory, urlsParserFactory, driverManagerFactory);
            final ActorRef urlScrapingActorBR = initUrlScrapingActor(WebsiteEnum.BEZ_REALITKY, ScrapedDataType.URLS, requestersMapper, inzeratQueryManagerActor, urlsScraperFactory, urlsParserFactory, driverManagerFactory);
            final ActorRef urlScrapingActorIR = initUrlScrapingActor(WebsiteEnum.IDNES_REALITY, ScrapedDataType.URLS, requestersMapper, inzeratQueryManagerActor, urlsScraperFactory, urlsParserFactory, driverManagerFactory);
            final Map<WebsiteEnum, ActorRef> urlScrapersMap = new HashMap<>();
            urlScrapersMap.put(WebsiteEnum.SREALITY, urlScrapingActorSR);
            urlScrapersMap.put(WebsiteEnum.BEZ_REALITKY, urlScrapingActorBR);
            urlScrapersMap.put(WebsiteEnum.IDNES_REALITY, urlScrapingActorIR);

            // WebSegments
            final WebSegmentsData webSegmentsServiceHelper = new WebSegmentsData();
            final WebsegmentsProgressFileHandler fileHandler = new WebsegmentsProgressFileHandler(AppConfig.websegmentsDir, AppConfig.websegmentBeingProcessedFileName);
            final WebSegmentsService webSegmentsService = new WebSegmentsServiceImpl(httpClient, webSegmentsServiceHelper, fileHandler, AppConfig.lastProcessedSegmentsAgeToConsider);
            final ActorRef webSegmentsActor = system.actorOf(WebSegmentsActor.props(urlScrapersMap, dataSenderActor, requestersMapper), "WebSegmentsActor");
            final ActorRef webSegmentsManagerActor = system.actorOf(WebSegmentsManagerActor.props(requestersMapper, webSegmentsService, webSegmentsActor), "WebSegmentsManagerActor");

            // Cycles
            final CyclesService cyclesService = new CyclesServiceImpl(httpClient);
            final CycleStartFinishRecorder cycleStartFinishRecorder = new CycleStartFinishRecorder();
            final ActorRef cycleActor = system.actorOf(CyclesActor.props(webSegmentsManagerActor, dataSenderActor, requestersMapper, cycleStartFinishRecorder), "CyclesActor");
            final ActorRef cyclesManagerActor = system.actorOf(CyclesManagerActor.props(requestersMapper, cyclesService, cycleActor), "CyclesManagerActor");

            // Services
            final Services services = new Services(cyclesService, webSegmentsService, inzeratQueryService, imageQueryService);
            final ActorRef servicesUpdaterActor = system.actorOf(ServicesUpdaterActor.props(services, RetApiConfig.servicesUpdateRetryPeriod, RetApiConfig.servicesUpdatePeriod), "ServicesUpdaterActor");

            final ActorRef initialiserActor = system.actorOf(PipeInitialiserActor.props(cyclesManagerActor, servicesUpdaterActor, requestersMapper), "PipeInitialiserActor");

            // this starts heartbeats - needs to be last ...
            final ActorRef heartbeatActor = system.actorOf(HeartbeatActor.props(httpClient, RetApiConfig.heartbeatPeriod), "HeartbeatActor");

            initialiserActor.tell(new PipeInitialiserActor.InitialisePipeMsg(), ActorRef.noSender());


            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    Future<Terminated> terminate = system.terminate();
                    try {
                        log.info(">>>> Terminating AKKA <<<<");
                        Await.result(terminate, Duration.apply(30, TimeUnit.SECONDS));
                        log.info(">>>> AKKA terminated <<<<");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // temporary solution ...
            Thread.sleep(Duration.create(48, TimeUnit.HOURS).toMillis());

        } catch (Exception ex) {

            log.error("App shutting down due to error: ", ex);

        } finally {
            system.terminate();
        }

        log.info("APP SHUT DOWN");
    }


    private static ActorRef initUrlScrapingActor(WebsiteEnum website,
                                                 ScrapedDataType scrapedDataType,
                                                 SimpleRequestersMapper requestersMapper,
                                                 ActorRef inzeratScraperManagerActor,
                                                 UrlsScraperFactory urlsScraperFactory,
                                                 UrlsParserFactory urlsParserFactory,
                                                 DriverManagerFactory driverManagerFactory) {

        DriverManager<?> driverManager = driverManagerFactory.newDriverManager(website, scrapedDataType);
        UrlsParser urlsParser = urlsParserFactory.newUrlsParser(website, driverManager);
        UrlsScraperBase urlsScraper = urlsScraperFactory.newUrlsScraper(website, urlsParser);
        return system.actorOf(UrlsScraperActor.props(urlsScraper, inzeratScraperManagerActor, requestersMapper, AppConfig.checkForIdleDriverInterval), "UrlsScraperActor" + website.getShortName());
    }


    private static Queue<ActorRef>  initInzeratQueryActors(WebsiteEnum website,
                                                           ScrapedDataType scrapedDataType,
                                                           SimpleRequestersMapper requestersMapper,
                                                           ActorRef dataSenderActor,
                                                           ActorRef responsivenessStatsCollectorActor,
                                                           ScraperSettingsAll scrapersSettingsAll,
                                                           ActorRef imagesScrapersManagerActor,
                                                           ImageQueryService imageQueryService,
                                                           InzeratScraperFactory inzeratScraperFactory,
                                                           InzeratQueryService inzeratQueryService,
                                                           InzeratParserFactory inzeratParserFactory,
                                                           DriverManagerFactory driverManagerFactory) {

        Queue<ActorRef> inzeratQueryActors = new LinkedList<>();

        int maxSrInzeratScrapersCount = scrapersSettingsAll.getScrapersSettings(website, scrapedDataType).getMaxScrapers();

        for (int i = 0; i < maxSrInzeratScrapersCount; i++) {
            DriverManager<?> driverManager = driverManagerFactory.newDriverManager(website, scrapedDataType);
            InzeratParser inzeratParser = inzeratParserFactory.newInzeratParser(website, driverManager);
            InzeratScraper inzeratScraper = inzeratScraperFactory.newInzeratScraper(website, inzeratParser);

            ActorRef scrapeImagesQueryActor = system.actorOf(ImageQueryActor.props(imageQueryService, imagesScrapersManagerActor, dataSenderActor, requestersMapper, AppConfig.imageScrapingEnabled), "ImageQueryActor"  + website.getShortName() + "_No-" + i);
            ActorRef scraperActor = system.actorOf(InzeratScraperActor.props(scrapeImagesQueryActor, inzeratScraper, dataSenderActor, requestersMapper, responsivenessStatsCollectorActor, AppConfig.checkForIdleDriverInterval), "InzeratScraperActor"  + website.getShortName() + "_No-" + i);
            ActorRef scrapeInzeratQueryActor = system.actorOf(InzeratQueryActor.props(inzeratQueryService, scraperActor, requestersMapper), "InzeratQueryActor"  + website.getShortName() + "_No-" + i);
            log.info("Init scraper actor: {} of type: {}", ActorUtils.getName(scraperActor), scrapedDataType);
            inzeratQueryActors.add(scrapeInzeratQueryActor);
        }
        return inzeratQueryActors;
    }


    private static Queue<ActorRef> initImageScraperActors(WebsiteEnum website,
                                                          ScrapedDataType scrapedDataType,
                                                          SimpleRequestersMapper requestersMapper,
                                                          ActorRef dataSenderActor,
                                                          ActorRef responsivenessStatsCollectorActor,
                                                          ScraperSettingsAll scrapersSettingsAll,
                                                          ImageScraperFactory imageScraperFactory,
                                                          DriverManagerFactory driverManagerFactory,
                                                          ImageParserFactory imageParserFactory) {

        Queue<ActorRef> imagesScrapers = new LinkedList<>();
        int maxSrImageScrapersCount = scrapersSettingsAll.getScrapersSettings(website, scrapedDataType).getMaxScrapers();

        for (int i = 0; i < maxSrImageScrapersCount; i++) {
            DriverManager<?> driverManager = driverManagerFactory.newDriverManager(website, scrapedDataType);
            ImageParser imageParser = imageParserFactory.newImageParser(website, driverManager);
            ImageScraper imageScraperSR = imageScraperFactory.newImageScraper(website, imageParser);
            ActorRef imagesScraperActor = system.actorOf(ImageScraperActor.props(dataSenderActor, imageScraperSR, requestersMapper, AppConfig.checkForIdleDriverInterval, responsivenessStatsCollectorActor), "ImageScraperActor" + website.getShortName() + "_No-" + i);
            imagesScrapers.add(imagesScraperActor);
        }
        return imagesScrapers;
    }

}
