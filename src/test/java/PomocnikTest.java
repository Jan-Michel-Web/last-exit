import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PomocnikTest {

    @Test
    void normalizujOdstraniDiakritikuAMalaPismena() {
        assertEquals("skrutkovac", Pomocnik.normalizuj("Skrutkovač"));
    }

    @Test
    void obsahujeNajdePredmetBezDiakritiky() {
        assertTrue(Pomocnik.obsahuje("Prístupová karta", "pristupova"));
    }

    @Test
    void obsahujeOdmietnePrilisKratkyText() {
        assertFalse(Pomocnik.obsahuje("Skrutkovač", "sk"));
    }

    @Test
    void zacinaSlovamiNajdeMiestnostPodlaCastiNazvu() {
        assertTrue(Pomocnik.zacinaSlovami("Riadiace centrum", "riadice cent"));
    }

    @Test
    void nespravnyNazovMiestnostiSaNemaNajst() {
        assertFalse(Pomocnik.zacinaSlovami("Riadiace centrum", "sklad"));
    }
}
