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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.location.AdresaScrpd;


public class SRealityLocationResolver {

    private static final Logger log = LogManager.getLogger();

    private static final String TXT = "(\\p{L}|\\s|.)+"; // we might need to add more special chars appearing in the location names ...

    // ulice / nabrezi / namesti ...
    private static final String UL = TXT + ", ";

    // obec
    private static final String OB = TXT;

    // cast obce
    private static final String COB = " - " + TXT;

    // okres
    private static final String OK = ", okres" + TXT;


    // location combintions
    private static final String UL_OB_COB_OK    = UL + OB + COB + OK;
    private static final String UL_OB_OK        = UL + OB + OK;
    private static final String UL_OB_COB       = UL + OB + COB;
    private static final String OB_COB_OK       = OB + COB + OK;
    private static final String UL_OB           = UL + OB;
    private static final String OB_COB          = OB + COB;
    private static final String OB_OK           = OB + OK;

    /*

    // TODO handle this case: 'Praha 9 - Praha-Čakovice' ... remove 'Praha-'

        Examples:

        Ulrichovo náměstí,      Hradec Králové - část obce Hradec Králové, okres Hradec Králové
        Ulrichovo náměstí,      Hradec Králové - část obce Hradec Králové
        ulice Milady Horákové,  Hradec Králové - část obce Třebeš
                                Nový Bydžov    - část obce Zábědov      , okres Hradec Králové
                                Vrchlabí                                , okres Trutnov
        Střelecká,              Hradec Králové - část obce Hradec Králové
        Smetanovo nábřeží,      Hradec Králové - část obce Hradec Králové
        Jana Masaryka,          Hradec Králové - Nový Hradec Králové
                                Hradec Králové - Nový Hradec Králové
        Jana Masaryka,          Hradec Králové
        ulice Kocianova,        Praha 5        - část obce Stodůlky
        ulice Krásného,         Praha          - část obce Veleslavín
                                Praha 9
        ulice Českobrodská,     Praha
                                Praha 5        - část obce Zbraslav

                                ulice Petřínská, Praha - Praha 5
                                Praha 3 - část obce Strašnice
    */


    public static AdresaScrpd parsePolohaFrom(String srInzeratHeader) {

        AdresaScrpd adresaScrpd = new AdresaScrpd();
        if (srInzeratHeader == null) {
            return adresaScrpd;
        }

        String[] parts = srInzeratHeader.split(", | - ");

        if (parts.length == 0) {
            log.warn("Could not parse location parts from '{}'", srInzeratHeader);
            return adresaScrpd;
        }

        String ulice = null;
        String obec = null;
        String mestskaCtvrt = null;
        String mestskaCast = null;
        String okres = null;

        if (srInzeratHeader.matches(UL_OB_COB_OK)) {
            ulice = parseUlice(parts[0]);
            obec = parseObec(parts[1]);
            mestskaCtvrt = parseCastObce(parts[2]);
            mestskaCast = parseMestskaCast(parts[1]); // relevantni jen pro prahu ...
            okres = parseOkres(parts[3]);
        } else if (srInzeratHeader.matches(UL_OB_OK)) {
            ulice = parseUlice(parts[0]);
            obec = parseObec(parts[1]);
            mestskaCast = parseMestskaCast(parts[1]);
            okres = parseOkres(parts[2]);
        } else if (srInzeratHeader.matches(UL_OB_COB)) {
            ulice = parseUlice(parts[0]);
            obec = parseObec(parts[1]);
            mestskaCtvrt = parseCastObce(parts[2]);
            mestskaCast = parseMestskaCast(parts[1]); // relevantni jen pro prahu ...
        } else if (srInzeratHeader.matches(OB_COB_OK)) {
            obec = parseObec(parts[0]);
            mestskaCtvrt = parseCastObce(parts[1]);
            mestskaCast = parseMestskaCast(parts[0]); // relevantni jen pro prahu ...
            okres = parseOkres(parts[2]);
        } else if (srInzeratHeader.matches(OB_OK)) {
            obec = parseObec(parts[0]);
            mestskaCast = parseMestskaCast(parts[0]);
            okres = parseOkres(parts[1]);
        } else if (srInzeratHeader.matches(UL_OB)) {
            ulice = parseUlice(parts[0]);
            obec = parseObec(parts[1]);
            mestskaCast = parseMestskaCast(parts[1]);
        } else if (srInzeratHeader.matches(OB_COB)) {
            obec = parseObec(parts[0]);
            mestskaCtvrt = parseCastObce(parts[1]);
            mestskaCast = parseMestskaCast(parts[0]);
        } else if (srInzeratHeader.matches(OB)) {
            obec = parseObec(parts[0]);
            mestskaCast = parseMestskaCast(parts[0]);
        } else {
            log.warn("Could not parse location parts from '{}'", srInzeratHeader);
        }

        adresaScrpd.setUliceScrpd(ulice);
        adresaScrpd.setObecScrpd(obec);
        adresaScrpd.setCastObceScrpd(mestskaCtvrt);
        adresaScrpd.setCtvrtScrpd(mestskaCast);
        adresaScrpd.setOkresScrpd(okres);

        return adresaScrpd;
    }


    private static String parseUlice(String ulice) {
        return ulice.replace("ulice ", "").trim();
    }

    private static String parseObec(String obec) {
        if (obec.matches("Praha (\\d+)")) {
            return "Praha";
        } else {
            return obec.trim();
        }
    }

    private static String parseCastObce(String ctvrt) {
        if (ctvrt.contains("část obce ")) {
            return ctvrt.replace("část obce ", "").replace("Praha-", "").trim();
        } else if (ctvrt.matches("Praha (\\d+)")) {  // special case where we get Praha 10 as cast obce after " - "
            return null;
        } else {
            return ctvrt.replace("Praha-", "").trim();
        }
    }

    private static String parseMestskaCast(String obec) {
        if (obec.matches("Praha (\\d+)")) {
            return obec.trim();
        } else {
            return null;
        }
    }

    private static String parseOkres(String okres) {
        return okres.replace("okres ", "").trim();
    }

}
