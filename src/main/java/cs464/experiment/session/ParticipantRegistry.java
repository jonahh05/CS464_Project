package cs464.experiment.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Keeps track of participant numbers on this laptop, using the CSV files already in the data folder.
 *
 * <p>The next participant ID is one more than the highest number already saved (P01 if none).
 * Before the pilot study starts, {@link #startPilot(int)} moves the practice data into
 * {@code data/archive/} and records the study status in {@code data/study_status.properties},
 * so real participants start again from the chosen first number.
 */
public final class ParticipantRegistry {

    public static final String STATUS_FILE = "study_status.properties";
    public static final String ARCHIVE_DIR = "archive";

    /** {@code <id>_<yyyyMMdd-HHmmss>_<kind>.csv}, the names {@code ResultWriter} uses. */
    private static final Pattern SESSION_FILE =
            Pattern.compile("^(.+)_(\\d{8}-\\d{6})_(trials|ratings|demographics)\\.csv$");
    private static final DateTimeFormatter ARCHIVE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /** Whether this laptop is still practicing or running the real pilot study. */
    public record Status(boolean pilot, LocalDate pilotStarted, int firstNumber) {
        static Status testing() {
            return new Status(false, null, 1);
        }
    }

    private final Path dataDir;

    public ParticipantRegistry(Path dataDir) {
        this.dataDir = dataDir;
    }

    public Path dataDir() {
        return dataDir;
    }

    public static String formatId(int number) {
        return String.format("P%02d", number);
    }

    public Status status() throws IOException {
        Path file = dataDir.resolve(STATUS_FILE);
        if (!Files.exists(file)) {
            return Status.testing();
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        }
        if (!"pilot".equals(props.getProperty("mode"))) {
            return Status.testing();
        }
        LocalDate started = props.containsKey("started") ? LocalDate.parse(props.getProperty("started")) : null;
        int first = Integer.parseInt(props.getProperty("first_number", "1"));
        return new Status(true, started, first);
    }

    /** Sessions saved in the data folder (one per trials file), not counting the archive. */
    public int savedSessions() throws IOException {
        return (int) sessionFiles().stream()
                .filter(name -> name.endsWith("_trials.csv"))
                .count();
    }

    /** The participant numbers that already have data, read from the file names. */
    public TreeSet<Integer> usedNumbers() throws IOException {
        TreeSet<Integer> numbers = new TreeSet<>();
        for (String name : sessionFiles()) {
            Matcher m = SESSION_FILE.matcher(name);
            if (m.matches()) {
                Integer number = numberIn(m.group(1));
                if (number != null) {
                    numbers.add(number);
                }
            }
        }
        return numbers;
    }

    /**
     * True if this ID (or another ID with the same number, like "2" and "P02") already has a saved
     * session in the data folder. Checked again right before a session starts.
     */
    public boolean hasData(String participantId) throws IOException {
        Integer number = numberIn(participantId);
        if (number != null) {
            return usedNumbers().contains(number);
        }
        String safe = participantId.replaceAll("[^A-Za-z0-9_-]", "_");
        for (String name : sessionFiles()) {
            Matcher m = SESSION_FILE.matcher(name);
            if (m.matches() && m.group(1).equalsIgnoreCase(safe)) {
                return true;
            }
        }
        return false;
    }

    /** One more than the highest saved number, but never below the pilot's first number. */
    public int nextNumber() throws IOException {
        int first = status().firstNumber();
        TreeSet<Integer> used = usedNumbers();
        int afterLast = used.isEmpty() ? first : used.last() + 1;
        return Math.max(first, afterLast);
    }

    public String nextId() throws IOException {
        return formatId(nextNumber());
    }

    /**
     * Moves every saved session into {@code data/archive/<label>-<timestamp>/} (nothing is deleted)
     * and marks this laptop as running the pilot study, numbering from {@code firstNumber}.
     *
     * @return the archive folder, or null if there was nothing to move
     */
    public Path startPilot(int firstNumber) throws IOException {
        if (firstNumber < 1) {
            throw new IllegalArgumentException("The first participant number must be 1 or more");
        }
        Files.createDirectories(dataDir);
        String label = status().pilot() ? "pilot-data" : "test-data";

        List<String> files = sessionFiles();
        Path archive = null;
        if (!files.isEmpty()) {
            archive = dataDir.resolve(ARCHIVE_DIR).resolve(label + "-" + LocalDateTime.now().format(ARCHIVE_STAMP));
            Files.createDirectories(archive);
            for (String name : files) {
                Files.move(dataDir.resolve(name), archive.resolve(name));
            }
        }

        Properties props = new Properties();
        props.setProperty("mode", "pilot");
        props.setProperty("started", LocalDate.now().toString());
        props.setProperty("first_number", String.valueOf(firstNumber));
        try (OutputStream out = Files.newOutputStream(dataDir.resolve(STATUS_FILE))) {
            props.store(out, "Written by the Start pilot study button on the setup screen");
        }
        return archive;
    }

    /** CSV file names directly in the data folder (not in the archive). */
    private List<String> sessionFiles() throws IOException {
        List<String> names = new ArrayList<>();
        if (!Files.isDirectory(dataDir)) {
            return names;
        }
        try (Stream<Path> files = Files.list(dataDir)) {
            files.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(name -> name.endsWith(".csv"))
                    .sorted()
                    .forEach(names::add);
        }
        return names;
    }

    /** The number in an ID like P07, p7 or 07; null if there is none. */
    static Integer numberIn(String id) {
        String digits = id.replaceAll("\\D", "");
        if (digits.isEmpty() || digits.length() > 6) {
            return null;
        }
        return Integer.parseInt(digits);
    }
}
