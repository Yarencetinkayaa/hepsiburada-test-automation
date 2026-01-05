package core;

import com.google.gson.*;
import org.openqa.selenium.By;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ElementReader {

    private static final Map<String, Locator> LOCATORS = new HashMap<>();

    static {
        // Önce element-infos altında arar, yoksa resources root'a bakar
        String[] candidates = new String[]{
                "element-infos/elements.json",
                "elements.json"
        };

        InputStream is = null;
        for (String c : candidates) {
            is = ElementReader.class.getClassLoader().getResourceAsStream(c);
            if (is != null) break;
        }

        if (is == null) {
            throw new RuntimeException("elements.json bulunamadı. resources altında 'element-infos/elements.json' veya 'elements.json' olmalı.");
        }

        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);

            // Desteklenen formatlar:
            // 1) Liste: [ { "key": "...", "value": "...", "type": "css" }, ... ]
            // 2) Tek obje: { "key": "...", "value": "...", "type": "css" }
            // 3) Map: { "home.searchBox": {"type":"css","value":"..."}, ... }  (destek amaçlı)

            if (root.isJsonArray()) {
                JsonArray arr = root.getAsJsonArray();
                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) continue;
                    readKeyValueTypeObject(el.getAsJsonObject());
                }
            } else if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();

                // Eğer direkt {key,value,type} ise
                if (obj.has("key") && obj.has("value") && obj.has("type")) {
                    readKeyValueTypeObject(obj);
                } else {
                    // Map formatı ise
                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                        String k = entry.getKey();
                        JsonElement v = entry.getValue();
                        if (!v.isJsonObject()) continue;
                        JsonObject loc = v.getAsJsonObject();
                        String type = getAsString(loc, "type");
                        String value = getAsString(loc, "value");
                        if (type != null && value != null) {
                            LOCATORS.put(k, new Locator(type, value));
                        }
                    }
                }
            } else {
                throw new RuntimeException("elements.json beklenmeyen formatta.");
            }

        } catch (Exception e) {
            throw new RuntimeException("elements.json okunamadı: " + e.getMessage(), e);
        }
    }

    private static void readKeyValueTypeObject(JsonObject obj) {
        String key = getAsString(obj, "key");
        String value = getAsString(obj, "value");
        String type = getAsString(obj, "type");

        if (key == null || value == null || type == null) return;
        LOCATORS.put(key, new Locator(type, value));
    }

    private static String getAsString(JsonObject obj, String field) {
        JsonElement el = obj.get(field);
        if (el == null || el.isJsonNull()) return null;
        return el.getAsString();
    }

    public static By by(String key) {
        Locator loc = LOCATORS.get(key);
        if (loc == null) {
            throw new RuntimeException("Locator bulunamadı: " + key + " (elements.json içinde bu key yok)");
        }

        String type = loc.type.toLowerCase().trim();
        String value = loc.value;

        if ("id".equals(type)) {
            return By.id(value);
        } else if ("css".equals(type) || "cssselector".equals(type)) {
            return By.cssSelector(value);
        } else if ("xpath".equals(type)) {
            return By.xpath(value);
        } else if ("name".equals(type)) {
            return By.name(value);
        } else if ("classname".equals(type) || "class".equals(type)) {
            return By.className(value);
        } else {
            throw new RuntimeException("Desteklenmeyen locator type: " + loc.type + " (key=" + key + ")");
        }
    }

    private static class Locator {
        final String type;
        final String value;

        Locator(String type, String value) {
            this.type = type;
            this.value = value;
        }
    }
}
