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

package ret.webscrapers.scraping.data.inzeraty.parsers;

import aaanew.drivers.SeleniumDriverManager;
import aaanew.utils.OperationExecutor;
import aaanew.utils.ProcessingException;
import aaanew.utils.SeleniumUtils;
import aaanew.utils.SupplierOperation;
import com.gargoylesoftware.htmlunit.html.DomElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ret.appcore.model.*;
import ret.appcore.model.enums.*;
import ret.appcore.model.location.AdresaScrpd;
import ret.appcore.model.location.Coordinates;
import ret.appcore.model.validation.InzeratValidator;
import ret.appcore.utils.StringUtil;
import ret.webscrapers.scraping.data.inzeraty.parsers.model.EnumMapper;
import ret.webscrapers.scraping.data.inzeraty.parsers.model.ValuesMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BezRealitkyInzeratParser extends InzeratParserBase<WebDriver> {

    private static final Logger log = LogManager.getLogger(BezRealitkyInzeratParser.class);
    private final Pattern NUMBER_PATTERN = Pattern.compile("(^\\d+)");

    public BezRealitkyInzeratParser(SeleniumDriverManager driverManager) {
        super(driverManager);
    }

    @Override
    public Optional<ScrapedInzerat> scrapeData(String inzeratUrl, WebsiteEnum website, TypNabidkyEnum typNabidky, TypNemovitostiEnum typNemovitosti, WebSegment webSegment) {

        String header = "";
        String location = "";

        String celkovaCena = null;
        String poznamkaKCene = "";
        String aktualizace = null;
        String webInzeratId = null;
        String typStavbyScrpd = null;
        String stavObjektuScrpd = null;
        String vlastnictviScrpd = null;
        String stavNabidkyScrpd = null;
        String umisteniObjektu = null;
        String podlazi = null;
        Integer podlaziDeriv = null;

        String uzitnaPlocha = null;
        String plochaPodlahova = null;
        String plochaPozemku = null;

        String voda = null;
        String topeni = null;
        String telekomunikace = null;
        String doprava = null;
        String energetickaNarocnostBudovy = null;
        String vybaveni = null;

        String realitniSpolecnost = null;
        String description = "";  // needs to be empty

        String dispoziceDeriv = null;
        String uliceDeriv = null;
        String obecDeriv = null;
        String castObceDeriv = null;
        Double cenaDeriv = null;

        Double uzitnaPlochaDeriv = null;
        Double plochaPodlahovaDeriv = null;

        Boolean maTerasu = Boolean.FALSE;
        Double terasaPlochaDeriv = null;

        Boolean maGaraz = Boolean.FALSE;
        Double garazPlochaDeriv = null;

        Boolean maSklep = Boolean.FALSE;
        Double sklepPlochaDeriv = null;

        Boolean maBalkon = Boolean.FALSE;
        Double balkonPlochaDeriv = null;

        Boolean maLodgie = Boolean.FALSE;
        Double lodgiePlochaDeriv = null;

        Boolean maVytah = Boolean.FALSE;

        String rokKolaudace = null;
        String rokRekonstrukce = null;
        String anuita = null;
        Double anuitaDeriv = null;
        String provize = null;
        Double provizeDeriv = null;
        String puvodniCena = null;
        Double puvodniCenaDeriv = null;


        Optional<ScrapedInzerat> InzeratOp = Optional.empty();

        try {

            WebDriver driver = driverManager.getDriver();
//            Thread.sleep(2000);
            driver.get(inzeratUrl);

            Optional<WebElement> motherElemOp = getMotherElem();

            if (motherElemOp.isPresent()) {

                WebElement motherElem = motherElemOp.get();
                Optional<WebElement> tableElemOp = getParamsTable(motherElem);

                if (tableElemOp.isPresent()) {
                    WebElement tableElem = tableElemOp.get();

                    webInzeratId              = parseParamValue(tableElem, "Číslo inzerátu:").orElse(null);
                    dispoziceDeriv  = parseParamValue(tableElem, "Dispozice:").orElse(null);
                    dispoziceDeriv  = ValuesMapper.mapValue(dispoziceDeriv);
                    uzitnaPlocha    = parseParamValue(tableElem, "Plocha:").orElse(null);
                    uzitnaPlochaDeriv = parseNumberFromText(uzitnaPlocha);
                    celkovaCena     = parseParamValue(tableElem, "Cena:").orElse(null);
                    cenaDeriv       = parseNumberFromText(celkovaCena.replace(".", ""));
                    obecDeriv      = parseParamValue(tableElem, "Město:").orElse(null);
                    castObceDeriv   = parseParamValue(tableElem, "Městská část:").orElse(null);
                    vlastnictviScrpd     = parseParamValue(tableElem, "Typ vlastnictví:").orElse(null);
                    typStavbyScrpd          = parseParamValue(tableElem, "Typ budovy:").orElse(null);
                    vybaveni        = parseParamValue(tableElem, "Vybavenost:").orElse(null);
                    podlazi         = parseParamValue(tableElem, "Podlaží:").orElse(null);
                    try {
                        podlaziDeriv = Integer.valueOf(podlazi);
                    } catch (NumberFormatException e) {
                        log.error("Error parsing podlaziDeriv value.");
                    }
                    String balkon   = parseParamValue(tableElem, "Balkón:").orElse("");
                    maBalkon        = balkon.equals("Ano");
                    String terasa   = parseParamValue(tableElem, "Terasa:").orElse("");
                    maTerasu        = terasa.equals("Ano");
                    description     = parseDescription(motherElem).orElse("").replace("\n", " "); // has to be empty string
                    stavNabidkyScrpd     = parseJeRezervovano(motherElem) ? "Rezervováno" : null;
                }
            }

            description = StringUtil.left(description, 4000);

            DispoziceEnum dispozice = EnumMapper.mapToDispoziceEnum(dispoziceDeriv);
            StavNabidkyEnum stavNabidky = EnumMapper.mapStavNabidkyEnum(stavNabidkyScrpd);
            StavObjektuEnum stavObjektu = EnumMapper.mapToStavObjektuEnum(stavObjektuScrpd);
            VlastnictviEnum vlastnictvi = EnumMapper.mapToVlastnictviEnum(vlastnictviScrpd);
            TypStavbyEnum typStavby = EnumMapper.mapTypStavbyEnum(typStavbyScrpd);

            InzeratInfoScrpd inzeratInfoScrpd = new InzeratInfoScrpd(header, stavNabidkyScrpd, realitniSpolecnost);
            InzeratInfo inzeratInfo = new InzeratInfo(typNabidky, website, webInzeratId, stavNabidky, realitniSpolecnost, inzeratUrl); // TODO
            AdresaScrpd adresaScrpd = new AdresaScrpd(null, uliceDeriv, null, castObceDeriv, obecDeriv, null, null);
            Coordinates coordinates = new Coordinates(); // TODO
            CenaScrpd cenaScrpd = new CenaScrpd(celkovaCena, null, null, null);
            CenaInfo cenaInfo = new CenaInfo(cenaDeriv, false, false, null, null);
            NemovitostInfoScrpd nemovitostInfoScrpd = new NemovitostInfoScrpd(dispoziceDeriv, typStavbyScrpd, stavObjektuScrpd, vlastnictviScrpd, podlazi, uzitnaPlocha, plochaPodlahova, plochaPozemku);

            NemovitostInfo nemovitostInfo = new NemovitostInfo(typNemovitosti, dispozice, typStavby, stavObjektu, vlastnictvi, null, null, null, null, null); // TODO
            NemovitostInfoExtended nemovitostInfoExtended = new NemovitostInfoExtended(); // TODO
            String inzeratIdentifier = Inzerat.makeIdentifier(inzeratUrl);

            List<String> emptyMandatoryFields = InzeratValidator.getEmptyMandatoryFields(inzeratInfo, coordinates, nemovitostInfo);
            if (!emptyMandatoryFields.isEmpty()) {
                log.warn("ScrapedInzerat {} has following mandatry fields empty: {}", inzeratIdentifier, emptyMandatoryFields);
            }

            LocalDateTime now = LocalDateTime.now();

            ScrapedInzerat scrapedInzerat = new ScrapedInzerat(
                    inzeratIdentifier,
                    inzeratInfoScrpd,
                    inzeratInfo,
                    adresaScrpd,
                    coordinates,
                    cenaScrpd,
                    cenaInfo,
                    nemovitostInfoScrpd,
                    nemovitostInfo,
                    nemovitostInfoExtended,
                    webSegment,
                    new ScrapedInzeratStatus(ScrapedInzeratStatusEnum.CAN_BE_PROCESSED),
                    now,
                    now
            );

            return Optional.of(scrapedInzerat);

        } catch (Exception e) {

            log.error("Parsing exception with header = [{}], location : [{}] and celkovaCena = [{}] for url = [{}].", header, location, celkovaCena, inzeratUrl, e);
        }

        return Optional.empty();
    }



    private Double parseMoney(String numberString) {
        if (numberString == null) {
            return null;
        }
        return Double.valueOf(numberString.split("Kč")[0].replace(" ", "").trim());
    }



    private Double extractArea( Optional<String> paramVal) {
        try {
            String plocha = paramVal.orElse("").replace("m2", "");
            return plocha.equals("") ? null : Double.valueOf(plocha);
        } catch (NumberFormatException e) {
            log.error("Error extracting area:", e);
            return null;
        }
    }



    private Optional<WebElement> getMotherElem() {

        /*  BTW: this contains all propertyData
                <div class="mother">
         */

        SupplierOperation<Optional<WebElement>> getMotherElemOperation = () -> {
            List<WebElement> divElems = driverManager.getDriver().findElements(By.tagName("div"));
            for (WebElement divElem : divElems) {
                if (SeleniumUtils.hasAttributeWithValue(divElem, "class", "mother")) {
                    return Optional.ofNullable(divElem);
                }
            }
            return Optional.empty();
        };

        Optional<WebElement> motherElement = Optional.empty();
        try {
            motherElement = OperationExecutor.attemptExecute(getMotherElemOperation, 100, 1000, log, "BR: ",
                    Optional.of("FAILED getting mother element. KEEP trying"), "FAILED getting mother element",  Optional.of("SUCCESS getting mother element"));
        } catch (ProcessingException e) {
            log.error(e);
        }

        return  motherElement;
    }



    private Optional<WebElement> getParamsTable(WebElement motherElem) {

        /*
                <table class="table" style="border-collapse: initial;">
         */

        SupplierOperation<Optional<WebElement>> getTableElemOperation = () -> {
            Optional<WebElement> tableElemOp = SeleniumUtils.getElement(motherElem, "table", "class", "table");
            return tableElemOp;
        };

        Optional<WebElement> motherElement = Optional.empty();
        try {
            motherElement = OperationExecutor.attemptExecute(getTableElemOperation, 100, 1000, log, "BR: ",
                    Optional.of("FAILED getting table element. KEEP trying"), "FAILED getting table element",  Optional.of("SUCCESS getting table element"));
        } catch (ProcessingException e) {
            log.error(e);
        }

        return  motherElement;
    }



    private Optional<String> parseParamValue(WebElement tableElem, String searchParamHeader) {

        /*
                <table class="table" style="border-collapse: initial;">
                                <tbody>
                                      <tr>
                                        <th>Číslo inzerátu:</th>
										<td colspan="2">509102</td>
                                      </tr>

                                      ...
         */

        SupplierOperation<Optional<String>> getTableElemOperation = () -> {
            List<WebElement> trElems = tableElem.findElements(By.tagName("tr"));
            for (WebElement trElem : trElems) {
                List<WebElement> thElems = trElem.findElements(By.tagName("th"));
                for (WebElement thElem : thElems) {
                    if (thElem.getText().trim().contains(searchParamHeader)) {
                        WebElement tdElem = trElem.findElement(By.tagName("td"));
                        String value = tdElem.getText().trim();
                        return Optional.of(value);
                    }
                }
            }
            return Optional.empty();
        };

        Optional<String> value = Optional.empty();
        try {
            value = OperationExecutor.attemptExecute(getTableElemOperation, 100, 1000, log, "BR: ",
                    Optional.of("FAILED getting value for " + searchParamHeader + ". KEEP trying"), "FAILED getting value for " + searchParamHeader,  Optional.of("SUCCESS getting value for  " + searchParamHeader));
        } catch (ProcessingException e) {
            log.error(e);
        }

        return  value;
    }



    private Optional<String> getHeader(DomElement motherElem) {

        /*
            <h1 class="heading__title" data-fancybox-title="">
                <span>Prodej bytu  3+kk bez realitky</span>
                <br>
                <span class="heading__perex font-weight-medium" data-fancybox-description="">
                    Praha - Hlubočepy
                </span>
            </h1>

        */
        // TODO
        return Optional.empty();
    }


    private Boolean parseJeRezervovano(WebElement motherElem) {

        /*
                <span class="badge badge-warning position-absolute m-3" style="z-index: 9999;">
                                Rezervováno
                         </span>
         */
        SupplierOperation<Boolean> getStavNabidkyOperation = () -> {
            Optional<WebElement> descElemOp = SeleniumUtils.getElement(motherElem, "span", "class", "badge badge-warning position-absolute m-3");
            if (descElemOp.isPresent()) {
                String text = descElemOp.get().getText().trim();
                return text.toLowerCase().equals("rezervováno");
            }
            return false;
        };

        Boolean rezervovano = false;
        try {
            rezervovano = OperationExecutor.attemptExecute(getStavNabidkyOperation, 100, 1000, log, "BR: ",
                    Optional.of("FAILED getting description. KEEP trying"), "FAILED getting description.",  Optional.of("SUCCESS getting description."));
        } catch (ProcessingException e) {
            log.error(e);
        }

        return  rezervovano;
    }


    private Optional<String> parseDescription(WebElement motherElem) {

        /*
            <p class="b-desc__info">
                Byt 4+1 s velkou halou, 2xWC, spíž, balkon. ....
            </p>
         */
        SupplierOperation<Optional<String>> getTableElemOperation = () -> {
            Optional<WebElement> descElemOp = SeleniumUtils.getElement(motherElem, "p", "class", "b-desc__info");
            return descElemOp.map(webElement -> webElement.getText().trim());
        };

        Optional<String> descriptionOp = Optional.empty();
        try {
            descriptionOp = OperationExecutor.attemptExecute(getTableElemOperation, 100, 1000, log, "BR: ",
                    Optional.of("FAILED getting description. KEEP trying"), "FAILED getting description.",  Optional.of("SUCCESS getting description."));
        } catch (ProcessingException e) {
            log.error(e);
        }

        return  descriptionOp;
    }


    private Double parseNumberFromText(String text) {

        Matcher m = NUMBER_PATTERN.matcher(text);

        Double parsed = null;
        try {
            m.find();
            parsed = Double.valueOf(m.group());
        } catch (Exception e) {
            log.error("Error while parsing price from text {}", text);
        }

        return parsed;

    }


}
