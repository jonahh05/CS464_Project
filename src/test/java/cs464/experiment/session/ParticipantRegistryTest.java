package cs464.experiment.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParticipantRegistryTest {

    @TempDir
    Path dataDir;

    private void session(String id, String stamp) throws Exception {
        for (String kind : List.of("demographics", "trials", "ratings")) {
            Files.writeString(dataDir.resolve(id + "_" + stamp + "_" + kind + ".csv"), "header\n");
        }
    }

    @Test
    void firstParticipantIsP01WhenTheFolderIsEmptyOrMissing() throws Exception {
        assertEquals("P01", new ParticipantRegistry(dataDir).nextId());
        assertEquals("P01", new ParticipantRegistry(dataDir.resolve("not-there")).nextId());
    }

    @Test
    void nextIdIsOneMoreThanTheHighestSavedNumber() throws Exception {
        session("P01", "20261119-100000");
        session("P02", "20261119-110000");
        session("1", "20261103-135628");   // older runs typed just "1"
        session("P02", "20261119-120000"); // a re-run of the same ID

        ParticipantRegistry registry = new ParticipantRegistry(dataDir);

        assertEquals(List.of(1, 2), List.copyOf(registry.usedNumbers()));
        assertEquals("P03", registry.nextId());
        assertEquals(4, registry.savedSessions());
    }

    @Test
    void startingThePilotArchivesTestDataAndRestartsNumbering() throws Exception {
        session("P01", "20261119-100000");
        session("P02", "20261119-110000");
        Files.writeString(dataDir.resolve(".gitkeep"), "");
        ParticipantRegistry registry = new ParticipantRegistry(dataDir);
        assertFalse(registry.status().pilot());

        Path archive = registry.startPilot(1);

        assertTrue(registry.status().pilot());
        assertEquals(0, registry.savedSessions());
        assertEquals("P01", registry.nextId());
        assertEquals(6, Files.list(archive).count(), "all six CSVs are kept in the archive");
        assertTrue(archive.getFileName().toString().startsWith("test-data-"));
        assertTrue(Files.exists(dataDir.resolve(".gitkeep")), ".gitkeep stays in place");
    }

    @Test
    void eachLaptopCanStartFromItsOwnFirstNumber() throws Exception {
        ParticipantRegistry registry = new ParticipantRegistry(dataDir);
        assertNull(registry.startPilot(4), "nothing to archive in an empty folder");

        assertEquals("P04", registry.nextId());
        session("P04", "20261120-100000");
        assertEquals("P05", registry.nextId());
        assertEquals(1, registry.savedSessions());
    }
}
