import java.util.ArrayList;
import java.util.List;

public class Miestnost extends HernyObjekt {

    private final String moznosti; // text pre príkaz napoveda
    private final List<Predmet> predmety = new ArrayList<>();

    public Miestnost(String nazov, String popis, String moznosti) {
        super(nazov, popis);
        this.moznosti = moznosti;
    }

    public String getMoznosti() {
        return moznosti;
    }

    // Vraciame kópiu zoznamu, aby ho nikto zvonku nemohol meniť (zapuzdrenie).
    public List<Predmet> getPredmety() {
        return new ArrayList<>(predmety);
    }

    public void pridajPredmet(Predmet predmet) {
        predmety.add(predmet);
    }

    public void odoberPredmet(Predmet predmet) {
        predmety.remove(predmet);
    }

    // Stream + lambda: nájde prvý predmet, ktorého názov obsahuje hľadaný text.
    public Predmet najdiPredmet(String text) {
        return predmety.stream()
                .filter(p -> Pomocnik.obsahuje(p.getNazov(), text))
                .findFirst()
                .orElse(null);
    }
}
