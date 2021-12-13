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


import aaanew.drivers.HtmlUnitDriverManager;
import aaanew.utils.HtmlUnitUtils;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.ScrapedInzerat;
import ret.appcore.model.WebSegment;
import ret.appcore.model.enums.TypNabidkyEnum;
import ret.appcore.model.enums.TypNemovitostiEnum;
import ret.appcore.model.enums.WebsiteEnum;
import ret.appcore.model.location.AdresaScrpd;
import ret.appcore.utils.StringUtil;
import ret.webscrapers.scraping.data.inzeraty.parsers.model.ScrapedData;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SRealityInzeratParser extends HtmlUnitInzeratParserBase {

    private static final Logger log = LogManager.getLogger();
    private final Pattern LONGITUDE_PATTERN = Pattern.compile("x=\\d+.\\d+&");
    private final Pattern LATITUDE_PATTERN = Pattern.compile("y=\\d+.\\d+&");


    public SRealityInzeratParser(HtmlUnitDriverManager driverManager) {
        super(driverManager);
    }


    // TODO review it for both DUM and GARAZ the meaning of the website data is the same
    @Override
    public Optional<ScrapedInzerat> scrapeData(String inzeratUrl, WebsiteEnum website, TypNabidkyEnum typNabidky, TypNemovitostiEnum typNemovitosti, WebSegment webSegment) {

        ScrapedData parsedData = new ScrapedData();

        try {

            WebClient webClient = driverManager.getDriver();
            final HtmlPage page = getHtmlPage(inzeratUrl, webClient);
            // TODO Corner case here - if the scraped inzerat is cancelled  before we get to scrape it, we might still get a responce from the next best url match
            //  -> we must check the pages url to see if it is the same as the one we intended to scrape!
            //  (BTW we dont get any redirects here),
//            System.out.println(page.getBaseURL().getPath());
//            System.out.println(page.getWebResponse().getStatusCode());
            Optional<DomElement> propertyDetailElemOp = getPropertyDetailElem(page);

            if (!propertyDetailElemOp.isPresent()) {
                return Optional.empty();

            } else {

                Optional<DomElement> propertyTitleElemOp = getPropertyTitleElem(propertyDetailElemOp.get());

                if (propertyTitleElemOp.isPresent()) {

                    Optional<String> locationString = getLocationString(propertyDetailElemOp.get());
                    if (locationString.isPresent()) {
                        parsedData.latitude = parseLatitude(locationString.get());
                        parsedData.longitude = parseLongitude(locationString.get());
                    }

                    Optional<String> headerOp = getHeader(propertyTitleElemOp.get());
                    if (headerOp.isPresent()) {
                        parsedData.hlavickaInzeratu = headerOp.get();
                    }

                    Optional<String> locationOp = getLocation(propertyTitleElemOp.get());
                    if (locationOp.isPresent()) {
                        //TODO  the replacement is temporaty - make better ...
                        parsedData.locationRaw = locationOp.get().replace("Panorama", "").trim();
                    }

                    Optional<String> descriptionOp = getDescription(propertyDetailElemOp.get());
                    if (descriptionOp.isPresent()) {
                        parsedData.description = descriptionOp.get();
                    }

                    Optional<String> realtniSpolecnostOp = getContact(propertyDetailElemOp.get());
                    if (realtniSpolecnostOp.isPresent()) {
                        parsedData.prodavajici = sanitizeProdavajici(realtniSpolecnostOp.get());
                    }

                    List<DomElement> allParamElems = getAllParamElems(propertyDetailElemOp.get());

                    for (DomElement paramElem : allParamElems) {

                        Optional<String> paramLabel = HtmlUnitUtils.getChildElementTextContent(paramElem, "label", "class", "param-label ng-binding", true);

                        if (paramLabel.isPresent()) {

                            /*
                             SOMETIMES SOME FIELDS MAY HAVE VALUES OR CAN BE FALSE/TRUE these fields are:
                             - terasa, garaz, balkon, lodgie, vytah, sklep, bezbarierovy
                             .. they needs special treatment checking if they are not FALSE and if not then read their value or just see that they are TRUE.

                             example code is here:

                                    <li class="param ng-scope" ng-repeat="item in group.params"><label class="param-label ng-binding">Garáž:</label><strong class="param-value">
                                        <!-- ngIf: item.type == 'link' -->
                                        <!-- ngIf: item.type != 'link' --><span ng-if="item.type != 'link'" class="ng-binding ng-scope"></span><!-- end ngIf: item.type != 'link' -->
                                        <!-- ngIf: item.type == 'area' -->
                                        <!-- ngIf: item.type == 'boolean-true' --><span ng-if="item.type == 'boolean-true'" class="icof icon-ok ng-scope"></span><!-- end ngIf: item.type == 'boolean-true' -->
                                        <!-- ngIf: item.type == 'boolean-false' -->
                                        <!-- ngIf: item.type == 'topped' -->
                                    </strong></li>
                              */

                            List<DomElement> isFalse = HtmlUnitUtils.getAllChildElements(paramElem, "span", "ng-if", "item.type == 'boolean-false'", true);
                            boolean paramMarkedAsFalse = !isFalse.isEmpty();
                            Optional<String> paramVal = HtmlUnitUtils.getChildElementTextContent(paramElem, "strong", "class", "param-value", true);

                            switch (paramLabel.get()) {

                                case "Celková cena:":
                                case "Cena:":
                                case "Zlevněno:":
                                    parsedData.cenaRaw = paramVal.orElse(null);
                                    break;
                                case "Poznámka k ceně:":
                                    parsedData.poznamkaKCeneRaw = paramVal.orElse("");
                                    break;
                                case "Původní cena:":
                                    parsedData.puvodniCenaRaw = paramVal.orElse(null);
                                    break;
                                case "Provize:":
                                    parsedData.provizeRaw = paramVal.orElse(null);
                                    break;
                                case "Anuita:":
                                    parsedData.anuitaRaw = paramVal.orElse(null);
                                    break;
                                case "ID zakázky:":
                                case "ID:":
                                    parsedData.webNemovitostId = paramVal.orElse(null);
                                    break;
                                case "Stav:":
                                    parsedData.stavNabidkyRaw = paramVal.orElse("");
                                    break;
                                case "Aktualizace:":
                                    /*
                                    Včera
                                    Dnes - prevest na datum
                                     */
                                    parsedData.aktualizaceNaWebu = paramVal.orElse(null);
                                    break;
                                case "Stavba:":
                                    parsedData.typStavbyRaw = paramVal.orElse(null);
                                    break;
                                case "Rok kolaudace:":
                                    parsedData.rokKolaudace = paramVal.orElse(null);
                                    break;
                                case "Rok rekonstrukce:":
                                    parsedData.rokRekonstrukce = paramVal.orElse(null);
                                    break;

                                case "Stav objektu:":
                                    parsedData.stavObjektuRaw = paramVal.orElse(null);
                                    break;
                                case "Vlastnictví:":
                                    parsedData.vlastnictviRaw = paramVal.orElse(null);
                                    break;

                                case "Podlaží:":
                                    parsedData.podlaziRaw = paramVal.orElse("");
                                    parsedData.podlazi = parsePodlazi(parsedData.podlaziRaw,  inzeratUrl);
                                    break;

                                case "Užitná plocha:":
                                    parsedData.uzitnaPlochaRaw = parsePlochaString(paramVal.orElse(""));
                                    parsedData.uzitnaPlocha = parseDouble(parsedData.uzitnaPlochaRaw, "uzitnaPlocha", inzeratUrl);
                                    break;
                                case "Plocha podlahová:":
                                    parsedData.plochaPodlahovaRaw = parsePlochaString(paramVal.orElse(""));
                                    parsedData.plochaPodlahova = parseDouble(parsedData.plochaPodlahovaRaw, "plochaPodlahova", inzeratUrl);
                                    break;
                                case "Plocha pozemku:":
                                    parsedData.plochaPozemkuRaw = parsePlochaString(paramVal.orElse(""));
                                    parsedData.plochaPozemku = parseDouble(parsedData.plochaPozemkuRaw, "plochaPozemku", inzeratUrl);
                                    break;
                                case "Energetická náročnost budovy:":
                                    parsedData.energetickaNarocnostBudovy = paramVal.orElse(null);
                                    break;
                                case "Vybavení:":
                                    boolean maVybaveni = hasItem(paramMarkedAsFalse);
                                    if (!maVybaveni && hasValue(paramVal)) {
                                        parsedData.vybaveni = paramVal.orElse(null);
                                    } else if (maVybaveni) {
                                        parsedData.vybaveni = "Ano";
                                    } else {
                                        parsedData.vybaveni = "Ne";
                                    }
                                    break;
                                case "Terasa:":
                                    parsedData.maTerasu = hasItem(paramMarkedAsFalse);
                                    if (parsedData.maTerasu)
                                        parsedData.terasaPlocha = parsePlochaDouble(paramVal.orElse(""));
                                    break;
                                // IMPORTANT: It happens that inzerat has garaz = 2 and also parkovani = 2 => take into account just one of those
                                case "Garáž:":
                                    parsedData.maGaraz = hasItem(paramMarkedAsFalse);
                                    if (parsedData.maGaraz && hasValue(paramVal)) {
                                        if (paramVal.get().contains("m2")) {
                                            parsedData.garazPlocha = parsePlochaDouble(paramVal.orElse(""));
                                        } else {
                                            parsedData.pocetGarazi = Integer.valueOf(paramVal.get().trim());
                                        }
                                    }
                                    break;

                                case "Sklep:":
                                    parsedData.maSklep = hasItem(paramMarkedAsFalse);
                                    if (parsedData.maSklep)
                                        parsedData.sklepPlocha = parsePlochaDouble(paramVal.orElse(""));
                                    break;

                                case "Balkón:":
                                    parsedData.maBalkon = hasItem(paramMarkedAsFalse);
                                    if (parsedData.maBalkon)
                                        parsedData.balkonPlocha = parsePlochaDouble(paramVal.orElse(""));
                                    break;

                                case "Lodžie:":
                                    parsedData.maLodgie = hasItem(paramMarkedAsFalse);
                                    if (parsedData.maLodgie)
                                        parsedData.lodgiePlocha = parsePlochaDouble(paramVal.orElse(""));
                                    break;

                                case "Výtah:":
                                    parsedData.maVytah = hasItem(paramMarkedAsFalse);
                                    break;

                                // IMPORTANT: It happens that inzerat has garaz = 2 and also parkovani = 2 => take into account just one of those
                                case "Parkování:": // (can be true/false but also numeric e.g. = 1)
                                    parsedData.maParkovani = hasItem(paramMarkedAsFalse);
                                    if (!parsedData.maParkovani && paramVal.isPresent()) {
                                        String pocetParkovaniRaw = paramVal.get().trim();
                                        parsedData.pocetParkovani = parseInteger(pocetParkovaniRaw, "pocetParkovani", inzeratUrl);
                                    } else if (parsedData.maParkovani) {
                                        parsedData.pocetParkovani = 1;
                                    } else {
                                        parsedData.pocetParkovani = 0;
                                    }
                                    break;

                                case "Plocha zahrady:":
                                    parsedData.plochaZahdrady = parsePlochaDouble(paramVal.orElse(""));
                                    break;

                                case "Bazén:":
                                    parsedData.maBazen = hasItem(paramMarkedAsFalse);
                                    break;

                                case "Plocha bazénu:":
                                    parsedData.plochaBazenu = parsePlochaDouble(paramVal.orElse(""));
                                    break;

                                // TODO are any of these important for houses ?
                                case "Umístění objektu:":
                                case "Voda:":
                                case "Topení:":
                                case "Telekomunikace:":
                                case "Doprava:":
                                case "Plyn:":
                                case "Elektřina:":
                                case "Odpad:":
                                case "Datum zahájení prodeje:":
                                case "Ukazatel energetické náročnosti budovy:":
                                case "Průkaz energetické náročnosti budovy:":
                                case "Datum nastěhování:":
                                case "Plocha zastavěná:":
                                case "Bezbariérový:":
                                case "Komunikace:":
                                case "Převod do OV:":
                                case "Náklady na bydlení:":
                                case "Výška stropu:":
                                case "Datum ukončení výstavby:":
                                case "Půdní vestavba:":
                                case "Datum prohlídky:":
                                case "Počet bytů:":
                                case "Poloha domu:":
                                case "Typ domu:": // ignore this contains values like Radovy, atd ....
                                case "Typ bytu:": // ignore this contains values like Radovy, atd ....

                                    break;
                                default:
                                    log.warn("Unknown paramLabel = [{}] for url = [{}]", paramLabel, inzeratUrl);
                            }

                            paramVal = Optional.empty();
                        }

                    } //  for (DomElement paramElem : allParamElems) {
                }

                try {
                    if (typNemovitosti.isDispoziceSupported()) {
                        if (parsedData.hlavickaInzeratu.contains("6 pokojů a více")
                            || parsedData.hlavickaInzeratu.contains("6 a více")) {
                            parsedData.dispozice = "6 pokojů a více";
                        } else {
                            parsedData.dispozice = (parsedData.hlavickaInzeratu.replace("\u00a0", " ").split(" ")[2]).trim();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Could not parse dispoziceDeriv from text '{}', for url = {}", parsedData.hlavickaInzeratu, inzeratUrl);
                }

                parsedData.cena = parseMoney(parsedData.cenaRaw, "cena", inzeratUrl);
                parsedData.anuita = parseMoney(parsedData.anuitaRaw, "anuita", inzeratUrl);
                parsedData.provize = parseMoney(parsedData.provizeRaw, "provize", inzeratUrl);
                parsedData.puvodniCena = parseMoney(parsedData.puvodniCenaRaw, "puvodniCenaDeriv", inzeratUrl);
            }

        } catch (Exception ex) {

            log.error("Parsing exception with hlavickaInzeratu = {}, polohaScrpd : {} and cenaScrpd = {} for url = {}.", parsedData.hlavickaInzeratu, parsedData.locationRaw, parsedData.cenaRaw, inzeratUrl, ex);

            return Optional.empty();

        } finally {

            driverManager.terminateDriver();
        }

        parsedData.description = StringUtil.left(parsedData.description, 4000);

        AdresaScrpd adresaScrpd = SRealityLocationResolver.parsePolohaFrom(parsedData.locationRaw);

        ScrapedInzerat inzerat = getScrapedInzerat(inzeratUrl, website, typNabidky, typNemovitosti, webSegment, parsedData, adresaScrpd);

        return Optional.ofNullable(inzerat);
    }

    private boolean hasValue(Optional<String> paramVal) {
        return paramVal.isPresent() && !paramVal.get().equals("");
    }


    private Double parseLongitude(String locationString) {

        Matcher m = LONGITUDE_PATTERN.matcher(locationString);

        try {
            while (m.find()) {
                String lngStr = m.group().replace("x=", "").replace("&", "");
                return Double.valueOf(lngStr);
            }
        } catch (NumberFormatException ex) {
            log.error("Error parsing longitude from {}", locationString, ex);
        }

        return null;
    }


    private Double parseLatitude(String locationString) {

        Matcher m = LATITUDE_PATTERN.matcher(locationString);

        try {
            while (m.find()) {
                String lngStr = m.group().replace("y=", "").replace("&", "");
                return Double.valueOf(lngStr);
            }
        } catch (NumberFormatException ex) {
            log.error("Error parsing latitude from {}", locationString, ex);
        }

        return null;
    }

    private Boolean hasItem(boolean paramMarkedAsFalse) {
        return paramMarkedAsFalse ? Boolean.FALSE : Boolean.TRUE;
    }


    private Optional<DomElement> getPropertyDetailElem(HtmlPage page) {

        /*   <div class="property-detail ng-scope">   */

        List<DomElement> divElems = page.getElementsByTagName("div");

        for (DomElement divElem : divElems) {
            boolean isPropertyDetail = HtmlUnitUtils.hasAttributeWithValue(divElem, "class", "property-detail ng-scope", true);
            if (isPropertyDetail) {
                return Optional.ofNullable(divElem);
            }
        }

        return Optional.empty();
    }


    private Optional<DomElement> getPropertyTitleElem(DomElement propertyDetailElem) {

        /*   <div class="property-title">  */
        return HtmlUnitUtils.getChildElement(propertyDetailElem, "div", "class", "property-title", true);
    }

    private Optional<String> getLocationString(DomElement propertyDetailElem) {

        /*
            <a class="print" target="_blank"
                href="//mapy.cz/?x=14.340280&amp;y=50.037386&amp;z=18"
                style="position: absolute; right: 0px; bottom: 0px; left: auto; top: auto;"><img src="https://api.mapy.cz/img/api/logo.svg">
            </a>
         */

        return HtmlUnitUtils.getAttributeValueByItsExpectedPart(propertyDetailElem, "a", "href", "mapy.cz");
    }

    private Optional<String> getHeader(DomElement propertyTitleElem) {
        /*  <span class="name ng-binding">Prodej bytu 2+kk 77&nbsp;m²</span>  */
        return HtmlUnitUtils.getChildElementTextContent(propertyTitleElem, "span", "class", "name ng-binding", true);
    }


    private Optional<String> getLocation(DomElement propertyTitleElem) {

        /*   <span class="location"><span class="location-text ng-binding">ulice Nepilova, Praha 9 - část obce Vysočany</span>   */
        return HtmlUnitUtils.getChildElementTextContent(propertyTitleElem, "span", "class", "location", true);
    }


    private Optional<String> getDescription(DomElement propertyDetailElem) {

        /*  <div class="description ng-binding" itemprop="description" ng-bind-html="contentData.description | lineFilter">
                    <p>Nabízíme k prodeji moderní a velmi prostorný byt 2+kk s terasou,
                ...
         */
        return HtmlUnitUtils.getChildElementTextContent(propertyDetailElem, "div", "class", "description ng-binding", true);
    }

    private Optional<String> getContact(DomElement propertyDetailElem) {

        /*  <div class="contacts">
                ...
                <li class="line name ng-binding">Housa reality</li>
                 ....
         */
        List<DomElement> contactElems = HtmlUnitUtils.getAllChildElements(propertyDetailElem, "div", "class", "contacts", true);

        for (DomElement contactElem : contactElems) {
            Optional<String> contactName = HtmlUnitUtils.getChildElementTextContent(contactElem, "li", "class", "line name ng-binding", true);
            if (contactName.isPresent()) {
                return contactName;
            }
        }
        return Optional.empty();
    }


    private List<DomElement> getAllParamElems(DomElement propertyDetailElem) {

        /*   <li class="param ng-scope" ng-repeat="item in group.params"><label class="param-label ng-binding">Topení:</label><strong class="param-value">
                    <!-- ngIf: item.type == 'link' -->
                    <!-- ngIf: item.type != 'link' --><span ng-if="item.type != 'link'" class="ng-binding ng-scope">Ústřední dálkové</span><!-- end ngIf: item.type != 'link' -->
                    <!-- ngIf: item.type == 'area' -->
                    <!-- ngIf: item.type == 'boolean-true' -->
                    <!-- ngIf: item.type == 'boolean-false' -->
                    <!-- ngIf: item.type == 'topped' -->
                </strong></li>
         */

        return HtmlUnitUtils.getAllChildElements(propertyDetailElem, "li", "class", "param ng-scope", true);
    }

}
