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

package ret.webscrapers.integrationtests;

import aaanew.drivers.HtmlUnitDriverManager;
import aaanew.drivers.HtmlUnitDriversFactory;
import aaanew.throttling.model.ScrapedDataType;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import okhttp3.OkHttpClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ret.appcore.model.Inzerat;
import ret.appcore.model.ScrapedInzerat;
import ret.appcore.model.WebSegment;
import ret.appcore.model.enums.TypNabidkyEnum;
import ret.appcore.model.enums.TypNemovitostiEnum;
import ret.appcore.model.enums.WebsiteEnum;
import ret.webscrapers.AppConfig;
import ret.webscrapers.data.DataService;
import ret.webscrapers.data.DataServiceImpl;
import ret.webscrapers.data.HttpHandlersManager;
import ret.webscrapers.data.repo.read.UnsentFilesReader;
import ret.webscrapers.data.send.DataSenderActor;
import ret.webscrapers.http.HttpClient;
import ret.webscrapers.messages.InzeratWithImagesDataMsg;
import ret.webscrapers.pipe.MultipleRequestersMapper;
import ret.webscrapers.pipe.SimpleRequestersMapper;
import ret.webscrapers.scraping.data.inzeraty.parsers.InzeratParser;
import ret.webscrapers.scraping.data.inzeraty.parsers.SRealityInzeratParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Ignore
public class TestSendingDataToProcessor {

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build();

    public static HttpClient httpClient;
    private static ActorSystem system;


    @BeforeClass
    public static void setupClass() {
        system = ActorSystem.create("AppSystem");
    }

    @AfterClass
    public static void teardownClass() {
        system.terminate();
    }

    @Test
    public void test_Sending_Single_Inzerat_Data_To_DataProcessor() throws InterruptedException, IOException {

        AppConfig.turnOffHtmlUnitLogger();

        httpClient = new HttpClient(okHttpClient);

        final SimpleRequestersMapper requestersMapper = SimpleRequestersMapper.getInstance();

        // data handling and sending
        final UnsentFilesReader fileListReader = new UnsentFilesReader(AppConfig.jsonsDir, AppConfig.imagesDir);
        final HttpHandlersManager httpHandlersManager = new HttpHandlersManager(httpClient, AppConfig.jsonsDir, AppConfig.imagesDir);
        final DataService dataService = new DataServiceImpl(httpHandlersManager, fileListReader);
        final ActorRef dataSenderActor = system.actorOf(DataSenderActor.props(dataService, new MultipleRequestersMapper()), "DataSenderActor");

        HtmlUnitDriverManager driverManager = new HtmlUnitDriverManager(ScrapedDataType.INZERAT, new HtmlUnitDriversFactory());

        final InzeratParser parser =  new SRealityInzeratParser((HtmlUnitDriverManager) driverManager);

        WebSegment webSegment = createWebSegment();

        String inzeratUrl = "https://www.sreality.cz/detail/prodej/byt/2+kk/praha-cast-obce-vinohrady-ulice-rejskova/2446233180#img=0&fullscreen=false";
        Optional<ScrapedInzerat> scrapedInzeratOpt = parser.scrapeData(inzeratUrl, WebsiteEnum.SREALITY, TypNabidkyEnum.PRODEJ, TypNemovitostiEnum.BYT, webSegment);
//        Optional<ScrapedInzerat> scrapedInzeratOpt = Optional.of(ScrapedInzerat.emptyScrapedInzerat(inzeratUrl, WebsiteEnum.SREALITY, TypNabidkyEnum.PRODEJ, webSegment));

        System.out.println(scrapedInzeratOpt);

        BufferedImage bufferedImage = ImageIO.read(new File("/Users/tzoumas/ProjectRET/Apps/RetApp/Images/1AEF3B7DABD34B1/img_1.jpeg"));
        List<BufferedImage> bufferedImages = Collections.singletonList(bufferedImage);

        ScrapedInzerat scrapedInzerat = scrapedInzeratOpt.get();
        for (int i = 0; i < 1; i++) {
            scrapedInzerat.setInzeratIdentifier("ident" + i);
            dataSenderActor.tell(new InzeratWithImagesDataMsg(scrapedInzerat, webSegment, inzeratUrl, bufferedImages, Inzerat.makeIdentifier(inzeratUrl)), ActorRef.noSender());
        }

        Thread.sleep(10000);

    }


    private WebSegment createWebSegment() {
        WebSegment webSegment = new WebSegment(
                WebsiteEnum.SREALITY,
                TypNabidkyEnum.PRODEJ,
                TypNemovitostiEnum.BYT,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        webSegment.setId(1);

        return webSegment;
    }


    @Test
    public void pathTest() {
        ActorRef actorRef = system.actorOf(Props.create(TestActor.class, () -> new TestActor()));
        ActorRef actorRef2 = system.actorOf(Props.create(TestActor.class, () -> new TestActor()));
        System.out.println(actorRef.path().name());
        System.out.println(actorRef2.path().name());
        System.out.println(actorRef);
    }


    private static class TestActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return null;
        }
    }

}
