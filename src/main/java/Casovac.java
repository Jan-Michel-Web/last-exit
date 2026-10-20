public class Casovac {

    private static final long LIMIT_MS = 15 * 60 * 1000; // 15 minút v milisekundách.
    private long start;
    private long zostavaPriPauze;
    private boolean pozastaveny;

    public Casovac() {
        restart();
    }

    public void restart() {
        start = System.currentTimeMillis();
        zostavaPriPauze = LIMIT_MS;
        pozastaveny = false;
    }

    public boolean uplynul() {
        return !pozastaveny && System.currentTimeMillis() - start >= LIMIT_MS;
    }

    public long zostavaSekund() {
        if (pozastaveny) {
            return Math.max(zostavaPriPauze, 0) / 1000;
        }
        long zostava = LIMIT_MS - (System.currentTimeMillis() - start);
        return Math.max(zostava, 0) / 1000;
    }

    // Pozastaví časovač, napríklad počas menu po uložení hry.
    public void pozastav() {
        if (!pozastaveny) {
            zostavaPriPauze = Math.max(0, LIMIT_MS - (System.currentTimeMillis() - start));
            pozastaveny = true;
        }
    }

    // Pokračuje v odpočítavaní od času, ktorý zostával pri pauze.
    public void pokracuj() {
        if (pozastaveny) {
            start = System.currentTimeMillis() - (LIMIT_MS - zostavaPriPauze);
            pozastaveny = false;
        }
    }

    public void nastavZostavajuceSekundy(long sekundy) {
        long obmedzeneSekundy = Math.max(0, Math.min(getLimitSekundy(), sekundy));
        zostavaPriPauze = obmedzeneSekundy * 1000;
        start = System.currentTimeMillis() - (LIMIT_MS - zostavaPriPauze);
        pozastaveny = false;
    }

    public static long getLimitSekundy() {
        return LIMIT_MS / 1000;
    }

    // Napr. "09:41".
    public String format() {
        long s = zostavaSekund();
        return String.format("%02d:%02d", s / 60, s % 60);
    }
}
