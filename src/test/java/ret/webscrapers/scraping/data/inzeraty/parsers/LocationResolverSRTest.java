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


import org.junit.Test;
import ret.appcore.model.location.AdresaScrpd;

import static org.junit.Assert.assertEquals;

public class LocationResolverSRTest {

    @Test
    public void parsePolohaFrom() {

        SRealityLocationResolver lr = new SRealityLocationResolver();
        AdresaScrpd polohaINZ;

        polohaINZ = lr.parsePolohaFrom("Ulrichovo náměstí, Hradec Králové - část obce Nove Dvory, okres okr. Hradec Králové");

        assertEquals("Ulrichovo náměstí", polohaINZ.getUliceScrpd());
        assertEquals("Hradec Králové", polohaINZ.getObecScrpd());
        assertEquals("Nove Dvory", polohaINZ.getCastObceScrpd());
        assertEquals( null, polohaINZ.getCtvrtScrpd());
        assertEquals("okr. Hradec Králové", polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("Ulrichovo náměstí, Hradec Králové, okres okr. Hradec Králové");

        assertEquals("Ulrichovo náměstí", polohaINZ.getUliceScrpd());
        assertEquals("Hradec Králové", polohaINZ.getObecScrpd());
        assertEquals(null, polohaINZ.getCtvrtScrpd());
        assertEquals( null, polohaINZ.getCastObceScrpd());
        assertEquals("okr. Hradec Králové", polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("Ulrichovo náměstí, Hradec Králové");

        assertEquals("Ulrichovo náměstí", polohaINZ.getUliceScrpd());
        assertEquals("Hradec Králové", polohaINZ.getObecScrpd());
        assertEquals(null, polohaINZ.getCtvrtScrpd());
        assertEquals( null, polohaINZ.getCastObceScrpd());
        assertEquals(null, polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("Hradec Králové - část obce Nove Dvory");

        assertEquals(null, polohaINZ.getUliceScrpd());
        assertEquals("Hradec Králové", polohaINZ.getObecScrpd());
        assertEquals("Nove Dvory", polohaINZ.getCastObceScrpd());
        assertEquals( null, polohaINZ.getCtvrtScrpd());
        assertEquals(null, polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("Hradec Králové - Nove Dvory");

        assertEquals(null, polohaINZ.getUliceScrpd());
        assertEquals("Hradec Králové", polohaINZ.getObecScrpd());
        assertEquals("Nove Dvory", polohaINZ.getCastObceScrpd());
        assertEquals( null, polohaINZ.getCastObceScrpd());
        assertEquals(null, polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("Praha 11");

        assertEquals(null, polohaINZ.getUliceScrpd());
        assertEquals("Praha", polohaINZ.getObecScrpd());
        assertEquals(null, polohaINZ.getCtvrtScrpd());
        assertEquals( "Praha 11", polohaINZ.getCastObceScrpd());
        assertEquals(null, polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("Vrchlabí, okres Trutnov");

        assertEquals(null, polohaINZ.getUliceScrpd());
        assertEquals("Vrchlabí", polohaINZ.getObecScrpd());
        assertEquals(null, polohaINZ.getCtvrtScrpd());
        assertEquals( null, polohaINZ.getCastObceScrpd());
        assertEquals("Trutnov", polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("ulice Petřínská, Praha - Praha 5");

        assertEquals("Petřínská", polohaINZ.getUliceScrpd());
        assertEquals("Praha", polohaINZ.getObecScrpd());
        assertEquals(null, polohaINZ.getCtvrtScrpd());
        assertEquals( null, polohaINZ.getCastObceScrpd()); // we do not handle this case
        assertEquals(null, polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("Praha 3 - část obce Strašnice");

        assertEquals(null, polohaINZ.getUliceScrpd());
        assertEquals("Praha", polohaINZ.getObecScrpd());
        assertEquals("Strašnice", polohaINZ.getCtvrtScrpd());
        assertEquals( "Praha 3", polohaINZ.getCastObceScrpd());
        assertEquals(null, polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("ulice Provázkova, Praha");

        assertEquals("Provázkova", polohaINZ.getUliceScrpd());
        assertEquals("Praha", polohaINZ.getObecScrpd());
        assertEquals(null, polohaINZ.getCtvrtScrpd());
        assertEquals( null, polohaINZ.getCastObceScrpd());
        assertEquals(null, polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("Praha 5 - Praha-Lochkov");

        assertEquals(null, polohaINZ.getUliceScrpd());
        assertEquals("Praha", polohaINZ.getObecScrpd());
        assertEquals("Praha-Lochkov", polohaINZ.getCtvrtScrpd());
        assertEquals( "Praha 5", polohaINZ.getCastObceScrpd());
        assertEquals(null, polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("Na Pomezí, Praha 5 - Jinonice");
        assertEquals("Na Pomezí", polohaINZ.getUliceScrpd());
        assertEquals("Praha", polohaINZ.getObecScrpd());
        assertEquals("Jinonice", polohaINZ.getCtvrtScrpd());
        assertEquals( "Praha 5", polohaINZ.getCastObceScrpd());
        assertEquals(null, polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("ulice Veronské nám., Praha 10 - část obce Horní Měcholupy");
        assertEquals("Veronské nám.", polohaINZ.getUliceScrpd());
        assertEquals("Praha", polohaINZ.getObecScrpd());
        assertEquals("Horní Měcholupy", polohaINZ.getCtvrtScrpd());
        assertEquals( "Praha 10", polohaINZ.getCastObceScrpd());
        assertEquals(null, polohaINZ.getOkresScrpd());

        polohaINZ = lr.parsePolohaFrom("Polepy - část obce Encovany, okres Litoměřice");
        assertEquals(null, polohaINZ.getUliceScrpd());
        assertEquals("Polepy", polohaINZ.getObecScrpd());
        assertEquals("Encovany", polohaINZ.getCtvrtScrpd());
        assertEquals( null, polohaINZ.getCastObceScrpd());
        assertEquals("Litoměřice", polohaINZ.getOkresScrpd());

        System.out.println(polohaINZ);

    }
}
