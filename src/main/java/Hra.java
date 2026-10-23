import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Hra {

    private static final Path SAVE_FILE = Paths.get("last_exit_save.properties");
    private static final int POCET_MIESTNOSTI = 3;

    private final Scanner scanner = new Scanner(System.in);
    private final Casovac casovac = new Casovac();
    private final Random random = new Random();

    private Hrac hrac;
    private Vedec vedec;
    private List<Miestnost> miestnosti;

    // dvereOdomknute[0] = dvere lab <-> riadiace centrum
    // dvereOdomknute[1] = dvere riadiace centrum <-> komora
    private boolean[] dvereOdomknute;

    // Tri kódy, v každej hre iné (generujú sa v novaHra)
    private int kodLaboratorium;
    private int kodZamok;
    private int kodFinalny;
    private boolean zamokDeaktivovany;
    private boolean servisnyPanelOtvoreny;
    private boolean chladenieOpravene;
    private boolean kartaOverena;
    private boolean finalnyKodOvereny;
    private boolean poznamkyPrecitane;
    private boolean hraBezi;
    private boolean programBezi = true;
    // Možnosť „Koniec“ v hlavnej ponuke sa zobrazí iba po vypršaní časového limitu.
    private boolean menuPoVyprsani = false;

    // ---------------------------------------------------------------
    // Príprava a reštart hry (všetko sa vráti do pôvodného stavu)
    // ---------------------------------------------------------------
    private void novaHra() {
        hrac = new Hrac();
        vedec = new Vedec("Dr. Elias Novak");
        dvereOdomknute = new boolean[POCET_MIESTNOSTI-1];
        zamokDeaktivovany = false;
        servisnyPanelOtvoreny = false;
        chladenieOpravene = false;
        kartaOverena = false;
        finalnyKodOvereny = false;
        poznamkyPrecitane = false;
        hraBezi = true;

        // Tri rôzne náhodné štvorciferné kódy
        kodLaboratorium = nahodnyKod();
        do {
            kodZamok = nahodnyKod();
        } while (kodZamok == kodLaboratorium);
        do {
            kodFinalny = nahodnyKod();
        } while (kodFinalny == kodLaboratorium || kodFinalny == kodZamok);

        Miestnost laboratorium = new Miestnost(
                "Experimentálne laboratórium",
                "Stojíš v zdemolovanom laboratóriu. Červené núdzové svetlá blikajú a sirény kvília.\n"
                        + "Pracovný stôl je plný vybavenia. Na stene je panel s číselným zámkom,\n"
                        + "ktorý blokuje dvere do riadiaceho centra.",
                "preskumaj stol, preskumaj panel, zober <predmet>, precitaj poznamky, kod, chod riadiace centrum");
        laboratorium.pridajPredmet(new Predmet(Predmet.POZNAMKY,
                "Pomačkané poznámky popísané tvojím rukopisom. Prečítaj si ich príkazom 'precitaj'."));
        laboratorium.pridajPredmet(new Predmet(Predmet.SKRUTKOVAC,
                "Malý krížový skrutkovač. Hodí sa na povolenie skrutiek."));

        Miestnost riadiaceCentrum = new Miestnost(
                "Riadiace centrum",
                "Monitory blikajú varovnými správami. Je tu počítačový terminál, bezpečnostný panel\n"
                        + "a servisný panel. Pri konzole stojí vedec Dr. Elias Novak.",
                "hovorit, preskumaj terminal / servisny panel / bezpecnostny panel / vedca, kod,\n"
                        + "pouzi skrutkovac na servisny panel, zober <predmet>, chod laboratorium / dekontaminacna komora");

        Miestnost komora = new Miestnost(
                "Dekontaminačná komora",
                "Vzduch je cítiť chemikáliami. Je tu poškodený núdzový chladiaci systém,\n"
                        + "bezpečnostný panel a zamknuté výstupné dvere.",
                "preskumaj chladiaci system / panel / dvere, pouzi vzorku na chladiaci system,\n"
                        + "pouzi kartu na panel, kod, chod riadiace centrum");

        miestnosti = new ArrayList<>();
        miestnosti.add(laboratorium);
        miestnosti.add(riadiaceCentrum);
        miestnosti.add(komora);

        casovac.restart();
    }

    // Náhodné štvorciferné číslo 1000 - 9999
    private int nahodnyKod() {
        return 1000 + random.nextInt(9000);
    }

    private void uvod() {
        System.out.println("\n==============================================");
        System.out.println("      ÚNIK Z PODZEMNÉHO LABORATÓRIA");
        System.out.println("==============================================");
        System.out.println("Chladiaci systém experimentu zlyhal! Bezpečnostný systém uzamkol komplex.");
        System.out.println("Máš 15 minút, kým experiment kriticky zlyhá.\n");
        System.out.println("ZOBUDIL SI SA V LABORATÓRIU. PRESKÚMAJ MOŽNOSTI.\n");
        mapa();
        System.out.println();
        napoveda();
    }

    private void uvodPoNacitani() {
        System.out.println("\n==============================================");
        System.out.println("      ÚNIK Z PODZEMNÉHO LABORATÓRIA");
        System.out.println("==============================================");
        System.out.println("Hra bola úspešne načítaná.");
        System.out.println("Pokračuješ tam, kde si skončil.");
        System.out.println("Zostávajúci čas: " + casovac.format() + "\n");

        mapa();
        System.out.println();

        vypisMiestnost();
        System.out.println();

        napoveda();
    }

    // ---------------------------------------------------------------
    // Hlavný cyklus hry
    // ---------------------------------------------------------------
    public void spusti() {
        while (programBezi) {
            boolean ulozenaHraExistuje = Files.exists(SAVE_FILE);

            // Pri prvom spustení bez uloženej hry sa hra začne priamo, bez ponuky.
            // Ponuka sa zobrazí, ak existuje uložená hra alebo vypršal časový limit.
            if (!ulozenaHraExistuje && !menuPoVyprsani) {
                novaHra();
                uvod();
                spustiHernyCyklus();
                continue;
            }

            System.out.println("\n==============================================");
            System.out.println("                 LAST EXIT");
            System.out.println("==============================================");
            System.out.println("Nová hra");

            if (ulozenaHraExistuje) {
                System.out.println("Načítať uloženú hru");
            }

            // Možnosť „Koniec“ sa ponúka iba po vypršaní časového limitu.
            if (menuPoVyprsani) {
                System.out.println("Koniec");
            }

            System.out.print("Vyber možnosť: ");
            String volba = Pomocnik.normalizuj(scanner.nextLine().trim())
                    .replaceAll("\\s+", " ");

            boolean novaHraVolba = volba.equals("nova hra")
                    || volba.equals("spusti novu hru")
                    || volba.equals("zacat novu hru")
                    || volba.equals("zacni novu hru");
            boolean nacitatVolba = volba.equals("nacitat ulozenu hru")
                    || volba.equals("nacitaj ulozenu hru");
            boolean koniecVolba = volba.equals("koniec") || volba.equals("konec");

            if (novaHraVolba) {
                if (ulozenaHraExistuje) {
                    System.out.println("Existujúca uložená hra bude vymazaná.");
                    System.out.print("Naozaj chceš začať novú hru? (ano/nie): ");
                    String potvrdenie = Pomocnik.normalizuj(scanner.nextLine().trim());
                    if (!potvrdenie.equals("ano")) {
                        System.out.println("Nová hra bola zrušená.");
                        continue;
                    }
                    try {
                        Files.deleteIfExists(SAVE_FILE);
                    } catch (IOException e) {
                        System.out.println("Starú uloženú hru sa nepodarilo vymazať: " + e.getMessage());
                        continue;
                    }
                }
                menuPoVyprsani = false;
                novaHra();
                uvod();
                spustiHernyCyklus();
            } else if (nacitatVolba && ulozenaHraExistuje) {
                if (nacitajHru()) {
                    menuPoVyprsani = false;
                    uvodPoNacitani();
                    spustiHernyCyklus();
                }
            } else if (menuPoVyprsani && koniecVolba) {
                programBezi = false;
            } else {
                System.out.println("Neplatná voľba. Skús to znova.");
            }
        }
        System.out.println("Program ukončený.");
    }

    private void spustiHernyCyklus() {
        while (hraBezi && programBezi) {
            System.out.print("\n[" + casovac.format() + "] > ");
            String riadok = scanner.nextLine().trim();

            if (casovac.uplynul()) {
                vybuch();
                continue;
            }
            spracuj(riadok);
        }
    }

    private void spracuj(String riadok) {
        if (riadok.isEmpty()) {
            return;
        }
        // Malé písmená, bez diakritiky a viacnásobné medzery zmeníme na jednu
        String upraveny = Pomocnik.normalizuj(riadok).replaceAll("\\s+", " ");

        // Rozdelíme vstup na príkaz a argument: "zober skrutkovac" -> "zober" + "skrutkovac"
        String[] casti = upraveny.split(" ", 2);
        String prikaz = casti[0];
        String argument = casti.length > 1 ? casti[1].trim() : "";

        switch (prikaz) {
            case "chod" -> chod(argument);
            case "napoveda" -> {
                if (bezArgumentu(argument, "napoveda")) napoveda();
            }
            case "zober" -> zober(argument);
            case "hovorit" -> {
                if (bezArgumentu(argument, "hovorit")) hovorit();
            }
            case "preskumaj" -> preskumaj(argument);
            case "precitaj" -> {
                if (argument.isEmpty() || argument.equals("poznamky")) {
                    precitaj();
                } else {
                    System.out.println("Použi 'precitaj' alebo 'precitaj poznamky'.");
                }
            }
            case "pouzi" -> pouzi(argument);
            case "kod" -> {
                if (bezArgumentu(argument, "kod")) kod();
            }
            case "mapa" -> {
                if (bezArgumentu(argument, "mapa")) mapa();
            }
            case "inventar" -> {
                if (bezArgumentu(argument, "inventar")) hrac.vypisInventar();
            }
            case "cas" -> {
                if (bezArgumentu(argument, "cas")) {
                    System.out.println("Do výbuchu zostáva: " + casovac.format());
                }
            }
            case "uloz" -> {
                if (bezArgumentu(argument, "uloz")) ulozHru();
            }
            case "koniec" -> {
                if (bezArgumentu(argument, "koniec")) {
                    System.out.println("Hra ukončená.");
                    hraBezi = false;
                    programBezi = false;
                }
            }
            default -> {
                if (prikaz.matches("\\d{4}")) {
                    System.out.println("Kód sa nezadáva priamo. Najprv napíš príkaz 'kod' pri zariadení, a až potom čísla.");
                } else {
                    System.out.println("Neznámy príkaz. Napíš 'napoveda'.");
                }
            }
        }
    }

    // Príkazy bez argumentov nesmú potichu ignorovať ďalšie slová.
    private boolean bezArgumentu(String argument, String prikaz) {
        if (!argument.isEmpty()) {
            System.out.println("Príkaz '" + prikaz + "' neprijíma ďalšie slová. Zadaj iba '" + prikaz + "'.");
            return false;
        }
        return true;
    }

    private Miestnost aktualna() {
        return miestnosti.get(hrac.getMiestnost());
    }

    // ---------------------------------------------------------------
    // Príkazy
    // ---------------------------------------------------------------
    private void napoveda() {
        System.out.println("DOSTUPNÉ PRÍKAZY:");
        System.out.println(" chod <miestnosť>        - presun do inej miestnosti");
        System.out.println(" zober <predmet>         - zoberie predmet z miestnosti");
        System.out.println(" hovorit                 - rozhovor s vedcom");
        System.out.println(" preskumaj [vec]         - preskúma miestnosť alebo vec");
        System.out.println(" precitaj                - prečíta poznámky");
        System.out.println(" pouzi <predmet> na <vec> - použije predmet");
        System.out.println(" kod                     - zadá kód na zariadení v miestnosti");
        System.out.println(" mapa                    - ukáže miestnosti a tvoju polohu");
        System.out.println(" inventar, cas, uloz, koniec, napoveda");
        System.out.println("\nV TEJTO MIESTNOSTI SKÚS: " + aktualna().getMoznosti());
    }

    private void mapa() {
        StringBuilder sb = new StringBuilder("MAPA: ");
        for (int i = 0; i < miestnosti.size(); i++) {
            if (i > 0) {
                sb.append(" - ");
            }
            sb.append(miestnosti.get(i).getNazov());
            if (i == hrac.getMiestnost()) {
                sb.append(" (aktuálna poloha)");
            }
        }
        System.out.println(sb);
    }

    private void chod(String cil) {
        // Ak hráč použije „chod do ...“, odstránime predložku „do“.
        cil = Pomocnik.normalizuj(cil).trim().replaceAll("\\s+", " ");
        if (cil.startsWith("do ")) {
            cil = cil.substring(3).trim();
        }

        // Podporíme aj prirodzené gramatické tvary názvov miestností.
        cil = switch (cil) {
            case "laboratoria" -> "laboratorium";
            case "riadiaceho centra" -> "riadiace centrum";
            case "dekontaminacnej komory" -> "dekontaminacna komora";
            default -> cil;
        };

        if (cil.isEmpty()) {
            System.out.println("Kam chceš ísť? Napíš napríklad: chod riadiace centrum");
            return;
        }
        if (cil.length() < 3) {
            System.out.println("Napíš aspoň 3 písmená názvu miestnosti, napríklad: chod riadiace centrum");
            return;
        }

        // Nájdeme všetky miestnosti, ktorých názov zodpovedá zadanému textu
        List<Integer> zhody = new ArrayList<>();
        for (int i = 0; i < miestnosti.size(); i++) {
            if (Pomocnik.zacinaSlovami(miestnosti.get(i).getNazov(), cil)) {
                zhody.add(i);
            }
        }

        if (zhody.isEmpty()) {
            System.out.println("Taká miestnosť neexistuje. Zoznam miestností ukáže príkaz 'mapa'.");
            return;
        }
        if (zhody.size() > 1) {
            System.out.println("Nie je jasné, ktorú miestnosť myslíš. Napíš jej názov presnejšie (pozri 'mapa').");
            return;
        }

        int cielIndex = zhody.get(0);
        int aktualnaIndex = hrac.getMiestnost();

        if (cielIndex == aktualnaIndex) {
            System.out.println("V tejto miestnosti už si.");
        } else if (Math.abs(cielIndex - aktualnaIndex) > 1) {
            System.out.println("Tam sa nedá ísť priamo. Najprv musíš prejsť cez susednú miestnosť.");
        } else {
            int dvere = Math.min(cielIndex, aktualnaIndex);
            if (!dvereOdomknute[dvere]) {
                if (dvere == 0) {
                    System.out.println("Dvere sú zamknuté. Na stene je panel s číselným zámkom (príkaz 'kod').");
                } else {
                    System.out.println("Dvere sú zamknuté bezpečnostným zámkom. Deaktivuj ho na termináli.");
                }
            } else {
                hrac.setMiestnost(cielIndex);
                System.out.println("Prechádzaš do miestnosti: " + aktualna().getNazov());
                vypisMiestnost();
                System.out.println("\nSkús: " + aktualna().getMoznosti());
                System.out.println("(Všetky príkazy ukáže 'napoveda'.)");
            }
        }
    }

    private void vypisMiestnost() {
        System.out.println("\n=== " + aktualna().getNazov().toUpperCase() + " ===");
        System.out.println(aktualna().getPopis());
        vypisPredmety();
    }

    private void vypisPredmety() {
        List<Predmet> predmety = aktualna().getPredmety();
        if (predmety.isEmpty()) {
            System.out.println("Nevidíš tu žiadne predmety, ktoré by sa dali zobrať.");
        } else {
            System.out.println("Predmety, ktoré môžeš zobrať:");
            predmety.forEach(p -> System.out.println(" - " + p.getNazov()));
        }
    }

    private void zober(String text) {
        if (text.isEmpty()) {
            System.out.println("Čo chceš zobrať? Napríklad: zober skrutkovac");
            return;
        }
        Predmet predmet = aktualna().najdiPredmet(text);
        if (predmet == null) {
            Predmet uMna = hrac.najdiPredmet(text);
            if (uMna != null) {
                System.out.println("Predmet '" + uMna.getNazov() + "' už máš v inventári.");
            } else {
                System.out.println("Taký predmet tu nie je.");
            }
            return;
        }
        aktualna().odoberPredmet(predmet);
        hrac.pridajPredmet(predmet);
        System.out.println("Zobral si: " + predmet.getNazov());
        if (predmet.getNazov().equals(Predmet.POZNAMKY)) {
            System.out.println("(Môžeš si ich prečítať príkazom 'precitaj'.)");
        }
    }

    private void precitaj() {
        if (!hrac.ma(Predmet.POZNAMKY)) {
            System.out.println("Nemáš čo čítať. Najprv musíš nájsť a zobrať poznámky.");
            return;
        }
        System.out.println("--- POZNÁMKY ---");
        System.out.println("* Experiment Z-7 drží pod kontrolou iba chladiaci systém v dekontaminačnej komore.");
        System.out.println("* Ak chladenie vypadne, bezpečnostný systém uzamkne celý komplex.");
        System.out.println("* Kód k dverám do riadiaceho centra som si sem zapísal, ale rozlial som naň kávu.");
        System.out.println("  Pamätám si len, že je to štvorciferné číslo, budem ho musieť hádať.");
        System.out.println("* Bezpečnostný zámok sa vypína na termináli v riadiacom centre.");
        System.out.println("  Servisný panel sa otvára skrutkovačom.");
        System.out.println("(Kód na paneli zadáš príkazom 'kod'.)");
        poznamkyPrecitane = true;
        System.out.println("Teraz skús zadať kód príkazom 'kod'.");
    }

    private void hovorit() {
        if (hrac.getMiestnost() != 1) {
            System.out.println("Tu nie je nikto, s kým by si mohol hovoriť.");
            return;
        }
        System.out.println(vedec.getMeno() + ":");
        System.out.println(vedec.hovor(kodZamok));
    }

    // ---------------------------------------------------------------
    // Preskúmanie vecí
    // ---------------------------------------------------------------
    private void preskumaj(String objekt) {
        if (objekt.isEmpty()) {
            vypisMiestnost();
            return;
        }

        // Najprv skúsime, či hráč nehľadá predmet (v miestnosti alebo v inventári)
        Predmet predmet = aktualna().najdiPredmet(objekt);
        if (predmet == null) {
            predmet = hrac.najdiPredmet(objekt);
        }
        if (predmet != null) {
            System.out.println(predmet.getPopis());
            return;
        }

        int izba = hrac.getMiestnost();
        if (izba == 0) {
            preskumajLaboratorium(objekt);
        } else if (izba == 1) {
            preskumajRiadiaceCentrum(objekt);
        } else {
            preskumajKomoru(objekt);
        }
    }

    private void preskumajLaboratorium(String objekt) {
        if (objekt.contains("stol") || objekt.contains("vybav")) {
            System.out.println("Pracovný stôl je zasypaný papiermi, skúmavkami a rozbitými prístrojmi.");
            vypisPredmety();
        } else if (objekt.contains("panel") || objekt.contains("zamok")) {
            if (dvereOdomknute[0]) {
                System.out.println("Panel svieti zeleno. Dvere do riadiaceho centra sú odomknuté.");
            } else {
                System.out.println("Panel s číselným zámkom svieti červeno. Vyžaduje štvorciferný kód (príkaz 'kod').");
            }
        } else {
            System.out.println("Nič zaujímavé si nenašiel.");
        }
    }

    private void preskumajRiadiaceCentrum(String objekt) {
        if (objekt.contains("terminal") || objekt.contains("pocitac")) {
            if (zamokDeaktivovany) {
                System.out.println("Terminál hlási: BEZPEČNOSTNÝ ZÁMOK DEAKTIVOVANÝ.");
                System.out.println("Záložný kód núdzového východu: " + kodFinalny);
            } else {
                System.out.println("Terminál žiada štvorciferný kód na deaktiváciu bezpečnostného zámku (príkaz 'kod').");
            }
        } else if (objekt.contains("servis")) {
            if (servisnyPanelOtvoreny) {
                System.out.println("Servisný panel je otvorený a prázdny.");
            } else if (!zamokDeaktivovany) {
                System.out.println("Servisný panel je blokovaný bezpečnostným zámkom. Najprv ho deaktivuj na termináli.");
            } else {
                System.out.println("Servisný panel je pripevnený skrutkami. Potreboval by si vhodný nástroj.");
            }
        } else if (objekt.contains("bezpec")) {
            System.out.println("Bezpečnostný panel bliká: KOMPLEX UZAMKNUTÝ. Do výbuchu zostáva " + casovac.format() + ".");
            if (zamokDeaktivovany) {
                System.out.println("Stav zámku: DEAKTIVOVANÝ.");
            }
        } else if (objekt.contains("panel")) {
            System.out.println("Ktorý panel myslíš? Napíš: preskumaj servisny panel, alebo: preskumaj bezpecnostny panel");
        } else if (objekt.contains("ved") || objekt.contains("novak") || objekt.contains("elias")) {
            System.out.println("Dr. Elias Novak vyzerá vystrašene, ale sústredene. Napíš 'hovorit', ak chceš s ním hovoriť.");
        } else {
            System.out.println("Nič zaujímavé si nenašiel.");
        }
    }

    private void preskumajKomoru(String objekt) {
        if (objekt.contains("chlad")) {
            if (chladenieOpravene) {
                System.out.println("Chladiaci systém beží. Teplota experimentu klesá.");
            } else {
                System.out.println("Chladiaci systém je poškodený, z potrubia syčí para. Na opravu treba chemickú vzorku.");
            }
        } else if (objekt.contains("panel")) {
            System.out.println("Bezpečnostný panel núdzového východu má čítačku kariet a klávesnicu.");
            System.out.println(" " + (chladenieOpravene ? "[x]" : "[ ]") + " Chladiaci systém opravený");
            System.out.println(" " + (kartaOverena ? "[x]" : "[ ]") + " Prístupová karta overená");
            System.out.println(" " + (finalnyKodOvereny ? "[x]" : "[ ]") + " Finálny kód zadaný");
        } else if (objekt.contains("dvere") || objekt.contains("vychod")) {
            System.out.println("Masívne výstupné dvere sú zamknuté. Otvoria sa po oprave chladenia,");
            System.out.println("overení karty a zadaní finálneho kódu.");
        } else {
            System.out.println("Nič zaujímavé si nenašiel.");
        }
    }

    // ---------------------------------------------------------------
    // Používanie predmetov
    // ---------------------------------------------------------------
    private void pouzi(String argument) {
        if (argument.isEmpty()) {
            System.out.println("Čo chceš použiť? Napríklad: pouzi skrutkovac na servisny panel");
            return;
        }

        Predmet predmet = null;
        String objekt = "";

        if (argument.contains(" na ")) {
            // "skrutkovac na servisny panel" -> predmet + objekt
            String[] casti = argument.split(" na ", 2);
            predmet = hrac.najdiPredmet(casti[0].trim());
            objekt = casti[1].trim();
        } else {
            // Hráč zabudol na slovo "na": "pristupova karta panel".
            // Skúšame od najdlhšej časti: čo je predmet a čo zvyšok (objekt).
            String[] slova = argument.split(" ");
            for (int pocet = slova.length; pocet >= 1 && predmet == null; pocet--) {
                String text = String.join(" ", Arrays.copyOfRange(slova, 0, pocet));
                predmet = hrac.najdiPredmet(text);
                if (predmet != null) {
                    objekt = String.join(" ", Arrays.copyOfRange(slova, pocet, slova.length));
                }
            }
        }

        if (predmet == null) {
            System.out.println("Taký predmet nemáš v inventári.");
            return;
        }
        if (objekt.isEmpty()) {
            System.out.println("Na čo ho chceš použiť? Napríklad: pouzi " + Pomocnik.normalizuj(predmet.getNazov()) + " na <vec>");
            return;
        }

        int izba = hrac.getMiestnost();
        switch (predmet.getNazov()) {
            case Predmet.SKRUTKOVAC -> pouziSkrutkovac(objekt, izba);
            case Predmet.KARTA -> pouziKartu(objekt, izba);
            case Predmet.VZORKA -> pouziVzorku(objekt, izba);
            default -> System.out.println("Tento predmet sa takto použiť nedá. Poznámky si prečítaš príkazom 'precitaj'.");
        }
    }

    private void pouziSkrutkovac(String objekt, int izba) {
        if (izba != 1) {
            System.out.println("V tejto miestnosti sa skrutkovač nehodí. Skús ho použiť v inej miestnosti.");
        } else if (objekt.equals("panel")) {
            System.out.println("Ktorý panel myslíš? Napíš napríklad: pouzi skrutkovac na servisny panel");
        } else if (!objekt.contains("servis")) {
            System.out.println("Skrutkovač sa na toto nehodí. Skús ho použiť na servisný panel.");
        } else if (servisnyPanelOtvoreny) {
            System.out.println("Servisný panel je už otvorený.");
        } else if (!zamokDeaktivovany) {
            System.out.println("Panel je stále blokovaný bezpečnostným zámkom. Najprv ho deaktivuj na termináli.");
        } else {
            servisnyPanelOtvoreny = true;
            miestnosti.get(1).pridajPredmet(vytvorPredmet(Predmet.KARTA));
            miestnosti.get(1).pridajPredmet(vytvorPredmet(Predmet.VZORKA));
            System.out.println("Odskrutkoval si servisný panel. Vo vnútri leží prístupová karta a chemická vzorka!");
            vypisPredmety();
        }
    }

    private void pouziKartu(String objekt, int izba) {
        if (izba != 2) {
            System.out.println("V tejto miestnosti sa karta nehodí. Skús ju použiť v inej miestnosti.");
        } else if (!(objekt.contains("panel") || objekt.contains("citack"))) {
            System.out.println("Karta sa na toto nehodí. Skús ju použiť na bezpečnostný panel.");
        } else if (kartaOverena) {
            System.out.println("Karta je už overená.");
        } else {
            kartaOverena = true;
            System.out.println("Čítačka pípla: PRÍSTUP OVERENÝ.");
            skontrolujUnik();
        }
    }

    private void pouziVzorku(String objekt, int izba) {
        if (izba != 2) {
            System.out.println("V tejto miestnosti sa vzorka nehodí. Skús ju použiť v inej miestnosti.");
        } else if (!objekt.contains("chlad")) {
            System.out.println("Vzorka sa na toto nehodí. Skús ju použiť na chladiaci systém.");
        } else {
            chladenieOpravene = true;
            Predmet vzorka = hrac.najdiPredmet(Predmet.VZORKA);
            if (vzorka != null) {
                hrac.odoberPredmet(vzorka);
            } // vzorka sa spotrebuje
            System.out.println("Vyliala si vzorku do chladiaceho okruhu. Systém sa rozbehol, chladenie je OPRAVENÉ.");
            System.out.println("(Chemická vzorka sa spotrebovala.)");
            skontrolujUnik();
        }
    }

    // ---------------------------------------------------------------
    // Kódy
    // ---------------------------------------------------------------
    private void kod() {
        int izba = hrac.getMiestnost();

        if (izba == 0) {
            if (dvereOdomknute[0]) {
                System.out.println("Zámok je už odomknutý.");
            } else if (!hrac.ma(Predmet.POZNAMKY)) {
                System.out.println("Nevieš, aký kód zadať. Možno ti pomôžu poznámky na stole.");
            } else if (!poznamkyPrecitane) {
                System.out.println("Najprv si prečítaj poznámky príkazom 'precitaj'.");
            } else if (zadajKod(kodLaboratorium, true)) {
                dvereOdomknute[0] = true;
                System.out.println("Zámok cvakol. Dvere do riadiaceho centra sú odomknuté!");
            }
        } else if (izba == 1) {
            if (zamokDeaktivovany) {
                System.out.println("Bezpečnostný zámok je už deaktivovaný.");
            } else if (zadajKod(kodZamok, false)) {
                zamokDeaktivovany = true;
                dvereOdomknute[1] = true;
                System.out.println("BEZPEČNOSTNÝ ZÁMOK DEAKTIVOVANÝ. Dvere do dekontaminačnej komory sa odomkli.");
                System.out.println("Servisný panel je už odblokovaný. Terminál zobrazuje aj záložný kód núdzového východu.");
            }
        } else {
            if (finalnyKodOvereny) {
                System.out.println("Finálny kód je už zadaný.");
            } else if (zadajKod(kodFinalny, false)) {
                finalnyKodOvereny = true;
                System.out.println("Panel pípol: FINÁLNY KÓD PRIJATÝ.");
                skontrolujUnik();
            }
        }
    }

    // Číta kódy od hráča, kým nezadá správny alebo nenapíše 'spat'.
    // Ak napoveda = true, hra vypisuje MENŠIE / VÄČŠIE.
    private boolean zadajKod(int spravny, boolean napoveda) {
        if (napoveda) {
            System.out.println("Kód nepoznáš presne, musíš ho hádať. Po každom tipe ti panel napíše,");
            System.out.println("či je hľadaný kód MENŠÍ alebo VÄČŠÍ ako tvoj tip.");
            System.out.println("(Kód je štvorciferný, od 1000 do 9999.)");
        }
        System.out.println("Zadaj štvorciferný kód (alebo napíš 'spat' pre zrušenie).");
        while (true) {
            System.out.print("[" + casovac.format() + "] KÓD > ");
            String vstup = scanner.nextLine().trim();

            if (casovac.uplynul()) {
                vybuch();
                return false;
            }
            if (Pomocnik.normalizuj(vstup).equals("spat")) {
                System.out.println("Zrušil si zadávanie kódu.");
                return false;
            }
            if (!vstup.matches("[0-9]{4}") || vstup.charAt(0) == '0') {
                System.out.println(
                        "Zadaj štvorciferné číslo od 1000 do 9999 (alebo 'spat' pre zrušenie)."
                );
                continue;
            }

            int tip = Integer.parseInt(vstup);

            if (tip == spravny) {
                return true;
            }
            if (napoveda) {
                if (tip > spravny) {
                    System.out.println("MENŠIE (hľadaný kód je menší ako tvoj tip)");
                } else {
                    System.out.println("VÄČŠIE (hľadaný kód je väčší ako tvoj tip)");
                }
            } else {
                System.out.println("Nesprávny kód.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Ukladanie a načítanie rozohranej hry
    // ---------------------------------------------------------------
    private void ulozHru() {
        casovac.pozastav();
        Properties data = new Properties();
        data.setProperty("player.room", Integer.toString(hrac.getMiestnost()));
        data.setProperty("player.items", spojNazvyPredmetov(hrac.getPredmety()));
        data.setProperty("timer.remainingSeconds", Long.toString(casovac.zostavaSekund()));
        data.setProperty("code.laboratory", Integer.toString(kodLaboratorium));
        data.setProperty("code.security", Integer.toString(kodZamok));
        data.setProperty("code.final", Integer.toString(kodFinalny));
        data.setProperty("door.0.open", Boolean.toString(dvereOdomknute[0]));
        data.setProperty("door.1.open", Boolean.toString(dvereOdomknute[1]));
        data.setProperty("lock.deactivated", Boolean.toString(zamokDeaktivovany));
        data.setProperty("servicePanel.open", Boolean.toString(servisnyPanelOtvoreny));
        data.setProperty("cooling.repaired", Boolean.toString(chladenieOpravene));
        data.setProperty("card.verified", Boolean.toString(kartaOverena));
        data.setProperty("finalCode.verified", Boolean.toString(finalnyKodOvereny));
        data.setProperty("notes.read", Boolean.toString(poznamkyPrecitane));
        data.setProperty("scientist.dialogFinished", Boolean.toString(vedec.isDialogDokonceny()));

        for (int i = 0; i < miestnosti.size(); i++) {
            data.setProperty("room." + i + ".items",
                    spojNazvyPredmetov(miestnosti.get(i).getPredmety()));
        }

        try {
            zapisVlastnosti(SAVE_FILE, data);

            System.out.println(
                    "Hra bola uložená do súboru '" + SAVE_FILE + "'."
            );
            System.out.println("Časovač je pozastavený. Zostávajúci čas: " + casovac.format());
            System.out.println("Napíš 'pokracovat', ak chceš pokračovať, alebo 'koniec', ak chceš hru ukončiť.");
            while (true) {
                System.out.print("> ");
                String odpoved = Pomocnik.normalizuj(scanner.nextLine().trim());
                if (odpoved.equals("pokracovat")) {
                    casovac.pokracuj();
                    System.out.println("Pokračuješ v hre.");
                    return;
                }
                if (odpoved.equals("koniec")) {
                    hraBezi = false;
                    programBezi = false;
                    System.out.println("Hra bola uložená. Program ukončený.");
                    return;
                }
                System.out.println("Napíš 'pokracovat' alebo 'koniec'.");
            }
        } catch (IOException e) {
            casovac.pokracuj();
            System.out.println("Hru sa nepodarilo uložiť: " + e.getMessage());
        }
    }

    private boolean nacitajHru() {
        if (!Files.exists(SAVE_FILE)) {
            System.out.println("Uložená hra sa nenašla. Najprv ulož hru príkazom 'uloz'.");
            return false;
        }

        Properties data;

        try {
            data = nacitajVlastnosti(SAVE_FILE);
        } catch (IOException e) {
            System.out.println(
                    "Uloženú hru sa nepodarilo načítať: " + e.getMessage()
            );
            return false;
        }

        try {
            int ulozenaMiestnost = Integer.parseInt(nacitajPovinnuHodnotu(data, "player.room"));

            if (ulozenaMiestnost < 0 || ulozenaMiestnost >= POCET_MIESTNOSTI) {
                throw new IllegalArgumentException("Neplatná miestnosť v uloženej hre.");
            }

            long zostavajuceSekundy = Long.parseLong(nacitajPovinnuHodnotu(data, "timer.remainingSeconds"));
            if (zostavajuceSekundy < 0 || zostavajuceSekundy > Casovac.getLimitSekundy()) {
                throw new IllegalArgumentException("Neplatný čas v uloženej hre.");
            }

            int ulozenyKodLaboratorium = Integer.parseInt(nacitajPovinnuHodnotu(data, "code.laboratory"));
            int ulozenyKodZamok = Integer.parseInt(nacitajPovinnuHodnotu(data, "code.security"));
            int ulozenyKodFinalny = Integer.parseInt(nacitajPovinnuHodnotu(data, "code.final"));
            if (ulozenyKodLaboratorium < 1000 || ulozenyKodLaboratorium > 9999
                    || ulozenyKodZamok < 1000 || ulozenyKodZamok > 9999
                    || ulozenyKodFinalny < 1000 || ulozenyKodFinalny > 9999) {
                throw new IllegalArgumentException(
                        "Neplatný kód v uloženej hre."
                );
            }

            if (ulozenyKodLaboratorium == ulozenyKodZamok
                    || ulozenyKodLaboratorium == ulozenyKodFinalny
                    || ulozenyKodZamok == ulozenyKodFinalny) {
                throw new IllegalArgumentException(
                        "Kódy v uloženej hre sa nesmú opakovať."
                );
            }
            boolean dvere0 = nacitajBoolean(data, "door.0.open");
            boolean dvere1 = nacitajBoolean(data, "door.1.open");
            boolean ulozenyZamok = nacitajBoolean(data, "lock.deactivated");
            boolean ulozenyServisnyPanel = nacitajBoolean(data, "servicePanel.open");
            boolean ulozeneChladenie = nacitajBoolean(data, "cooling.repaired");
            boolean ulozenaKarta = nacitajBoolean(data, "card.verified");
            boolean ulozenyFinalnyKod = nacitajBoolean(data, "finalCode.verified");
            boolean ulozenePoznamky = nacitajBoolean(data, "notes.read");
            boolean ulozenyDialog = nacitajBoolean(data, "scientist.dialogFinished");

            List<String> predmetyHraca = rozdelNazvy(nacitajPovinnuHodnotu(data, "player.items"));
            List<List<String>> predmetyMiestnosti = new ArrayList<>();
            for (int i = 0; i < POCET_MIESTNOSTI; i++) {
                predmetyMiestnosti.add(rozdelNazvy(data.getProperty("room." + i + ".items", "")));
            }

            // Overíme názvy všetkých predmetov ešte pred zmenou aktuálnej hry.
            predmetyHraca.forEach(this::vytvorPredmet);
            predmetyMiestnosti.forEach(zoznam -> zoznam.forEach(this::vytvorPredmet));

            novaHra();
            hrac.setMiestnost(ulozenaMiestnost);
            kodLaboratorium = ulozenyKodLaboratorium;
            kodZamok = ulozenyKodZamok;
            kodFinalny = ulozenyKodFinalny;
            dvereOdomknute[0] = dvere0;
            dvereOdomknute[1] = dvere1;
            zamokDeaktivovany = ulozenyZamok;
            servisnyPanelOtvoreny = ulozenyServisnyPanel;
            chladenieOpravene = ulozeneChladenie;
            kartaOverena = ulozenaKarta;
            finalnyKodOvereny = ulozenyFinalnyKod;
            poznamkyPrecitane = ulozenePoznamky;
            vedec.setDialogDokonceny(ulozenyDialog);
            obnovPredmety(predmetyHraca, predmetyMiestnosti);
            casovac.nastavZostavajuceSekundy(zostavajuceSekundy);
            hraBezi = true;

            return true;

        } catch (IllegalArgumentException | NullPointerException e) {
            System.out.println("Uložený súbor má neplatný obsah. Hru sa nepodarilo načítať.");
            return false;
        }
    }

    static void zapisVlastnosti(Path cesta, Properties data)
            throws IOException {
        try (OutputStream vystup = Files.newOutputStream(cesta)) {
            data.store(vystup, "Ulozena hra Last Exit");
        }
    }

    static Properties nacitajVlastnosti(Path cesta)
            throws IOException {
        Properties data = new Properties();

        try (InputStream vstup = Files.newInputStream(cesta)) {
            data.load(vstup);
        }

        return data;
    }

    private String nacitajPovinnuHodnotu(Properties data, String kluc) {
        String hodnota = data.getProperty(kluc);
        if (hodnota == null) {
            throw new IllegalArgumentException("Chýba údaj: " + kluc);
        }
        return hodnota;
    }

    private boolean nacitajBoolean(Properties data, String kluc) {
        String hodnota = nacitajPovinnuHodnotu(data, kluc);
        if (!hodnota.equalsIgnoreCase("true") && !hodnota.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("Neplatná hodnota: " + kluc);
        }
        return Boolean.parseBoolean(hodnota);
    }

    private String spojNazvyPredmetov(List<Predmet> predmety) {
        List<String> nazvy = new ArrayList<>();
        for (Predmet predmet : predmety) {
            nazvy.add(predmet.getNazov());
        }
        return String.join("|", nazvy);
    }

    private List<String> rozdelNazvy(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(text.split("\\|")));
    }

    private void obnovPredmety(List<String> predmetyHraca, List<List<String>> predmetyMiestnosti) {
        for (Predmet predmet : hrac.getPredmety()) {
            hrac.odoberPredmet(predmet);
        }
        for (Miestnost miestnost : miestnosti) {
            for (Predmet predmet : miestnost.getPredmety()) {
                miestnost.odoberPredmet(predmet);
            }
        }
        for (String nazov : predmetyHraca) {
            hrac.pridajPredmet(vytvorPredmet(nazov));
        }
        for (int i = 0; i < miestnosti.size(); i++) {
            for (String nazov : predmetyMiestnosti.get(i)) {
                miestnosti.get(i).pridajPredmet(vytvorPredmet(nazov));
            }
        }
    }

    private Predmet vytvorPredmet(String nazov) {
        return switch (nazov) {
            case Predmet.POZNAMKY -> new Predmet(Predmet.POZNAMKY,
                    "Pomačkané poznámky popísané tvojím rukopisom. Prečítaj si ich príkazom 'precitaj'.");
            case Predmet.SKRUTKOVAC -> new Predmet(Predmet.SKRUTKOVAC,
                    "Malý krížový skrutkovač. Hodí sa na povolenie skrutiek.");
            case Predmet.KARTA -> new Predmet(Predmet.KARTA,
                    "Biela prístupová karta s čipom a nápisom LEVEL 3.");
            case Predmet.VZORKA -> new Predmet(Predmet.VZORKA,
                    "Skúmavka so žiarivo zelenou kvapalinou. Stabilizátor chladiva.");
            default -> throw new IllegalArgumentException("Neznámy predmet: " + nazov);
        };
    }

    // ---------------------------------------------------------------
    // Koniec hry
    // ---------------------------------------------------------------
    private void skontrolujUnik() {
        List<String> chyba = new ArrayList<>();
        if (!chladenieOpravene) {
            chyba.add("opraviť chladiaci systém");
        }
        if (!kartaOverena) {
            chyba.add("overiť prístupovú kartu");
        }
        if (!finalnyKodOvereny) {
            chyba.add("zadať finálny kód");
        }

        if (chyba.isEmpty()) {
            System.out.println("\n*** NÚDZOVÝ VÝCHOD SA OTVÁRA ***");
            System.out.println("Vybehol si von tesne predtým, než experiment zlyhal. VYHRAL SI!");
            System.out.println("Zostávajúci čas: " + casovac.format());

            try {
                Files.deleteIfExists(SAVE_FILE);
            } catch (IOException e) {
                System.out.println(
                        "Upozornenie: Starú uloženú hru sa nepodarilo vymazať: "
                                + e.getMessage()
                );
            }

            ponukniRestart();
        } else {
            System.out.println("Núdzový východ sa ešte neotvoril. Zostáva: " + String.join(", ", chyba) + ".");
        }
    }

    private void vybuch() {
        System.out.println("\n*** BUM! ***");
        System.out.println("Čas vypršal. Experiment kriticky zlyhal a celý komplex vybuchol.");
        System.out.println("Po skončení časového limitu sa zobrazí hlavná ponuka.");
        hraBezi = false;
        menuPoVyprsani = true;
    }

    private void ponukniRestart() {
        System.out.println("\nChceš to skúsiť znova? Napíš 'pokracovat' pre novú hru alebo 'koniec' pre ukončenie.");
        while (true) {
            System.out.print("> ");
            String odpoved = Pomocnik.normalizuj(scanner.nextLine().trim());
            if (odpoved.equals("pokracovat")) {
                novaHra(); // nové kódy, nový čas, všetko od začiatku
                uvod();
                return;
            }
            if (odpoved.equals("koniec")) {
                System.out.println("Hra ukončená.");
                hraBezi = false;
                programBezi = false;
                return;
            }
            System.out.println("Napíš 'pokracovat' alebo 'koniec'.");
        }
    }
}
