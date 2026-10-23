import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VedecTest {

    @Test
    void prvyRozhovorPrezradiBezpecnostnyKodAOznaciDialogAkoDokonceny() {
        Vedec vedec = new Vedec("Dr. Elias Novak");

        String odpoved = vedec.hovor(4821);

        assertTrue(odpoved.contains("4821"));
        assertTrue(vedec.isDialogDokonceny());
    }

    @Test
    void opakovanyRozhovorUmoznujeZnovuZiskatKod() {
        Vedec vedec = new Vedec("Dr. Elias Novak");
        vedec.hovor(4821);

        String odpoved = vedec.hovor(4821);

        assertTrue(odpoved.contains("4821"));
        assertTrue(odpoved.contains("Ak si zabudol kód"));
    }

    @Test
    void obnoveniStavuRozhovoruNevymazeMoznostZopakovatKod() {
        Vedec vedec = new Vedec("Dr. Elias Novak");
        vedec.setDialogDokonceny(true);

        String odpoved = vedec.hovor(7394);

        assertTrue(odpoved.contains("7394"));
        assertTrue(vedec.isDialogDokonceny());
    }
}
