/**
 * Spoločný základ pre objekty herného sveta, ktoré majú názov a opis.
 * Miestnosť a predmet tieto vlastnosti dedia.
 */
public abstract class HernyObjekt {
    private final String nazov;
    private final String popis;

    protected HernyObjekt(String nazov, String popis) {
        this.nazov = nazov;
        this.popis = popis;
    }

    public String getNazov() {
        return nazov;
    }

    public String getPopis() {
        return popis;
    }
}
