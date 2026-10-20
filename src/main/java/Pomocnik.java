import java.text.Normalizer;
import java.util.Locale;

public class Pomocnik {

    // "Skrutkovač" -> "skrutkovac" (malé písmená, bez diakritiky).
    public static String normalizuj(String text) {
        String rozlozeny = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return rozlozeny.replaceAll("\\p{M}", "");
    }

    // Pre predmety: názov obsahuje hľadaný text.
    public static boolean obsahuje(String nazov, String hladane) {
        String n = normalizuj(nazov);
        String h = normalizuj(hladane).trim();

        if (h.length() < 3) {
            return false;
        }
        if (n.contains(h)) {
            return true;
        }
        return zacinaSlovami(nazov, hladane);
    }

    // Pre miestnosti: každé hľadané slovo musí byť začiatkom (alebo podobou) slova v názve.
    public static boolean zacinaSlovami(String nazov, String hladane) {
        String[] slovaNazvu = normalizuj(nazov).split("\\s+");
        String h = normalizuj(hladane).trim();

        if (h.isEmpty()) {
            return false;
        }
        for (String slovo : h.split("\\s+")) {
            boolean nasiel = false;
            for (String s : slovaNazvu) {
                if (s.startsWith(slovo) || podobne(slovo, s)) {
                    nasiel = true;
                    break;
                }
            }
            if (!nasiel) {
                return false;
            }
        }
        return true;
    }

    // Slová sú podobné, ak sa líšia len koncovkou alebo jedným preklepom.
    private static boolean podobne(String a, String b) {
        if (a.equals(b)) {
            return true;
        }
        int min = Math.min(a.length(), b.length());
        if (min < 4) {
            return false;
        }
        int i = 0;
        while (i < min && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        if (i >= min - 1) {
            return true;
        }
        return min >= 5 && vzdialenost(a, b) <= 1;
    }

    // Levenshteinova vzdialenosť: počet vložení, zmazaní alebo zámen písmen.
    private static int vzdialenost(String a, String b) {
        int[][] d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            d[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            d[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cena = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1),
                        d[i - 1][j - 1] + cena);
            }
        }
        return d[a.length()][b.length()];
    }
}
