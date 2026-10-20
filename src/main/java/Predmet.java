public class Predmet extends HernyObjekt {

    // Názvy predmetov na jednom mieste, aby sme sa nepomýlili v texte.
    public static final String POZNAMKY = "Poznámky";
    public static final String SKRUTKOVAC = "Skrutkovač";
    public static final String KARTA = "Prístupová karta";
    public static final String VZORKA = "Chemická vzorka";

    public Predmet(String nazov, String popis) {
        super(nazov, popis);
    }
}
