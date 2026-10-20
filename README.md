# Last Exit

Konzolová hra v Jave 17+ s Mavenom a JUnit 5.

## Spustenie v IntelliJ IDEA
1. Otvor priečinok projektu, v ktorom sa nachádza `pom.xml`.
2. Nechaj IntelliJ načítať Maven projekt.
3. Spusti `Main.java`.
4. Ak neexistuje uložená hra, hra sa spustí priamo bez hlavnej ponuky.
5. Ak uložená hra existuje, zobrazí sa textová ponuka novej hry alebo načítania uloženia. Možnosti sa zadávajú názvom, nie číslom.
6. Po vypršaní 15-minútového limitu sa zobrazí ponuka, ktorá obsahuje aj možnosť `Koniec`.

## Maven príkazy
- `mvn test` – spustí unit testy.
- `mvn package` – zostaví projekt.

## Príkazy
- Príkazy bez argumentov (`mapa`, `napoveda`, `inventar`, `cas`, `kod`, `hovorit`, `uloz`, `koniec`) odmietnu dodatočné slová namiesto toho, aby ich potichu ignorovali.
- Poznámky možno prečítať príkazom `precitaj` alebo `precitaj poznamky`.
- Príkazy s argumentmi, napríklad `chod`, `zober`, `preskumaj` a `pouzi`, naďalej prijímajú cieľ alebo predmet.

## Ukladanie hry
- Počas hry zadaj `uloz`.
- Časovač sa pozastaví a hra vypíše zostávajúci čas.
- Zadaj `pokracovat`, aby si pokračoval, alebo `koniec`, aby si hru uložil a program ukončil.
- Pri ďalšom spustení sa zobrazí ponuka načítania iba vtedy, keď uložený súbor existuje.
- Načítanie je dostupné z tejto ponuky, nie ako príkaz počas hrania.
- Ak vyberieš novú hru a uložená hra existuje, program sa najprv opýta, či ju má vymazať.

Uloženie sa vytvára ako `last_exit_save.properties` v pracovnom priečinku programu. Tento súbor nepatrí do Git repozitára.
