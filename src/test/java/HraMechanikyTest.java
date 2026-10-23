import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integračné testy skutočných herných príkazov v triede Hra.
 * Existujúce testy projektu zostávajú zachované; toto sú nové testy mechaník.
 */
class HraMechanikyTest {

    @Test
    void zamknuteDvereNedovoliaVstupDoRiadiacehoCentra() throws Exception {
        try (TestovaciaHra test = new TestovaciaHra()) {
            test.prikaz("chod riadiace centrum");

            assertEquals(0, test.hrac().getMiestnost(),
                    "Hráč musí zostať v laboratóriu, ak sú dvere zamknuté.");
            assertTrue(test.vystup().contains("Dvere sú zamknuté"));
            assertFalse(test.dvereSuOdomknute(0));
        }
    }

    @Test
    void kodLaboratoriaVyžadujePoznamkyAJichPrecitanie() throws Exception {
        try (TestovaciaHra test = new TestovaciaHra()) {
            test.prikaz("kod");
            assertTrue(test.vystup().contains("Možno ti pomôžu poznámky"));
            assertFalse(test.dvereSuOdomknute(0));

            test.prikaz("zober poznamky");
            test.prikaz("kod");
            assertTrue(test.vystup().contains("Najprv si prečítaj poznámky"));
            assertFalse(test.dvereSuOdomknute(0));

            test.prikaz("precitaj");
            test.posliVstup(Integer.toString(test.intPole("kodLaboratorium")));
            test.prikaz("kod");

            assertTrue(test.dvereSuOdomknute(0),
                    "Po prečítaní poznámok a zadaní správneho kódu sa dvere odomknú.");
            test.prikaz("chod riadiace centrum");
            assertEquals(1, test.hrac().getMiestnost());
        }
    }

    @Test
    void neplatnyVstupAKancelovanieKoduNeodomknuDvere() throws Exception {
        try (TestovaciaHra test = new TestovaciaHra()) {
            test.prikaz("zober poznamky");
            test.prikaz("precitaj");
            test.posliVstup("12x", "spat");

            test.prikaz("kod");

            assertTrue(test.vystup().contains("Zadaj štvorciferné číslo"));
            assertTrue(test.vystup().contains("Zrušil si zadávanie kódu"));
            assertFalse(test.dvereSuOdomknute(0),
                    "Neplatný alebo zrušený kód nesmie odomknúť dvere.");
        }
    }

    @Test
    void vedecJeDostupnyLenVRiadiacomCentreAZopakujeKod() throws Exception {
        try (TestovaciaHra test = new TestovaciaHra()) {
            int kodZamku = test.intPole("kodZamok");

            test.prikaz("hovorit");
            assertTrue(test.vystup().contains("Tu nie je nikto, s kým by si mohol hovoriť"));

            otvorPrveDvere(test);
            test.prikaz("hovorit");
            test.prikaz("hovorit");

            String vystup = test.vystup();
            assertTrue(vystup.contains("Ten kód je " + kodZamku),
                    "Prvý rozhovor má prezradiť bezpečnostný kód.");
            assertTrue(vystup.contains("Ak si zabudol kód, je to: " + kodZamku),
                    "Pri opakovanom rozhovore musí byť možné kód znovu získať.");
            assertTrue(((Vedec) test.hodnotaPola("vedec")).isDialogDokonceny());
        }
    }

    @Test
    void servisnyPanelSaOtvaraAzPoDeaktivaciiZamku() throws Exception {
        try (TestovaciaHra test = new TestovaciaHra()) {
            otvorPrveDvere(test);

            test.prikaz("chod dekontaminacna komora");
            assertEquals(1, test.hrac().getMiestnost());
            assertTrue(test.vystup().contains("Dvere sú zamknuté bezpečnostným zámkom"));

            test.prikaz("pouzi skrutkovac na servisny panel");
            assertFalse(test.booleanPole("servisnyPanelOtvoreny"));
            assertTrue(test.vystup().contains("Panel je stále blokovaný bezpečnostným zámkom"));

            test.posliVstup(Integer.toString(test.intPole("kodZamok")));
            test.prikaz("kod");
            assertTrue(test.booleanPole("zamokDeaktivovany"));
            assertTrue(test.dvereSuOdomknute(1));

            test.prikaz("pouzi skrutkovac na servisny panel");
            assertTrue(test.booleanPole("servisnyPanelOtvoreny"));
            assertNotNull(test.miestnost(1).najdiPredmet(Predmet.KARTA));
            assertNotNull(test.miestnost(1).najdiPredmet(Predmet.VZORKA));
        }
    }

    @Test
    void vzorkaNefungujeMimoKomoryASpotrebujeSaPriOprave() throws Exception {
        try (TestovaciaHra test = new TestovaciaHra()) {
            otvorPrveDvere(test);
            test.posliVstup(Integer.toString(test.intPole("kodZamok")));
            test.prikaz("kod");
            test.prikaz("pouzi skrutkovac na servisny panel");
            test.prikaz("zober chemicka vzorka");

            test.prikaz("pouzi chemicka vzorka na chladiaci system");
            assertFalse(test.booleanPole("chladenieOpravene"));
            assertTrue(test.hrac().ma(Predmet.VZORKA));
            assertTrue(test.vystup().contains("V tejto miestnosti sa vzorka nehodí"));

            test.prikaz("chod dekontaminacna komora");
            test.prikaz("pouzi chemicka vzorka na chladiaci system");

            assertTrue(test.booleanPole("chladenieOpravene"));
            assertFalse(test.hrac().ma(Predmet.VZORKA),
                    "Po oprave chladiaceho systému sa má vzorka spotrebovať.");
        }
    }

    @Test
    void ulozenieANacitanieObnoviStavRozhovoruSVedcom() throws Exception {
        Path ulozenaHra = Paths.get("last_exit_save.properties").toAbsolutePath();
        boolean povodnySuborExistoval = Files.exists(ulozenaHra);
        byte[] povodnyObsah = povodnySuborExistoval ? Files.readAllBytes(ulozenaHra) : null;

        // Test používa skutočný súbor hry, ale po teste obnoví prípadný používateľský save.
        try {
            if (povodnySuborExistoval) {
                Files.delete(ulozenaHra);
            }

            try (TestovaciaHra test = new TestovaciaHra()) {
                otvorPrveDvere(test);
                int kodZamku = test.intPole("kodZamok");
                test.prikaz("hovorit");
                assertTrue(((Vedec) test.hodnotaPola("vedec")).isDialogDokonceny());

                test.posliVstup("pokracovat");
                test.prikaz("uloz");
                assertTrue(Files.exists(ulozenaHra), "Príkaz uloz má vytvoriť súbor s hrou.");

                assertEquals(Boolean.TRUE, test.zavolaj("nacitajHru", new Class<?>[0]),
                        "Platné uloženie sa má úspešne načítať.");
                assertEquals(1, test.hrac().getMiestnost());
                assertTrue(test.hrac().ma(Predmet.POZNAMKY));
                assertTrue(test.hrac().ma(Predmet.SKRUTKOVAC));
                assertTrue(test.dvereSuOdomknute(0));
                assertTrue(((Vedec) test.hodnotaPola("vedec")).isDialogDokonceny());
                assertEquals(kodZamku, test.intPole("kodZamok"));

                test.prikaz("hovorit");
                assertTrue(test.vystup().contains("Ak si zabudol kód, je to: " + kodZamku),
                        "Po načítaní uloženia musí vedec vedieť znovu zopakovať bezpečnostný kód.");
            }
        } finally {
            if (povodnySuborExistoval) {
                Files.write(ulozenaHra, povodnyObsah);
            } else {
                Files.deleteIfExists(ulozenaHra);
            }
        }
    }

    @Test
    void automatickyPriechodHrouVedieOdNovejHryKVitazstvu() throws Exception {
        Path ulozenaHra = Paths.get("last_exit_save.properties").toAbsolutePath();
        boolean povodnySuborExistoval = Files.exists(ulozenaHra);
        byte[] povodnyObsah = povodnySuborExistoval ? Files.readAllBytes(ulozenaHra) : null;

        // Víťazstvo v hre tento súbor štandardne maže. Test pred prípadným zásahom
        // zálohuje existujúci používateľský save a po teste ho obnoví.
        try {
            if (povodnySuborExistoval) {
                Files.delete(ulozenaHra);
            }

            try (TestovaciaHra test = new TestovaciaHra()) {
                int kodLaboratoria = test.intPole("kodLaboratorium");
                int kodZamku = test.intPole("kodZamok");
                int finalnyKod = test.intPole("kodFinalny");

                List<String> prikazy = new ArrayList<>(List.of(
                        "chod riadiace centrum",             // dvere sú najprv zamknuté
                        "zober poznamky",
                        "zober skrutkovac",
                        "kod",                               // bez prečítania poznámok sa kód nepýta
                        "precitaj",
                        "kod",
                        Integer.toString(kodLaboratoria),
                        "chod riadiace centrum",
                        "chod dekontaminacna komora",         // druhé dvere sú stále zamknuté
                        "pouzi skrutkovac na servisny panel", // panel blokuje zámok
                        "hovorit",                            // vedec poskytne bezpečnostný kód
                        "kod",
                        Integer.toString(kodZamku),
                        "pouzi skrutkovac na servisny panel",
                        "zober pristupova karta",
                        "zober chemicka vzorka",
                        "preskumaj terminal",                 // terminál ukáže finálny kód
                        "chod dekontaminacna komora",
                        "pouzi chemicka vzorka na chladiaci system",
                        "pouzi pristupova karta panel",        // overenie aj bez slova „na“
                        "preskumaj panel",
                        "kod",
                        Integer.toString(finalnyKod),
                        "koniec"                              // odpoveď na ponuku po víťazstve
                ));

                test.posliVstupAUzavri(prikazy);
                test.spustiHernyCyklus();

                String vystup = test.vystup();
                assertTrue(vystup.contains("Dvere sú zamknuté"),
                        "Priechod má overiť zamknuté dvere.");
                assertTrue(vystup.contains("BEZPEČNOSTNÝ ZÁMOK DEAKTIVOVANÝ"));
                assertTrue(vystup.contains("Záložný kód núdzového východu: " + finalnyKod));
                assertTrue(vystup.contains("PRÍSTUP OVERENÝ"));
                assertTrue(vystup.contains("[x] Chladiaci systém opravený"));
                assertTrue(vystup.contains("[x] Prístupová karta overená"));
                assertTrue(vystup.contains("[ ] Finálny kód zadaný"));
                assertTrue(vystup.contains("*** NÚDZOVÝ VÝCHOD SA OTVÁRA ***"));
                assertTrue(vystup.contains("VYHRAL SI!"));

                assertEquals(2, test.hrac().getMiestnost());
                assertTrue(test.booleanPole("chladenieOpravene"));
                assertTrue(test.booleanPole("kartaOverena"));
                assertTrue(test.booleanPole("finalnyKodOvereny"));
                assertTrue(test.booleanPole("servisnyPanelOtvoreny"));
                assertFalse(test.hrac().ma(Predmet.VZORKA));
            }
        } finally {
            if (povodnySuborExistoval) {
                Files.write(ulozenaHra, povodnyObsah);
            } else {
                Files.deleteIfExists(ulozenaHra);
            }
        }
    }

    private static void otvorPrveDvere(TestovaciaHra test) throws Exception {
        test.prikaz("zober poznamky");
        test.prikaz("zober skrutkovac");
        test.prikaz("precitaj");
        test.posliVstup(Integer.toString(test.intPole("kodLaboratorium")));
        test.prikaz("kod");
        test.prikaz("chod riadiace centrum");
        assertEquals(1, test.hrac().getMiestnost());
    }

    /**
     * Testovacie rozhranie nad skutočnou triedou Hra. Reflection sa používa len
     * na prípravu novej hry, prístup ku generovaným kódom/stavu a spustenie
     * súkromného herného cyklu; hráčske príkazy sa spracúvajú pôvodným kódom Hra.
     */
    private static final class TestovaciaHra implements AutoCloseable {
        private final java.io.InputStream povodnyVstup = System.in;
        private final PrintStream povodnyVystup = System.out;
        private final PipedInputStream vstupHry;
        private final PipedOutputStream vstupHraca;
        private final ByteArrayOutputStream zachytenyVystup = new ByteArrayOutputStream();
        private final PrintStream vystupTestu;
        private final Hra hra;

        private TestovaciaHra() throws Exception {
            vstupHry = new PipedInputStream(16 * 1024);
            vstupHraca = new PipedOutputStream(vstupHry);
            vystupTestu = new PrintStream(zachytenyVystup, true, StandardCharsets.UTF_8);
            System.setIn(vstupHry);
            System.setOut(vystupTestu);

            hra = new Hra();
            zavolaj("novaHra", new Class<?>[0]);
        }

        private void prikaz(String prikaz) throws Exception {
            zavolaj("spracuj", new Class<?>[]{String.class}, prikaz);
        }

        private void spustiHernyCyklus() throws Exception {
            zavolaj("spustiHernyCyklus", new Class<?>[0]);
        }

        private void posliVstup(String... riadky) throws IOException {
            String obsah = String.join("\n", riadky) + "\n";
            vstupHraca.write(obsah.getBytes(StandardCharsets.UTF_8));
            vstupHraca.flush();
        }

        private void posliVstupAUzavri(List<String> riadky) throws IOException {
            posliVstup(riadky.toArray(new String[0]));
            vstupHraca.close();
        }

        private String vystup() {
            vystupTestu.flush();
            return zachytenyVystup.toString(StandardCharsets.UTF_8);
        }

        private Hrac hrac() throws Exception {
            return (Hrac) hodnotaPola("hrac");
        }

        @SuppressWarnings("unchecked")
        private List<Miestnost> miestnosti() throws Exception {
            return (List<Miestnost>) hodnotaPola("miestnosti");
        }

        private Miestnost miestnost(int index) throws Exception {
            return miestnosti().get(index);
        }

        private int intPole(String nazov) throws Exception {
            Field pole = Hra.class.getDeclaredField(nazov);
            pole.setAccessible(true);
            return pole.getInt(hra);
        }

        private boolean booleanPole(String nazov) throws Exception {
            Field pole = Hra.class.getDeclaredField(nazov);
            pole.setAccessible(true);
            return pole.getBoolean(hra);
        }

        private boolean dvereSuOdomknute(int index) throws Exception {
            return ((boolean[]) hodnotaPola("dvereOdomknute"))[index];
        }

        private Object hodnotaPola(String nazov) throws Exception {
            Field pole = Hra.class.getDeclaredField(nazov);
            pole.setAccessible(true);
            return pole.get(hra);
        }

        private Object zavolaj(String nazov, Class<?>[] typy, Object... argumenty)
                throws Exception {
            Method metoda = Hra.class.getDeclaredMethod(nazov, typy);
            metoda.setAccessible(true);
            try {
                return metoda.invoke(hra, argumenty);
            } catch (InvocationTargetException e) {
                Throwable pricina = e.getCause();
                if (pricina instanceof Exception vynimka) {
                    throw vynimka;
                }
                if (pricina instanceof Error chyba) {
                    throw chyba;
                }
                throw new RuntimeException(pricina);
            }
        }

        @Override
        public void close() throws IOException {
            System.setIn(povodnyVstup);
            System.setOut(povodnyVystup);
            try {
                vstupHraca.close();
            } finally {
                vstupHry.close();
                vystupTestu.close();
            }
        }
    }
}