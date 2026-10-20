import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class UlozenieHryTest {

    @TempDir
    Path tempDir;

    @Test
    void vlastnostiSaSpravneZapisuDoSuboruANacitajú() throws IOException {
        Path subor = tempDir.resolve("test-save.properties");

        Properties povodneData = new Properties();
        povodneData.setProperty("player.room", "1");
        povodneData.setProperty("player.items", "Poznámky;Skrutkovač");
        povodneData.setProperty("cooling.repaired", "true");
        povodneData.setProperty("timer.remainingSeconds", "420");

        Hra.zapisVlastnosti(subor, povodneData);

        assertTrue(java.nio.file.Files.exists(subor));

        Properties nacitaneData = Hra.nacitajVlastnosti(subor);

        assertEquals("1", nacitaneData.getProperty("player.room"));
        assertEquals(
                "Poznámky;Skrutkovač",
                nacitaneData.getProperty("player.items")
        );
        assertEquals("true", nacitaneData.getProperty("cooling.repaired"));
        assertEquals("420", nacitaneData.getProperty("timer.remainingSeconds"));
    }

    @Test
    void ulozenieZachovaDiakritiku() throws IOException {
        Path subor = tempDir.resolve("diacritics.properties");

        Properties data = new Properties();
        data.setProperty("room.name", "Riadiace centrum");
        data.setProperty("item.name", "Prístupová karta");
        data.setProperty("message", "Hráč úspešne uložil hru.");

        Hra.zapisVlastnosti(subor, data);
        Properties nacitane = Hra.nacitajVlastnosti(subor);

        assertEquals("Riadiace centrum", nacitane.getProperty("room.name"));
        assertEquals("Prístupová karta", nacitane.getProperty("item.name"));
        assertEquals(
                "Hráč úspešne uložil hru.",
                nacitane.getProperty("message")
        );
    }

    @Test
    void nacitanieNeexistujucehoSuboruVyhodiIOException() {
        Path neexistujuciSubor = tempDir.resolve("neexistuje.properties");

        assertFalse(java.nio.file.Files.exists(neexistujuciSubor));

        assertThrows(
                IOException.class,
                () -> Hra.nacitajVlastnosti(neexistujuciSubor)
        );
    }
}
