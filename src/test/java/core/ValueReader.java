package core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ValueReader {

    private static final JsonObject values;

    static {
        try {
            InputStream is = ValueReader.class
                    .getClassLoader()
                    .getResourceAsStream("values-infos/test/values.json");

            if (is == null) {
                throw new RuntimeException(
                        "values.json bulunamadı: values-infos/test/values.json"
                );
            }

            values = JsonParser.parseReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)
            ).getAsJsonObject();

        } catch (Exception e) {
            throw new RuntimeException("values.json okunamadı", e);
        }
    }

    public static String get(String key) {
        JsonElement el = resolve(key);

        if (el == null || el.isJsonNull()) {
            throw new RuntimeException(
                    "values.json içinde key bulunamadı: " + key
            );
        }

        if (!el.isJsonPrimitive()) {
            throw new RuntimeException(
                    "values.json key bir string değil: " + key
            );
        }

        return el.getAsString();
    }

    private static JsonElement resolve(String key) {
        String[] parts = key.split("\\.");
        JsonElement current = values;

        for (String part : parts) {
            if (current == null || !current.isJsonObject()) {
                return null;
            }
            JsonObject obj = current.getAsJsonObject();
            if (!obj.has(part)) {
                return null;
            }
            current = obj.get(part);
        }
        return current;
    }
}
