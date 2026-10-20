import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MiestnostTest {

    @Test
    void novaMiestnostMaPrazdnyZoznamPredmetov() {
        Miestnost miestnost = new Miestnost(
                "Laboratórium",
                "Opis laboratória",
                "Dostupné príkazy"
        );

        assertTrue(miestnost.getPredmety().isEmpty());
    }

    @Test
    void pridanyPredmetJeMozneNajst() {
        Miestnost miestnost = new Miestnost(
                "Laboratórium", "Opis", "Možnosti"
        );
        Predmet skrutkovac = new Predmet(
                "Skrutkovač", "Nástroj na odskrutkovanie panelu"
        );

        miestnost.pridajPredmet(skrutkovac);

        assertSame(skrutkovac, miestnost.najdiPredmet("skrutkovač"));
    }

    @Test
    void neexistujuciPredmetVratiNull() {
        Miestnost miestnost = new Miestnost(
                "Laboratórium", "Opis", "Možnosti"
        );

        assertNull(miestnost.najdiPredmet("skrutkovač"));
    }

    @Test
    void odobratiePredmetuHoOdstraniZMiestnosti() {
        Miestnost miestnost = new Miestnost(
                "Laboratórium", "Opis", "Možnosti"
        );
        Predmet skrutkovac = new Predmet(
                "Skrutkovač", "Nástroj"
        );

        miestnost.pridajPredmet(skrutkovac);
        miestnost.odoberPredmet(skrutkovac);

        assertTrue(miestnost.getPredmety().isEmpty());
        assertNull(miestnost.najdiPredmet("skrutkovač"));
    }

    @Test
    void zmenaVratenehoZoznamuNezmeniMiestnost() {
        Miestnost miestnost = new Miestnost(
                "Laboratórium", "Opis", "Možnosti"
        );
        miestnost.pridajPredmet(
                new Predmet("Poznámky", "Poznámky z laboratória")
        );

        List<Predmet> predmety = miestnost.getPredmety();
        predmety.clear();

        assertEquals(1, miestnost.getPredmety().size());
        assertNotNull(miestnost.najdiPredmet("poznámky"));
    }
}