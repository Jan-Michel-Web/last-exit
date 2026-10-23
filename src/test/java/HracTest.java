import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HracTest {

    @Test
    void novyHracMaPrazdnyInventar() {
        Hrac hrac = new Hrac();

        assertTrue(hrac.getPredmety().isEmpty());
        assertFalse(hrac.ma(Predmet.POZNAMKY));
    }

    @Test
    void pridanyPredmetJeMozneNajst() {
        Hrac hrac = new Hrac();
        Predmet poznamky = new Predmet(
                Predmet.POZNAMKY,
                "Poznámky z laboratória"
        );

        hrac.pridajPredmet(poznamky);

        assertTrue(hrac.ma(Predmet.POZNAMKY));
        assertSame(poznamky, hrac.najdiPredmet("poznam"));
    }

    @Test
    void odobratiePredmetuHoOdstraniZInventara() {
        Hrac hrac = new Hrac();
        hrac.pridajPredmet(
                new Predmet(Predmet.POZNAMKY, "Poznámky")
        );

        Predmet poznamky = hrac.najdiPredmet(Predmet.POZNAMKY);
        hrac.odoberPredmet(poznamky);

        assertFalse(hrac.ma(Predmet.POZNAMKY));
        assertNull(hrac.najdiPredmet("poznam"));
    }

    @Test
    void zmenaVratenehoZoznamuNezmeniInventarHraca() {
        Hrac hrac = new Hrac();
        hrac.pridajPredmet(
                new Predmet(Predmet.POZNAMKY, "Poznámky")
        );

        List<Predmet> inventar = hrac.getPredmety();
        inventar.clear();

        assertTrue(hrac.ma(Predmet.POZNAMKY));
    }
}