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

package ret.webscrapers.scraping.data.inzeraty.parsers.model;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ret.appcore.model.enums.*;

public class EnumMapper {

    private static Logger log = LogManager.getLogger();

    public static VlastnictviEnum mapToVlastnictviEnum(String vlastnictviStr) {
        if (vlastnictviStr == null) {
            return VlastnictviEnum.UNKNOWN;
        }
        switch (vlastnictviStr.toLowerCase()) {
            case "osobní":
                return VlastnictviEnum.OSOBNI;
            case "družstevní":
                return VlastnictviEnum.DRUZSTEVNI;
            case "státní/obecní":
                return VlastnictviEnum.STATNI_OBECNI;
            case "jiný":
                return VlastnictviEnum.UNKNOWN;
            default:
                log.error("Unknown Vlastnictvi: '{}'", vlastnictviStr);
                return VlastnictviEnum.UNKNOWN;
        }
    }


    public static DispoziceEnum mapToDispoziceEnum(String dispoziceStr) {
        if (dispoziceStr == null) {
            return DispoziceEnum.UNKNOWN;
        }
        switch (dispoziceStr.toLowerCase()) {
            case "1+kk":
                return DispoziceEnum._1_kk;
            case "1+1":
                return DispoziceEnum._1_1;
            case "2+kk":
                return DispoziceEnum._2_kk;
            case "2+1":
                return DispoziceEnum._2_1;
            case "3+kk":
                return DispoziceEnum._3_kk;
            case "3+1":
                return DispoziceEnum._3_1;
            case "4+kk":
                return DispoziceEnum._4_kk;
            case "4+1":
                return DispoziceEnum._4_1;
            case "5+kk":
                return DispoziceEnum._5_kk;
            case "5+1":
                return DispoziceEnum._5_1;
            case "6+kk":
                return DispoziceEnum._6_kk;
            case "6+1":
                return DispoziceEnum._6_1;
            case "6 a více":
            case "6 pokojů a více":
                return DispoziceEnum._6_A_VICE;
            case "atypické":
            case "atypického":
                return DispoziceEnum.ATYPICKY;
            default:
                log.error("Unknown Dispozice: '{}'", dispoziceStr);
                return DispoziceEnum.UNKNOWN;
        }
    }


    public static StavObjektuEnum mapToStavObjektuEnum(String stavObjektuStr) {
        if (stavObjektuStr == null) {
            return StavObjektuEnum.UNKNOWN;
        }
        switch (stavObjektuStr.toLowerCase()) {
            case "špatný":
            case "špatný stav":
                return StavObjektuEnum.SPATNY;
            case "dobrý":
            case "dobrý stav":
                return StavObjektuEnum.DOBRY;
            case "udržovaný":
                return StavObjektuEnum.UDRZOVANY;
            case "ve výstavbě":
                return StavObjektuEnum.VE_VYSTAVBE;
            case "projekt":
                return StavObjektuEnum.PROJEKT;
            case "novostavba":
                return StavObjektuEnum.NOVOSTAVBA;
            case "k demolici":
                return StavObjektuEnum.K_DEMOLICI;
            case "před rekonstrukcí":
                return StavObjektuEnum.PRED_REKONSTRUKCI;
            case "po rekonstrukci":
                return StavObjektuEnum.PO_REKONSTRUKCI;
            case "velmi dobrý":
                return StavObjektuEnum.VELMI_DOBRY;
            default:
                log.error("Unknown StavObjektu: '{}'", stavObjektuStr);
                return StavObjektuEnum.UNKNOWN;
        }
    }


    public static TypStavbyEnum mapTypStavbyEnum(String typStavby) {
        if (typStavby == null) {
            return TypStavbyEnum.UNKNOWN;
        }
        switch (typStavby.toLowerCase()) {
            case "cihlová":
                return TypStavbyEnum.CIHLA;
            case "panelová":
                return TypStavbyEnum.PANEL;
            case "smíšená":
                return TypStavbyEnum.SMISENA;
            case "skeletová":
                return TypStavbyEnum.SKELETOVA;
            case "dřevěná":
                return TypStavbyEnum.DREVENA;
            case "montovaná":
                return TypStavbyEnum.MONTOVANA;
            case "kamenná":
                return TypStavbyEnum.KAMENNA;
            default:
                log.error("Unknown TypStavby: '{}'", typStavby);
                return TypStavbyEnum.UNKNOWN;
        }
    }


    public static StavNabidkyEnum mapStavNabidkyEnum(String stavNabidkyStr) {
        if (stavNabidkyStr == null) {
            return StavNabidkyEnum.UNKNOWN;
        }
        switch (stavNabidkyStr.toLowerCase()) {
            case "rezervováno":
                return StavNabidkyEnum.REZERVOVANO;
            case "prodáno":
                return StavNabidkyEnum.PRODANO;
            default:
                log.error("Unknown StavNabidky: '{}'", stavNabidkyStr);
                return StavNabidkyEnum.UNKNOWN;
        }
    }

}
