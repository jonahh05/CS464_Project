package cs464.experiment.io;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import cs464.experiment.model.ExperimentConfig;
import cs464.experiment.model.Question;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the two JSON files bundled under src/main/resources.
 * JSON keys are snake_case ("correct_index"); Java fields are camelCase ("correctIndex").
 */
public final class JsonLoader {

    public static final String QUESTIONS_RESOURCE = "/questions/questions.json";
    public static final String CONFIG_RESOURCE = "/config/experiment.json";

    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private JsonLoader() {
    }

    public static ExperimentConfig loadConfig(String resourcePath) throws IOException {
        try (Reader reader = open(resourcePath)) {
            ExperimentConfig config = GSON.fromJson(reader, ExperimentConfig.class);
            if (config == null) {
                throw new IOException(resourcePath + " is empty");
            }
            return config;
        } catch (JsonParseException e) {
            throw new IOException("Could not parse " + resourcePath + ": " + e.getMessage(), e);
        }
    }

    /** @return questions keyed by id, in file order */
    public static Map<String, Question> loadQuestions(String resourcePath) throws IOException {
        Question[] questions;
        try (Reader reader = open(resourcePath)) {
            questions = GSON.fromJson(reader, Question[].class);
        } catch (JsonParseException e) {
            throw new IOException("Could not parse " + resourcePath + ": " + e.getMessage(), e);
        }
        if (questions == null) {
            throw new IOException(resourcePath + " is empty");
        }
        return indexById(Arrays.asList(questions));
    }

    static Map<String, Question> indexById(List<Question> questions) throws IOException {
        Map<String, Question> byId = new LinkedHashMap<>();
        for (Question q : questions) {
            if (q.id() == null || q.id().isBlank()) {
                throw new IOException("A question in questions.json has no id");
            }
            if (byId.put(q.id(), q) != null) {
                throw new IOException("Duplicate question id '" + q.id() + "' in questions.json");
            }
        }
        return byId;
    }

    private static Reader open(String resourcePath) throws IOException {
        InputStream in = JsonLoader.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return new InputStreamReader(in, StandardCharsets.UTF_8);
    }
}
