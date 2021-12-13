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
import aaanew.drivers.DriverOperatorBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.*;
import ret.appcore.model.enums.*;
import ret.appcore.model.location.AdresaScrpd;
import ret.appcore.model.location.Coordinates;
import ret.appcore.model.validation.InzeratValidator;
import ret.appcore.model.validation.ScrapedProdavajiciSanitizer;
import ret.webscrapers.scraping.data.inzeraty.parsers.model.EnumMapper;
import ret.webscrapers.scraping.data.inzeraty.parsers.model.ScrapedData;
import ret.webscrapers.scraping.data.inzeraty.parsers.model.ValuesMapper;

import java.time.LocalDateTime;
import java.util.List;

abstract class InzeratParserBase<T> extends DriverOperatorBase<T> implements InzeratParser {

    private static final Logger log = LogManager.getLogger();

    public InzeratParserBase(DriverManager<T> driverManager) {
        super(driverManager);
    }

    protected Double calcCenaZaM2(Double cenaDeriv, Double uzitnaPlocha) {
        try {
            if (cenaDeriv != null && uzitnaPlocha != null && uzitnaPlocha > 0.0) {
                return cenaDeriv / uzitnaPlocha;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected ScrapedInzerat getScrapedInzerat(String inzeratUrl, WebsiteEnum website, TypNabidkyEnum typNabidky, TypNemovitostiEnum typNemovitosti, WebSegment webSegment, ScrapedData parsedData, AdresaScrpd adresaScrpd) {
        String dispoziceScrpd = parsedData.hlavickaInzeratu;
        StavNabidkyEnum stavNabidky = parsedData.stavNabidkyRaw == null ? StavNabidkyEnum.AKTIVNI : EnumMapper.mapStavNabidkyEnum(parsedData.stavNabidkyRaw);
        String prodavajiciScrpdSanitized = sanitizeProdavajici(parsedData.prodavajici);
        InzeratInfoScrpd inzeratInfoScrpd = new InzeratInfoScrpd(parsedData.hlavickaInzeratu, parsedData.stavNabidkyRaw, prodavajiciScrpdSanitized);
        InzeratInfo inzeratInfo = new InzeratInfo(typNabidky, website, parsedData.webNemovitostId, stavNabidky, prodavajiciScrpdSanitized, inzeratUrl);

        DispoziceEnum dispozice = EnumMapper.mapToDispoziceEnum(parsedData.dispozice);
        TypStavbyEnum typStavby = EnumMapper.mapTypStavbyEnum(parsedData.typStavbyRaw);
        StavObjektuEnum stavObjektu = EnumMapper.mapToStavObjektuEnum(parsedData.stavObjektuRaw);
        VlastnictviEnum vlastnictvi = EnumMapper.mapToVlastnictviEnum(parsedData.vlastnictviRaw);

        NemovitostInfoScrpd nemovitostInfoScrpd = new NemovitostInfoScrpd(dispoziceScrpd, parsedData.typStavbyRaw, parsedData.stavObjektuRaw, parsedData.vlastnictviRaw, parsedData.podlaziRaw, parsedData.uzitnaPlochaRaw, parsedData.plochaPodlahovaRaw, parsedData.plochaPozemkuRaw);
        NemovitostInfo nemovitostInfo = new NemovitostInfo(typNemovitosti, dispozice, typStavby, stavObjektu, vlastnictvi, parsedData.podlazi, parsedData.uzitnaPlocha, parsedData.plochaPodlahova, parsedData.plochaPozemku, parsedData.uzitnaPlocha);
        NemovitostInfoExtended nemovitostInfoExtended = new NemovitostInfoExtended(parsedData.maBalkon, parsedData.balkonPlocha, parsedData.maLodgie, parsedData.lodgiePlocha, parsedData.maTerasu, parsedData.terasaPlocha, parsedData.maSklep, parsedData.sklepPlocha, parsedData.maGaraz, parsedData.garazPlocha, parsedData.energetickaNarocnostBudovy, parsedData.vybaveni, parsedData.description);

        Coordinates coordinates = new Coordinates(parsedData.latitude, parsedData.longitude);

        boolean provizeVCene = false; // TODO
        boolean pravniSluzbyVCene = false; // TODO

        CenaScrpd cenaScrpdObj = new CenaScrpd(parsedData.cenaRaw, parsedData.poznamkaKCeneRaw, parsedData.anuitaRaw, parsedData.provizeRaw);
        CenaInfo cenaInfo = new CenaInfo(parsedData.cena, provizeVCene, pravniSluzbyVCene, parsedData.anuita, parsedData.provize);

        String inzeratIdentifier = Inzerat.makeIdentifier(inzeratUrl);

        List<String> emptyMandatoryFields = InzeratValidator.getEmptyMandatoryFields(inzeratInfo, coordinates, nemovitostInfo);
        if (!emptyMandatoryFields.isEmpty()) {
            log.warn("ScrapedInzerat {} has following mandatry fields empty: {}", inzeratIdentifier, emptyMandatoryFields);
        }

        LocalDateTime now = LocalDateTime.now();
        return new ScrapedInzerat(
                inzeratIdentifier,
                inzeratInfoScrpd,
                inzeratInfo,
                adresaScrpd,
                coordinates,
                cenaScrpdObj,
                cenaInfo,
                nemovitostInfoScrpd,
                nemovitostInfo,
                nemovitostInfoExtended,
                webSegment,
                new ScrapedInzeratStatus(ScrapedInzeratStatusEnum.CAN_BE_PROCESSED),
                now,
                now
        );
    }


    protected String parsePlochaString(String plochaScrpd) {
        return plochaScrpd.toLowerCase().replace("m2", "").trim();
    }

    protected Double parsePlochaDouble(String plochaScrpd) {
        try {
            String plocha = parsePlochaString(plochaScrpd);
            return plocha.equals("") ? null : Double.valueOf(plocha);
        } catch (NumberFormatException e) {
            log.error("Error extracting area:", e);
            return null;
        }
    }

    protected Double parseMoney(String monayString, String attributeName, String inzeratUrl) {
        if (monayString == null) {
            return null;
        }
        String amountStr = monayString.split("Kč")[0].replace(" ", "").replace(" ", "").trim();
        return parseDouble(amountStr, attributeName, inzeratUrl);
    }


    protected Double parseDouble(String value, String attributeName, String inzeratUrl) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("Could not parse {} from '{}', for url = {}", attributeName, value, inzeratUrl);
        }
        return null;
    }

    protected Integer parseInteger(String value, String attributeName, String inzeratUrl) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("Could not parse {} from '{}', for url = {}", attributeName, value, inzeratUrl);
        }
        return null;
    }

    protected Integer parsePodlazi(String podlaziScrpd, String inzeratUrl) {
        try {
            podlaziScrpd = ValuesMapper.mapValue(podlaziScrpd);
            if (podlaziScrpd.toLowerCase().startsWith("přízemí")) {
                return 1;
            } else if (podlaziScrpd.contains(" ")) {
                return Integer.valueOf(podlaziScrpd.split(" ")[0].replace(".", ""));
            } else {
                return Integer.valueOf(podlaziScrpd);
            }
        } catch (NumberFormatException e) {
            log.warn("Could not parse podlazi from '{}', for url = {}", podlaziScrpd, inzeratUrl);
        }
        return null;
    }


    protected String sanitizeProdavajici(String prodavajiciName) {
        if (prodavajiciName != null) {
            String sanitized = ScrapedProdavajiciSanitizer.sanitize(prodavajiciName, true);
            log.info("Sanitized prodavajiciName: {} -> {}", prodavajiciName, sanitized);
            return sanitized;
        }
        return prodavajiciName;
    }

}
