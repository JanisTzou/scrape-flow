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

import aaanew.drivers.DriverManager;
import aaanew.utils.HtmlUnitUtils;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
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
import ret.webscrapers.scraping.data.inzeraty.parsers.model.ScrapedCoordinates;
import ret.webscrapers.scraping.data.inzeraty.parsers.model.ScrapedData;
import ret.webscrapers.scraping.data.inzeraty.parsers.model.ValuesMapper;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class IDnesRealityInzeratParser extends HtmlUnitInzeratParserBase {

    private static final Logger log = LogManager.getLogger();

    public IDnesRealityInzeratParser(DriverManager<WebClient> driverManager) {
        super(driverManager);
    }

    @Override
    public Optional<ScrapedInzerat> scrapeData(String inzeratUrl, WebsiteEnum website, TypNabidkyEnum typNabidky, TypNemovitostiEnum typNemovitosti, WebSegment webSegment) {

        ScrapedData parsedData = new ScrapedData();

        try {
            WebClient webClient = driverManager.getDriver();
            final HtmlPage page = getHtmlPage(inzeratUrl, webClient);
            Optional<DomElement> inzeratBodyOpt = getInzeratBody(page);

            if (inzeratBodyOpt.isPresent()) {

                HtmlElement pageBody = page.getBody();
                DomElement inzeratBody = inzeratBodyOpt.get();

                Optional<String> hlavickaInzeratuPlainOpt = parseHlavickaInzeratuPlain(inzeratBody, inzeratUrl);
                Optional<String> hlavickaInzeratuDetailedOpt = parseHlavickaInzeratuDetailed(inzeratBody, inzeratUrl);
                Optional<String> hlavickaInzeratuOpt = hlavickaInzeratuDetailedOpt;
                if (!hlavickaInzeratuOpt.isPresent()) {
                    hlavickaInzeratuOpt = hlavickaInzeratuPlainOpt;
                }
                parsedData.hlavickaInzeratu = hlavickaInzeratuOpt.orElse(null);

                if (typNemovitosti.isDispoziceSupported()) {
                    Optional<String> dispoziceOpt = parseDispozice(hlavickaInzeratuPlainOpt.orElse(""), inzeratUrl);
                    parsedData.dispozice = dispoziceOpt.orElse(null);
                }

                ScrapedCoordinates scrapedCoordinates = parseCoordinates(pageBody, inzeratUrl);
                parsedData.latitude = scrapedCoordinates.latitude;
                parsedData.longitude = scrapedCoordinates.longitude;

                // TODO we need to axtract the street from here .... we do it based on the adress below ...
                Optional<String> locationOpt = parseLocation(inzeratBody, inzeratUrl);

                AdresaScrpd adresaScrpd = parseAdresaScrpd(pageBody, inzeratUrl);

                Optional<String> prodavajiciFirstOpt = parseProdavajiciFirst(pageBody);
                if (prodavajiciFirstOpt.isPresent()) {
                    parsedData.prodavajici = prodavajiciFirstOpt.get();
                } else {
                    parsedData.prodavajici = parseProdavajiciSecond(pageBody, inzeratUrl).orElse(null);
                }

                DomNodeList<HtmlElement> dtElements = inzeratBody.getElementsByTagName("dt");

                for (HtmlElement dtElement : dtElements) {

                    String dtElementContent = dtElement.getTextContent().trim();
                    DomElement ddElement = dtElement.getNextElementSibling();
                    String ddElementContent = ddElement.getTextContent().trim();

                    switch (dtElementContent) {
                        case "Číslo zakázky":
                            parsedData.webNemovitostId = ddElementContent;
                            break;
                        case "Cena":
                            parsedData.cenaRaw = ddElement.getTextContent(); // untrimmed version needed ...
                            for (DomElement childElement : ddElement.getChildElements()) {
                                parsedData.cenaRaw = parsedData.cenaRaw.replace(childElement.getTextContent(), "");
                            }
                            parsedData.cenaRaw = parsedData.cenaRaw.trim();
                            parsedData.cena = parseMoney(parsedData.cenaRaw, "cena", inzeratUrl);
                            break;
                        case "Konstrukce budovy":
                            parsedData.typStavbyRaw = ddElementContent;
                            break;
                        case "Stav bytu":
                            parsedData.stavObjektuRaw = ddElementContent;
                            break;
                        case "Vlastnictví":
                            parsedData.vlastnictviRaw = ddElementContent;
                            break;
                        case "Užitná plocha":
                            parsedData.uzitnaPlochaRaw = ddElementContent;
                            parsedData.uzitnaPlocha = parsePlochaDouble(ddElementContent);
                            break;
                        case "Podlaží":
                            parsedData.podlaziRaw = ddElementContent;
                            parsedData.podlazi = parsePodlazi(ValuesMapper.mapValue(ddElementContent), inzeratUrl);
                            break;
                        case "Plocha pozemku":
                            parsedData.plochaPozemkuRaw = ddElementContent;
                            parsedData.plochaPozemku = parsePlochaDouble(ddElementContent);
                            break;
                        case "Plocha zahrady": // TODO importat ... sometimes included in uzitna plocha, as is also balcony and such ...
                            //  tohle je relavantni i pro byty, obcas tam maji pochu zahrady ...
                            break;
                        case "Výtah":
                            // TODO
                            break;
                        case "Vybavení":
                            // TODO
                            break;
                        case "Sklep":
                            // TODO
                            break;
                        case "Balkon":
                            // TODO
                            break;
                        case "Terasa":
                            // TODO
                            break;
                        case "Lodžie":
                            // TODO
                            break;
                        case "Lokalita objektu":
                        case "Lokalita projektu":
                        case "Topení":
                        case "Bazén":
                        case "Rekonstrukce":
                        case "Počet podzemních podlaží":
                        case "Kolaudace":
                        case "Elektřina":
                        case "PENB":
                        case "Parkování":
                        case "Internet":
                        case "Vybavení domu":
                        case "Poloha domu":
                        case "Voda":
                        case "Počet podlaží budovy":
                        case "Zastavěná plocha":
                        case "Odpad":
                        case "Výstavba": // obsahuje rok dokonceni stavby
                        case "Počet podlaží": // relevant for houses ... but probably unnecessary ...
                        case "Plyn":
                        case "Počet pokojů":
                        case "Televize":
                        case "Telefon":
                        case "Roční spotřeba energie":
                        case "Datum nastěhování":
                        case "Přístupová komunikace":
                        case "Občanská vybavenost":
                        case "Dopravní dostupnost":
                        case "Internet a TV":
                        case "Stav budovy":
                        case "Bezbariérový přístup":
                        case "Půdní vestavba":
                            break;
                        default:
                            log.warn("Unknown nemovitost attribute type: {}", dtElementContent);
                    }
                }

                // Special case when plocha is not available in the detail table -> try to get it from the discription header/hlavicka
                if (parsedData.uzitnaPlocha == null && parsedData.hlavickaInzeratu != null) {
                    String uzitnaPlocha = parsePlochaUzitnaFromHlavicka(parsedData.hlavickaInzeratu).orElse(null);
                    parsedData.uzitnaPlocha = parsePlochaDouble(uzitnaPlocha);
                    if (parsedData.uzitnaPlocha != null) {
                        log.info("Parsed uzitnaPlocha from detailed hlavickaInzeratu!");
                    }
                }

                ScrapedInzerat scrapedInzerat = getScrapedInzerat(inzeratUrl, website, typNabidky, typNemovitosti, webSegment, parsedData, adresaScrpd);
                return Optional.of(scrapedInzerat);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }


    private Optional<String> parseLocation(DomElement inzeratBody, String inzeratUrl) {
        /*
            <p class="b-detail__info">
                Opletalova, Praha 1 - Nové Město
            </p>
         */
        Optional<String> hlavickaInzeratu = HtmlUnitUtils.getChildElementTextContent(inzeratBody, "p", "class", "b-detail__info", true);
        if (!hlavickaInzeratu.isPresent()) {
            log.warn("Failed to parse Adresa from inzerat: {}", inzeratUrl);
        }
        return hlavickaInzeratu;
    }

    private Optional<String> parseHlavickaInzeratuPlain(DomElement inzeratBody, String inzeratUrl) {
        /*
            <h1 class="b-detail__title">
                <span>Prodej bytu 2+1, 88 m²</span>
            </h1>
         */
        Optional<String> hlavickaInzeratu = HtmlUnitUtils.getChildElementTextContent(inzeratBody, "h1", "class", "b-detail__title", true);
        if (!hlavickaInzeratu.isPresent()) {
            log.warn("Failed to parse hlavickaInzeratu from inzerat: {}", inzeratUrl);
        }
        return hlavickaInzeratu;
    }

    private Optional<String> parseHlavickaInzeratuDetailed(DomElement inzeratBody, String inzeratUrl) {
        /*
            <div class="b-desc pt-10 mt-10">
                <h2>
                    Prodej bytu 4+kk, 102 m2, Praha-Libeň
                </h2>
                <p>
                    Prostorný byt 4 + kk se nachází na ulici Františka Kadlece v Libni. Okolní zástavbu tvoří bytové domy původní městské zástavby, v současné době
                    ....
                </p>
            </div>
         */

        // IMPORTANT the space adfter 'b-desc' is intentiaonal
        Optional<DomElement> hlavickaInzeratuDiv = HtmlUnitUtils.getChildElement(inzeratBody, "div", "class", "b-desc ", false);
        if (!hlavickaInzeratuDiv.isPresent()) {
            log.warn("Failed to parse detailed hlavickaInzeratu div elements from: {}", inzeratUrl);
        } else {
            List<String> h2ElementsContent = HtmlUnitUtils.getChildElementsTextContent(hlavickaInzeratuDiv.get(), "h2");
            if (h2ElementsContent.size() == 1) {
                return Optional.ofNullable(h2ElementsContent.get(0));
            } else {
                log.warn("Unexpected h2 elements count! Cannot parse HlavickaInzeratuDetail from: {}", inzeratUrl);
            }
        }
        return Optional.empty();
    }

    private Optional<String> parsePlochaUzitnaFromHlavicka(String hlavickaInzeratu) {
        if (hlavickaInzeratu != null) {
            List<String> results = StringUtil.parse(hlavickaInzeratu, Pattern.compile("\\d+(\\.\\d+)? m2"));
            if (results.size() == 1) {
                return Optional.ofNullable(results.get(0).replace(" m2", ""));
            }
        }
        return Optional.empty();
    }

    private AdresaScrpd parseAdresaScrpd(DomElement inzeratBody, String inzeratUrl) {

        /*
            <script type="text/javascript">
                if (typeof dataLayer == 'object') {
                    dataLayer.push({
                        ....
                            "listing_localityRegion":"Liberecký kraj",   <- kraj
                            "listing_localityDistrict":"Česká Lípa",     <- praha 2
                            "listing_localityCity":"Kamenický Šenov",    <- Praha
                            "listing_localityCityArea":"Prácheň",        <- Nove mesto, Nusle ...
                        ....
                }
            </script>
        */

        final String listing_localityRegion = "listing_localityRegion";
        final String listing_localityDistrict = "listing_localityDistrict";
        final String listing_localityCity = "listing_localityCity";
        final String listing_localityCityArea = "listing_localityCityArea";

        List<String> coordinatesFields = List.of(listing_localityRegion, listing_localityDistrict, listing_localityCity, listing_localityCityArea);
        Optional<String> textContentOpt = getScriptWithFields(inzeratBody, coordinatesFields);

        if (textContentOpt.isPresent()) {
            String[] keyValuePairs = textContentOpt.get().split(",");
            String kraj = getScriptFieldValue(keyValuePairs, listing_localityRegion);
            String okres = getScriptFieldValue(keyValuePairs, listing_localityDistrict);
            String obec = getScriptFieldValue(keyValuePairs, listing_localityCity);
            String ctvrt = getScriptFieldValue(keyValuePairs, listing_localityCityArea);

            if (obec.toLowerCase().contains("praha")) {
                // this correction needed as sometimes the city is given as e.g. 'Praha 8' instead of 'Praha'
                obec = "Praha";
            }

            // TODO add the rest ...
            return new AdresaScrpd(null, null, ctvrt, null, obec, okres, kraj);
        }

        log.warn("Failed to scrape AdresaScrpd for: {}", inzeratUrl);
        return new AdresaScrpd();

        /*
        OUTPUT EXAMPLES:

        NOTE: Praha 1, 2 .. jsou vedeny jako okresy a skutecne jsou i na teto urovni realne ...

                Optional[Karoliny Světlé, Praha 1 - Staré Město]
                AdresaScrpd{polohaScrpd='', uliceScrpd='', ctvrtScrpd='"Staré Město"', castObceScrpd=' ', obecScrpd='"Praha"', okresScrpd='"Praha 1"', krajScrpd='"Hlavní město Praha"'}
                Optional[Opatovická, Praha 1 - Nové Město]
                AdresaScrpd{polohaScrpd=' ', uliceScrpd=' ', ctvrtScrpd='"Nové Město"', castObceScrpd=' ', obecScrpd='"Praha"', okresScrpd='"Praha 1"', krajScrpd='"Hlavní město Praha"'}
                Optional[Kamenický Šenov - Prácheň, okres Česká Lípa]
                AdresaScrpd{polohaScrpd=' ', uliceScrpd=' ', ctvrtScrpd='"Prácheň"', castObceScrpd=' ', obecScrpd='"Kamenický Šenov"', okresScrpd='"Česká Lípa"', krajScrpd='"Liberecký kraj"'}
                Optional[Šternberská, Uničov, okres Olomouc]
                AdresaScrpd{polohaScrpd=' ', uliceScrpd=' ', ctvrtScrpd='"Uničov"', castObceScrpd=' ', obecScrpd='"Uničov"', okresScrpd='"Olomouc"', krajScrpd='"Olomoucký kraj"'}
                Optional[Lesnická, Plumlov, okres Prostějov]
                AdresaScrpd{polohaScrpd=' ', uliceScrpd=' ', ctvrtScrpd='"Plumlov"', castObceScrpd=' ', obecScrpd='"Plumlov"', okresScrpd='"Prostějov"', krajScrpd='"Olomoucký kraj"'}
                Optional[Brněnská, Olomouc - Nová Ulice]
                AdresaScrpd{polohaScrpd=' ', uliceScrpd=' ', ctvrtScrpd='"Nová Ulice"', castObceScrpd=' ', obecScrpd='"Olomouc"', okresScrpd='"Olomouc"', krajScrpd='"Olomoucký kraj"'}
         */

    }


    private ScrapedCoordinates parseCoordinates(DomElement inzeratBody, String inzeratUrl) {

        /*
            <script type="text/javascript">
                if (typeof dataLayer == 'object') {
                    dataLayer.push({
                        ....
                        "listing_lat":50.7706,
                        "listing_lon":14.4958,
                        ....
                }
            </script>
        */
        final String listing_lon = "listing_lon";
        final String listing_lat = "listing_lat";

        List<String> coordinatesFields = List.of(listing_lat, listing_lon);
        Optional<String> textContentOpt = getScriptWithFields(inzeratBody, coordinatesFields);

        if (textContentOpt.isPresent()) {
            String[] keyValuePairs = textContentOpt.get().split(",");
            String latitude = getScriptFieldValue(keyValuePairs, listing_lat);
            String longitude = getScriptFieldValue(keyValuePairs, listing_lon);

            if (latitude != null && longitude != null) {
                Double latitudeDouble = Double.valueOf(latitude);
                Double longitudeDouble = Double.valueOf(longitude);
                return new ScrapedCoordinates(latitudeDouble, longitudeDouble);
            }
        }

        log.warn("Failed to scrape latitude and/or longitude for: {}", inzeratUrl);
        return new ScrapedCoordinates(null, null);
    }


    private String getScriptFieldValue(String[] keyValuePairs, String fieldName) {
        for (String keyValuePair : keyValuePairs) {
            if (keyValuePair.contains(fieldName)) {
                String value = keyValuePair.split(":")[1].trim().replace("\"", "").trim();
                if (value.equals("null")) {
                    value = null;
                }
                return value;
            }
        }
        return null;
    }


    private Optional<String> getScriptWithFields(DomElement inzeratBody, List<String> fieldsToFind) {
        List<DomElement> scriptElements = HtmlUnitUtils.getAllChildElements(inzeratBody, "script", "type", "text/javascript", true);
        for (DomElement scriptElement : scriptElements) {
            String textContent = scriptElement.getTextContent();
            boolean hasCoordinates = fieldsToFind.stream().anyMatch(field -> textContent.contains(field));
            if (hasCoordinates) {
                return Optional.of(textContent);
            }
        }
        return Optional.empty();
    }


    private Optional<String> parseDispozice(String hlavickaInzeratuPlain, String inzeratUrl) {
        // Prodej bytu 2+1, 88 m²  ... before parsing
        String[] split = hlavickaInzeratuPlain.split(",");
        if (split.length > 0) {
            String dispozice = split[0]
                    .replace("Prodej bytu", "")
                    .replace("Prodej domu", "")
                    .replace("Pronájem bytu", "")
                    .replace("Pronájem domu", "")
                    .trim();
            if (!dispozice.isEmpty()) {
                return Optional.of(dispozice);
            }
        }
        log.warn("Failed to parse dispozice from inzerat: {}", inzeratUrl);
        return Optional.empty();
    }

    private Optional<DomElement> getInzeratBody(HtmlPage page) {
        /*   <div class="row-main"> .... </div>  */
        List<DomElement> divElems = page.getElementsByTagName("div");
        for (DomElement divElem : divElems) {
            boolean isPropertyDetail = HtmlUnitUtils.hasAttributeWithValue(divElem, "class", "row-main", true);
            if (isPropertyDetail) {
                return Optional.ofNullable(divElem);
            }
        }
        return Optional.empty();
    }


    private Optional<String> parseProdavajiciFirst(DomElement inzeratBody) {
    /*
        <div class="b-author__content" id="pull-me">
            <p class="b-author__info">
                <a href="https://reality.idnes.cz/rk/detail/ranny-architects-s-r-o/5b8546bda26e3a707739fec9/">Ranný architects,
                    s.r.o.</a>
            </p>
            <p class="b-author__info">
                ...
            </p>
        </div>
     */
        List<DomElement> divElems = HtmlUnitUtils.getAllChildElements(inzeratBody, "div", "class", "hide-desktop-down", true);
        for (DomElement divElem : divElems) {
            Optional<String> realitkaName = HtmlUnitUtils.getChildElementTextContent(divElem, "p", "class", "b-author__info", true);
            if (realitkaName.isPresent()) {
                return realitkaName;
            }
        }
        return Optional.empty();
    }

    /**
     * IMPORTANT This sometimes returns some strange company names, unrelated to the actual prodavajci ... this should be the second backup option
     */
    private Optional<String> parseProdavajiciSecond(DomElement inzeratBody, String inzeratUrl) {
        //  like here: https://reality.idnes.cz/detail/prodej/byt/praha-8-molakova/5cf39618558f073a82050ea3/
        final String listing_brand = "listing_brand";
        List<String> realitkaField = List.of(listing_brand);
        Optional<String> textContentOpt = getScriptWithFields(inzeratBody, realitkaField);
        if (textContentOpt.isPresent()) {
            String[] keyValuePairs = textContentOpt.get().split(",");
            String realitka = getScriptFieldValue(keyValuePairs, listing_brand);
            return Optional.ofNullable(realitka);
        }
        return Optional.empty();
    }



}
