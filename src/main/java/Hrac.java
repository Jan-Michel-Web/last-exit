import java.util.ArrayList;
import java.util.List;

public class Hrac {

    private final List<Predmet> predmety = new ArrayList<>();
    private int miestnost = 0; // 0 = laboratórium, 1 = riadiace centrum, 2 = komora

    public int getMiestnost() {
        return miestnost;
    }

    public void setMiestnost(int miestnost) {
        this.miestnost = miestnost;
    }

    public List<Predmet> getPredmety() {
        return new ArrayList<>(predmety);
    }

    public void pridajPredmet(Predmet predmet) {
        predmety.add(predmet);
    }

    public void odoberPredmet(Predmet predmet) {
        predmety.remove(predmet);
    }

    // Má hráč predmet s presne týmto názvom?
    public boolean ma(String nazov) {
        return predmety.stream().anyMatch(p -> p.getNazov().equals(nazov));
    }

    public Predmet najdiPredmet(String text) {
        return predmety.stream()
                .filter(p -> Pomocnik.obsahuje(p.getNazov(), text))
                .findFirst()
                .orElse(null);
    }

    public void vypisInventar() {
        if (predmety.isEmpty()) {
            System.out.println("Inventár je prázdny.");
        } else {
            System.out.println("Inventár:");
            predmety.forEach(p -> System.out.println(" - " + p.getNazov()));
        }
    }
}
