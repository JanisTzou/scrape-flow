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

public class ScrapedData {

    public String hlavickaInzeratu = "";
    public String locationRaw = "";

    public String cenaRaw = null;
    public String poznamkaKCeneRaw = "";
    public String aktualizaceNaWebu = null;
    public String webNemovitostId = null;
    public String typStavbyRaw = null;
    public String stavObjektuRaw = null;
    public String vlastnictviRaw = null;
    public String stavNabidkyRaw = null;
    public String podlaziRaw = null;
    public Integer podlazi = null;

    public String uzitnaPlochaRaw = null;
    public String plochaPodlahovaRaw = null;
    public String plochaPozemkuRaw = null;
    public Double plochaPozemku = null;

    public String energetickaNarocnostBudovy = null;
    public String vybaveni = null;

    public String prodavajici = null;
    public String description = ""; // needs to be empty String

    public String dispozice = null;
    public Double cena = null;

    public Double uzitnaPlocha = null;
    public Double plochaPodlahova = null;

    public Boolean maTerasu = Boolean.FALSE;
    public Double terasaPlocha = null;

    public Boolean maGaraz = Boolean.FALSE;
    public Double garazPlocha = null;
    public Integer pocetGarazi = null;

    public Boolean maSklep = Boolean.FALSE;
    public Double sklepPlocha = null;

    public Boolean maBalkon = Boolean.FALSE;
    public Double balkonPlocha = null;

    public Boolean maLodgie = Boolean.FALSE;
    public Double lodgiePlocha = null;

    public Boolean maVytah;
    public Boolean maParkovani;
    public Boolean maBazen;
    public Double plochaBazenu;
    public Integer pocetParkovani = 0;
    public String rokKolaudace = null;
    public String rokRekonstrukce = null;
    public String datumZahajeniProdeje = null;

    public String anuitaRaw = null;
    public Double anuita = null;
    public String provizeRaw = null;
    public Double provize = null;
    public String puvodniCenaRaw = null;
    public Double puvodniCena = null;
    public Double plochaZahdrady = null;

    public Double latitude = null;
    public Double longitude = null;

}
